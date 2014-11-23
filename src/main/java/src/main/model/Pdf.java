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


/** 
 * Puts a wrapper around the PDF library
 *
 */
public class Pdf {
	private static final PDGamma ORANGE = new PDGamma();
	private static final PDGamma GREEN = new PDGamma();
	
	static {
	    ORANGE.setR(0.9921568627f);
	    ORANGE.setG(0.5333333333f);
	    ORANGE.setB(0.1803921568f);
	    
	    GREEN.setR(0);
	    GREEN.setG(1);
	    GREEN.setB(0);
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
	
	public void updateComments(List<PdfComment> comments, String login, String repo) {
		@SuppressWarnings("unchecked")
		List<PDPage> pages = doc.getDocumentCatalog().getAllPages();
		int commentOn = 0;
		for(PDPage page : pages) {
			try {
				List<PDAnnotation> annotations = page.getAnnotations();
				for(int i=0; i<annotations.size(); i++) {
					PDAnnotation anno = annotations.get(i);
					
					if(anno instanceof PDAnnotationTextMarkup) {
						PDAnnotationTextMarkup comment = (PDAnnotationTextMarkup) anno;
						
						if(comment.getContents() != null) {
							PdfComment userComment = comments.get(commentOn);
                            comment.setContents(userComment.getMessageWithLink(login, repo));
							commentOn++;
						}
						anno.setColour(ORANGE);
					}
				}
			} catch(IOException e) {
			    e.printStackTrace();
			}
		}
	}
	
	public PDDocument getDoc() {
		return doc;
	}
	
	public void close() throws IOException {
		doc.close();
	}
}
