package edu.ncsu.dlf.model;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;

import edu.ncsu.dlf.model.PdfComment.Tag;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDGamma;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationText;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;


/** 
 * Puts a wrapper around the PDF library
 *
 */
public class Pdf {
    private static final int BORDER_WIDTH = 30;
    private static final float SCALE_UP_FACTOR = 2.0f;
    private static final int DEFAULT_SIZE = 72;
    
	private static final PDGamma ORANGE = new PDGamma();
	private static final PDGamma GREEN = new PDGamma();
    private static final PDGamma YELLOW = new PDGamma();

	static {
	    ORANGE.setR(0.9921568627f);
	    ORANGE.setG(0.5333333333f);
	    ORANGE.setB(0.1803921568f);
	    
	    GREEN.setR(0);
	    GREEN.setG(1);
	    GREEN.setB(0);
	    
	    YELLOW.setR(1);
	    YELLOW.setG(1);
	    YELLOW.setB(0);
	}
    private PDDocument doc;
    private Color highlightColor = new Color(234, 249, 35, 140);
    public static final String pathToCommentBoxImage = "/images/comment_box.PNG";
    private BufferedImage commentBoxImage;
    private boolean DEBUG = Boolean.parseBoolean(System.getenv("DEBUG"));
	
    
    private Pdf(InputStream pdfInputStream, InputStream commentBoxInputStream) throws IOException {
        this.doc = PDDocument.load(pdfInputStream);
        if (commentBoxInputStream != null)
            this.commentBoxImage = ImageIO.read(commentBoxInputStream);
    }
	
	public Pdf(InputStream input, ServletContext servletContext) throws IOException {
		this(input, servletContext.getResourceAsStream(pathToCommentBoxImage));
		if (this.commentBoxImage == null) {
		    System.out.println(servletContext.getRealPath(pathToCommentBoxImage));
		}
	}
	
    public List<PdfComment> getPDFComments() {
        List<PdfComment> comments = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<PDPage> pages = doc.getDocumentCatalog().getAllPages();

        for (PDPage page : pages) {
            try {
                BufferedImage pageImage = null;

                List<PDAnnotation> annotations = page.getAnnotations();

                // erase annotations from page to avoid them blotting out text
                page.setAnnotations(Collections.<PDAnnotation> emptyList());

                for (PDAnnotation anno : annotations) {
                    if (pageImage == null) {
                        int size = Math.round(DEFAULT_SIZE * SCALE_UP_FACTOR);
                        pageImage = page.convertToImage(BufferedImage.TYPE_INT_RGB, size);
                    }
                    if (anno instanceof PDAnnotationTextMarkup) {
                        PDAnnotationTextMarkup comment = (PDAnnotationTextMarkup) anno;
                        String writtenComment = comment.getContents();
                        if (writtenComment == null) {
                            writtenComment = "[blank]";
                        }
                        PdfComment pdfComment = new PdfComment(writtenComment);

                        pdfComment.setImage(makeHighlightedSubImage(pageImage, comment.getQuadPoints()));
                        comments.add(pdfComment);
                    } else if (anno instanceof PDAnnotationText ){
                        String writtenComment = anno.getContents();
                        if (writtenComment == null) {
                            writtenComment = "[blank]";
                        }
                        PdfComment pdfComment = new PdfComment(writtenComment);

                        pdfComment.setImage(makePopupSubImage(pageImage, anno.getRectangle()));
                        comments.add(pdfComment);
                    }

                }
                // restore annotations
                page.setAnnotations(annotations);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return comments;
    }
	
    private BufferedImage makeHighlightedSubImage(BufferedImage img, float[] quadPoints) {
        return makeSubImage(img, quadPoints, PostExtractMarkup.HIGHLIGHTS);
    }

    private BufferedImage makePopupSubImage(BufferedImage img, PDRectangle r) {
        float[] convertedQuadPoints = new float[]{r.getLowerLeftX(),r.getLowerLeftY(),
                r.getUpperRightX(), r.getLowerLeftY(), r.getLowerLeftX(), r.getUpperRightY(),
                r.getUpperRightX(), r.getUpperRightY()};
        return makeSubImage(img, convertedQuadPoints, PostExtractMarkup.POPUP);
    }

    private enum PostExtractMarkup {
        NONE, HIGHLIGHTS, POPUP
    }

    /* adapted the specs of a pdf tool http://www.pdf-technologies.com/api/html/P_PDFTech_PDFMarkupAnnotation_QuadPoints.htm
     * The QuadPoints array must contain 8*n elements specifying the coordinates of n quadrilaterals. 
     * Each quadrilateral encompasses a word or group of continuous words in the text underlying the annotation. 
     * The coordinates for each quadrilateral are given in the order x4 y4 x3 y3 x1 y1 x2 y2 specifying the quadrilateral's 
     * four vertices  x1 is upper left and numbering goes clockwise. 
     * 
     * I assume all quadrilaterals are rectangles.  No guarantees on multi-column selects
     * 
     */
    private BufferedImage makeSubImage(BufferedImage img, float[] quadPoints, PostExtractMarkup markup) {
        if (quadPoints.length < 8) {
            return null;
        }

        //we find the upper left corner
        int minX = getMinXFromQuadPoints(quadPoints);
        int minY = getMinYFromQuadPoints(quadPoints);

        int x = Math.round((minX-BORDER_WIDTH) * SCALE_UP_FACTOR);  
        int y = Math.round((minY-BORDER_WIDTH) * SCALE_UP_FACTOR);
        
        int width = Math.round((getMaxXFromQuadPoints(quadPoints) - minX + 2* BORDER_WIDTH) * SCALE_UP_FACTOR);
        width = Math.min(width, img.getWidth() - x);  //clamp width
        int height = Math.round((getMaxYFromQuadPoints(quadPoints) - minY + 2* BORDER_WIDTH) * SCALE_UP_FACTOR);
        height = Math.min(height, img.getHeight() - y);  //clamp height

        BufferedImage subImage = img.getSubimage(x, (img.getHeight() - y - height), width, height);
        
        BufferedImage newImage = new BufferedImage(subImage.getWidth(), subImage.getHeight(), img.getType());
        Graphics2D g2 = newImage.createGraphics();
        // the y is counted from the bottom, so we have to flip our coordinate
        g2.drawImage(subImage, 0, 0, null);
       
        if (markup == PostExtractMarkup.HIGHLIGHTS) {
            for (int n = 0; n < quadPoints.length; n += 8) {
                float[] oneQuad = Arrays.copyOfRange(quadPoints, n, n + 8);
                
                paintHighlight(g2, scaleAndTransformQuad(oneQuad, x, y, height));
            }
        } else if (markup == PostExtractMarkup.POPUP) {
            //we know quadPoints will be only one quad because that's how makePopupSubImage defines it.
            paintCommentBox(g2, scaleAndTransformQuad(quadPoints, x, y, height));
        }
        
        g2.dispose();

        if (DEBUG) {
            try {
                // for debugging                
                File output = new File("test"+Math.random()+".png");
                System.out.println("Saving image to disk "+output.getAbsolutePath());
                ImageIO.write(newImage, "png", output);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        

        return newImage;
    }
    
    private Rectangle scaleAndTransformQuad(float[] oneQuad, int xOffset, int yOffset, int imageHeight) {
        int x = getMinXFromQuadPoints(oneQuad);
        int y = getMinYFromQuadPoints(oneQuad);

        int width = Math.round((getMaxXFromQuadPoints(oneQuad) - x) * SCALE_UP_FACTOR);
        int height = Math.round((getMaxYFromQuadPoints(oneQuad) - y) * SCALE_UP_FACTOR);

        x *= SCALE_UP_FACTOR;
        y *= SCALE_UP_FACTOR;

        x -= xOffset;
        y -= yOffset;
        // again, invert the y axis
        y = imageHeight - y - height;
        
        return new Rectangle(x, y, width, height);
    }
    
    private void paintCommentBox(Graphics2D g2, Rectangle rect) {
        if (commentBoxImage != null) {
            g2.drawImage(commentBoxImage, rect.x, rect.y, rect.width, rect.height, null);
        } else {
            g2.setColor(highlightColor);
            g2.setStroke(new BasicStroke(2 * SCALE_UP_FACTOR));
            g2.drawRect(rect.x, rect.y , rect.width, rect.height);
        }
        
    }

    private void paintHighlight(Graphics2D g2, Rectangle rect) {
        g2.setColor(highlightColor);
        
        g2.fillRect(rect.x, rect.y , rect.width, rect.height);

    }
    
    
    //x values are on the even integers
    private static int getMinXFromQuadPoints(float[] quadPoints) {
        int min = Integer.MAX_VALUE;
        for(int i = 0; i< quadPoints.length; i += 2) {
            if (quadPoints[i] < min) {
                min = (int)quadPoints[i];
            }
        }
        return min;
    }
    
    //y values are on the even integers
    private static int getMinYFromQuadPoints(float[] quadPoints) {
        int min = Integer.MAX_VALUE;
        for(int i = 1; i< quadPoints.length; i += 2) {
            if (quadPoints[i] < min) {
                min = (int)quadPoints[i];
            }
        }
        return min;
    }
    
    //x values are on the even integers
    private static int getMaxXFromQuadPoints(float[] quadPoints) {
        int max = 0;
        for(int i = 0; i< quadPoints.length; i += 2) {
            if (quadPoints[i] > max) {
                max = (int)quadPoints[i];
            }
        }
        return max;
    }
    
    //y values are on the even integers
    private static int getMaxYFromQuadPoints(float[] quadPoints) {
        int max = 0;
        for(int i = 1; i< quadPoints.length; i += 2) {
            if (quadPoints[i] > max) {
                max = (int)quadPoints[i];
            }
        }
        return max;
    }
	
	public void updateComments(List<PdfComment> comments, String repoOwner, String repo) {
		@SuppressWarnings("unchecked")
		List<PDPage> pages = doc.getDocumentCatalog().getAllPages();
		int commentOn = 0;
		for(PDPage page : pages) {
			try {
			    List<PDAnnotation> newList = new ArrayList<PDAnnotation>(); 
				List<PDAnnotation> annotations = page.getAnnotations();
				for(int i=0; i<annotations.size(); i++) {
					PDAnnotation anno = annotations.get(i);
					
                    if (anno instanceof PDAnnotationTextMarkup) {
                        PdfComment userComment = comments.get(commentOn);
                        commentOn++;
                        String newMessage = userComment.getMessageWithLink(repoOwner, repo);
                        PDAnnotationTextMarkup newComment = makeNewAnnotation((PDAnnotationTextMarkup) anno, userComment, newMessage);

                        newList.add(newComment);
                    } else {
                        newList.add(anno);
                    }
                }		
				page.setAnnotations(newList);
			} catch(IOException e) {
			    e.printStackTrace();
			} finally {
			    //page.clear();
			    page.updateLastModified();
			}
		}
	}

	//Makes a brand new text annotation that is almost exactly like the one passed in.
	// this is the only way I could get the annotations to actually change color.
    private PDAnnotationTextMarkup makeNewAnnotation(PDAnnotationTextMarkup comment, PdfComment userComment, String messageWithLink) {
        PDAnnotationTextMarkup newComment = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
        List<Tag> tags = userComment.getTags();
        if (tags.contains(Tag.CONSIDER_FIX) || tags.contains(Tag.POSITIVE)) {
            newComment.setColour(GREEN);
        } else if (tags.contains(Tag.MUST_FIX)) {
            newComment.setColour(ORANGE);
        } else {
            newComment.setColour(YELLOW);
        }
        newComment.setContents(messageWithLink);

        newComment.setRectangle(comment.getRectangle());   //both rectangle and quadpoints are needed... don't know why
        newComment.setQuadPoints(comment.getQuadPoints());
        newComment.setSubject(comment.getSubject());
        newComment.setTitlePopup(comment.getTitlePopup());    //author name
        return newComment;
    }
    
    

	public PDDocument getDoc() {
		return doc;
	}
	
	public void close() throws IOException {
		doc.close();
	}
	
	@SuppressWarnings("unused")
    private static void main(String[] args) throws Exception{
	    FileInputStream fos = new FileInputStream("test.pdf");
	    Pdf pdf = new Pdf(fos, new FileInputStream("src/main/resources/comment_box.PNG"));

	    System.out.println(pdf.getPDFComments());
	}
}
