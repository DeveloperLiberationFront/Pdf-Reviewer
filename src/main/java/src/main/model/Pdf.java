package src.main.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDGamma;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAppearanceDictionary;


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
			    List<PDAnnotation> newList = new ArrayList<PDAnnotation>(); 
				List<PDAnnotation> annotations = page.getAnnotations();
				for(int i=0; i<annotations.size(); i++) {
					PDAnnotation anno = annotations.get(i);
					
					if(anno instanceof PDAnnotationTextMarkup) {
					    PDAnnotationTextMarkup comment = (PDAnnotationTextMarkup) anno;
	                    PDAnnotationTextMarkup thing = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
	                    thing.setColour(GREEN);
	                    thing.setRectangle(comment.getRectangle());
	                    thing.setQuadPoints(comment.getQuadPoints());
	                    thing.setInvisible(false);
	                    thing.setAnnotationFlags(PDAnnotationTextMarkup.FLAG_PRINTED);
						thing.setAppearanceStream(comment.getAppearanceStream());
						thing.setConstantOpacity(1.0f);
						thing.setSubject(comment.getSubject());
						if(comment.getContents() != null) {
							PdfComment userComment = comments.get(commentOn);
                            thing.setContents(userComment.getMessageWithLink(login, repo));
							commentOn++;
						}
						newList.add(thing);
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
	
	public PDDocument getDoc() {
		return doc;
	}
	
	public void close() throws IOException {
		doc.close();
	}
}
