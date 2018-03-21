package edu.ncsu.dlf.model;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;

import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationText;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationRubberStamp;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationMarkup;
import org.apache.pdfbox.pdmodel.graphics.color.PDGamma;


import org.apache.pdfbox.rendering.PDFRenderer;
import javax.imageio.ImageIO;
import javax.servlet.ServletContext;

import org.apache.pdfbox.rendering.ImageType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.InputStream;
import java.awt.image.BufferedImage;

import edu.ncsu.dlf.utils.ImageUtils;;



public class Pdf {

  private PDDocument document = null;
  private PDFRenderer renderer;
  private BufferedImage pageImage;

 // private Color highlightColor = new Color(234, 249, 35, 140);

  public static final String pathToCommentBoxImage = "/images/comment_box.PNG";
  private BufferedImage commentBoxImage;
  //private boolean DEBUG = Boolean.parseBoolean(System.getenv("DEBUG"));

 // private static final int BORDER_WIDTH = 30;
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

  public Pdf(InputStream fileStream, ServletContext context) throws IOException {
    document = PDDocument.load(fileStream);
    renderer = new PDFRenderer(document);

    //The class is technically loaded fromt he snapshot apparently, so probably better to through ServletContext
    //System.out.println(getClass().getProtectionDomain().getCodeSource().getLocation());
    commentBoxImage = ImageIO.read(context.getResourceAsStream(pathToCommentBoxImage));
  }

  public List<PdfComment> getPDFComments() throws IOException {
	  if(document == null) {
          throw new NullPointerException("PDF Document has not been instantiated.");
	  }

	  List<PdfComment> comments = new ArrayList<>();
      PDPageTree pages = document.getDocumentCatalog().getPages();

    int pageIndex = 0;
    for(PDPage page: pages) {
      pageImage = null;
      List<PDAnnotation> originalAnnotations = page.getAnnotations();

      page.setAnnotations(nonBlockingAnnotations(page.getAnnotations()));
      int size = Math.round(DEFAULT_SIZE * SCALE_UP_FACTOR);

      for(PDAnnotation annotation: originalAnnotations) {
        if(pageImage == null) {
          pageImage = renderer.renderImageWithDPI(pageIndex++, size, ImageType.RGB);
        }

        PdfComment pdfComment = turnAnnotationIntoPDFComment(annotation);
        if (pdfComment != null) {
          comments.add(pdfComment);
        }
      }

      page.setAnnotations(originalAnnotations);
    }

	  return comments;
  }

  private List<PDAnnotation> nonBlockingAnnotations(List<PDAnnotation> annotations) {
      //filters out annotations that pdfbox draws poorly so they don't blot the text out and
      //make the images hard to see.  This includes hightlight textMarkups and Popups
      List<PDAnnotation> annotationsThatAreNotTextMarkupOrPopup = new ArrayList<>();
      for(PDAnnotation annotation: annotations) {
          if (annotation instanceof PDAnnotationTextMarkup) {
              if (!PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT.equals(annotation.getSubtype())) {
                  annotationsThatAreNotTextMarkupOrPopup.add(annotation);
              }
          }
          else if (annotation.getClass() == PDAnnotationMarkup.class || annotation.getClass() == PDAnnotationRubberStamp.class) {
              annotationsThatAreNotTextMarkupOrPopup.add(annotation);
          }
      }

      return annotationsThatAreNotTextMarkupOrPopup;
  }

  private PdfComment turnAnnotationIntoPDFComment(PDAnnotation anno) {
      PdfComment pdfComment = null;

      if (anno instanceof PDAnnotationTextMarkup) {
          PDAnnotationTextMarkup comment = (PDAnnotationTextMarkup) anno;
          String writtenComment = comment.getContents();
          if (writtenComment == null || writtenComment.isEmpty()) {
              writtenComment = "[blank]";
          }
          if (PDAnnotationTextMarkup.SUB_TYPE_STRIKEOUT.equals(comment.getSubtype())) {
              writtenComment = "delete this";
          }
          pdfComment = new PdfComment(writtenComment);

          if (PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT.equals(comment.getSubtype())) {
              pdfComment.setImage(ImageUtils.makeHighlightedSubImage(pageImage, comment.getQuadPoints()));
          } else {
              pdfComment.setImage(ImageUtils.makePlainSubImage(pageImage, comment.getRectangle()));
          }
      }
      else if (anno instanceof PDAnnotationText) {
          String writtenComment = anno.getContents();
          if (writtenComment == null || writtenComment.isEmpty()) {
              writtenComment = "[blank]";
          }
          pdfComment = new PdfComment(writtenComment);

          pdfComment.setImage(ImageUtils.makePopupSubImage(pageImage, anno.getRectangle(), commentBoxImage));
      }
      else if (anno.getContents() != null || anno.getAppearance() != null) {
          String writtenComment = anno.getContents();
          if (writtenComment == null || writtenComment.isEmpty()) {
              writtenComment = "[blank]";
          }
          pdfComment = new PdfComment(writtenComment);

          pdfComment.setImage(ImageUtils.makePlainSubImage(pageImage, anno.getRectangle()));

      }
      return pdfComment;
  }

  }

