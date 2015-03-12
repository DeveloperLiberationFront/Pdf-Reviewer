package edu.ncsu.dlf.model;

import java.awt.Color;
import java.awt.Graphics2D;
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

import edu.ncsu.dlf.model.PdfComment.Tag;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.color.PDGamma;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;


/** 
 * Puts a wrapper around the PDF library
 *
 */
public class Pdf {
    private static final int BORDER_WIDTH = 30;
    private static final int SCALE_UP_FACTOR = 2;
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
	
	
	public Pdf(InputStream input) throws IOException {
		doc = PDDocument.load(input);
	}
	
	@Deprecated
	public List<String> getComments() {
		List<String> comments = new ArrayList<>();
		 
		@SuppressWarnings("unchecked")
		List<PDPage> pages = doc.getDocumentCatalog().getAllPages();
		 
		 for(PDPage page : pages) {
			 try {
				 for(PDAnnotation anno : page.getAnnotations()) {
					 if(anno instanceof PDAnnotationTextMarkup) {
						 PDAnnotationTextMarkup comment = (PDAnnotationTextMarkup) anno;
						 
						 if(comment.getContents() != null) {
							 comments.add(comment.getContents());
						 }
					 }
				 }
			 } catch(IOException e) {}
		 }
		 
		 return comments;
	}
	
	public List<PdfComment> getPDFComments() {
        List<PdfComment> comments = new ArrayList<>();
        
        @SuppressWarnings("unchecked")
        List<PDPage> pages = doc.getDocumentCatalog().getAllPages();
         
         for(PDPage page : pages) {
             try {
                 BufferedImage img = null;

                 List<PDAnnotation> annotations = page.getAnnotations();
                 
                 //erase annotations from page to avoid them blotting out text
                page.setAnnotations(Collections.<PDAnnotation>emptyList());
                 
                for(PDAnnotation anno : annotations) {              
                     if (img == null) {
                         img = page.convertToImage(BufferedImage.TYPE_INT_RGB, DEFAULT_SIZE * SCALE_UP_FACTOR);
                     }
                    if (anno instanceof PDAnnotationTextMarkup) {
                        PDAnnotationTextMarkup comment = (PDAnnotationTextMarkup) anno;
                        if (comment != null) {
                            System.out.println(comment.getContents());

                            if (comment.getContents() != null) {
                                PdfComment pdfComment = new PdfComment(comment.getContents());

                                pdfComment.setImage(makeSubImage(img, comment.getQuadPoints()));
                                comments.add(pdfComment);
                            }
                        }
                    }
                     
                 }
             } catch(IOException e) {
                 e.printStackTrace();
             }
         }
         
         return comments;
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
    private BufferedImage makeSubImage(BufferedImage img, float[] quadPoints) {
        if (quadPoints.length < 8) {
            return null;
        }

        //we find the upper left corner
        int minX = getMinXFromQuadPoints(quadPoints);
        int minY = getMinYFromQuadPoints(quadPoints);

        int width = (getMaxXFromQuadPoints(quadPoints) - minX + 2* BORDER_WIDTH) * SCALE_UP_FACTOR;
        int height = (getMaxYFromQuadPoints(quadPoints) - minY + 2* BORDER_WIDTH) * SCALE_UP_FACTOR;

        int x = (minX-BORDER_WIDTH) * SCALE_UP_FACTOR;  
        int y = (minY-BORDER_WIDTH) * SCALE_UP_FACTOR;

        BufferedImage subImage = null;

        // the y is counted from the bottom, so we have to flip our coordinate
        subImage = img.getSubimage(x, (img.getHeight() - y - height), width, height);

        Graphics2D g2 = subImage.createGraphics();
        for (int n = 0; n < quadPoints.length; n += 8) {
            float[] oneQuad = Arrays.copyOfRange(quadPoints, n, n + 8);
            paintHighlight(g2, oneQuad, x, y);
        }

        try {
            // for debugging
            ImageIO.write(subImage, "png", new File("test"+Math.random()+".png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return subImage;
    }
    
    private void paintHighlight(Graphics2D g2, float[] oneQuad, int xOffset, int yOffset) {
        int x = getMinXFromQuadPoints(oneQuad);
        int y = getMinYFromQuadPoints(oneQuad);

        int width = (getMaxXFromQuadPoints(oneQuad) - x) * SCALE_UP_FACTOR;
        int height = (getMaxYFromQuadPoints(oneQuad) - y) * SCALE_UP_FACTOR;

        x *= SCALE_UP_FACTOR;
        y *= SCALE_UP_FACTOR;
        
        x -= xOffset;
        y -= yOffset;
        
        g2.setColor(highlightColor);
        g2.fillRect(x, y, width, height);

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
					
					if(anno instanceof PDAnnotationTextMarkup) {
					    if(anno.getContents() != null) {
					        PdfComment userComment = comments.get(commentOn);
					        commentOn++;
					        String newMessage = userComment.getMessageWithLink(repoOwner, repo);
                            PDAnnotationTextMarkup newComment = makeNewAnnotation((PDAnnotationTextMarkup) anno, userComment, newMessage);
					        
                            newList.add(newComment);
                        }
					}
				}			
				page.setAnnotations(newList);
			} catch(IOException e) {
			    e.printStackTrace();
			} finally {
			    page.clear();
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
	
	public static void main(String[] args) throws Exception{
	    FileInputStream fos = new FileInputStream("test.pdf");
	    Pdf pdf = new Pdf(fos);

	    System.out.println(pdf.getPDFComments());
	}
}
