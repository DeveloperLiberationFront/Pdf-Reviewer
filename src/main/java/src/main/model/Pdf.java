package src.main.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.graphics.color.PDGamma;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;

import src.main.model.PdfComment.Tag;


/** 
 * Puts a wrapper around the PDF library
 *
 */
public class Pdf {
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
			    page.clearCache();
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
}
