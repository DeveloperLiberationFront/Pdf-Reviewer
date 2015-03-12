package edu.ncsu.dlf.model;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
    private static final int BORDER_WIDTH = 10;
    
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
                 for(PDAnnotation anno : page.getAnnotations()) {
                     if (img == null) {
                         img = page.convertToImage();
                     }
                     if(anno instanceof PDAnnotationTextMarkup) {
                         PDAnnotationTextMarkup comment = (PDAnnotationTextMarkup) anno;
                         
                         if(comment.getContents() != null) {
                            PdfComment pdfComment = new PdfComment(comment.getContents());
                            
                            pdfComment.setImage(makeSubImage(img,comment.getQuadPoints()));
                            comments.add(pdfComment);
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
     */
    private BufferedImage makeSubImage(BufferedImage img, float[] quadPoints) {
        if (quadPoints.length < 8) {
            return null;
        }
        int x = (int) (quadPoints[4]-BORDER_WIDTH) * 2;
        int y = (int) (quadPoints[5]-BORDER_WIDTH) * 2;
        
        int width = (int) (quadPoints[2] - quadPoints[4] + 2* BORDER_WIDTH) * 2;
        int height = (int) (quadPoints[3] - quadPoints[5] + 2* BORDER_WIDTH) * 2;
        
        
        //for debugging
        try {
            ImageIO.write(img.getSubimage(x, (img.getHeight() - y - height), width, height), "png", new File("test.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return img.getSubimage(x, y, width, height);


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
