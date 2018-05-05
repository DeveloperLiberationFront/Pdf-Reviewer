package edu.ncsu.dlf.model;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDPageLabels;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationText;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationRubberStamp;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationMarkup;

import org.apache.pdfbox.pdmodel.graphics.color.PDColor;
import org.apache.pdfbox.pdmodel.graphics.color.PDDeviceRGB;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;

import javax.imageio.ImageIO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.InputStream;
import java.awt.image.BufferedImage;

import edu.ncsu.dlf.utils.ImageUtils;

import edu.ncsu.dlf.model.PdfComment.Tag;

  /**
   * The class is used to represent a PDF document in the system.
   * Converts raw PDF data to a PDF representation in Apache PDFBox (https://pdfbox.apache.org/)
   * and then retrieves PDF annotations as a list of PdfComments objects. 
   * @author Team19
   */
public class Pdf {

  //Document object from the Apache PDFBox library
  private PDDocument document = null;
  private PDFRenderer renderer;

  private BufferedImage pageImage;
  private BufferedImage commentBoxImage;

  private static final float SCALE_UP_FACTOR = 2.0f;
  private static final int DEFAULT_SIZE = 72;

  private static final PDColor ORANGE = new PDColor(
        new float[] { 0.9921568627f, 0.5333333333f, 0.1803921568f },
        PDDeviceRGB.INSTANCE
    );

    private static final PDColor GREEN = new PDColor(
        new float[] { 0, 1, 0 },
        PDDeviceRGB.INSTANCE
    );

    private static final PDColor YELLOW = new PDColor(
        new float[] { 1, 1, 0 },
        PDDeviceRGB.INSTANCE
    );

  /**
   * Create a new PDF object with the raw PDF file data and a stream of the comment box image.
   * @param fileStream raw PDF file data
   * @param commentBoxImageStream image of a comment box
   */
  public Pdf(InputStream fileStream, InputStream commentBoxImageStream) throws IOException {
    //https://pdfbox.apache.org/2.0/getting-started.html
    System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");

    document = PDDocument.load(fileStream);
    renderer = new PDFRenderer(document);

    commentBoxImage = ImageIO.read(commentBoxImageStream);
  }

  /**
   * Iterates through the PDF document and makes each annotation into a PdfComment object
   * @return A list of PDFComment objects which map to annotations from the document
   */
  public List<PdfComment> getPDFComments() throws IOException {
	  if(document == null) {
          throw new NullPointerException("PDF Document has not been instantiated.");
	  }

	  List<PdfComment> comments = new ArrayList<>();
      PDPageTree pages = document.getDocumentCatalog().getPages();

      String[] pageLabels = new PDPageLabels(document).getLabelsByPageIndices();

    for(PDPage page: pages) {
      pageImage = null;
      int pageIndex = pages.indexOf(page);
      List<PDAnnotation> originalAnnotations = page.getAnnotations();

      page.setAnnotations(nonBlockingAnnotations(page.getAnnotations()));
      int size = Math.round(DEFAULT_SIZE * SCALE_UP_FACTOR);

      for(PDAnnotation annotation: originalAnnotations) {
        if(pageImage == null) {
          pageImage = renderer.renderImageWithDPI(pageIndex, size, ImageType.RGB);
        }

        PdfComment pdfComment = turnAnnotationIntoPDFComment(annotation);
        if (pdfComment != null) {
            pdfComment.setPageNumber(pageLabels[pageIndex]);
            comments.add(pdfComment);
        }
      }

      page.setAnnotations(originalAnnotations);
    }

	  return comments;
  }

  /**
   * Filters out annotations that PDFBox draws poorly so they don't blot the text out and
   * make the images hard to see.  This includes hightlight textMarkups and Popups
   * @return A list of annotations that are not text markup or popup
   */
  private List<PDAnnotation> nonBlockingAnnotations(List<PDAnnotation> annotations) {

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

  /**
   * Turns a PDAnnotation object into a PdfComment object
   * @param anno represents a PDF annotation
   * @return A PdfComment object (with an image) thats maps to the annotation
   */
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

  /**
   * Updates a list of PdfComment objects to have colors and links to issues
   * @param comments list of PdfComment objects
   * @param repo the GitHub repository the issues will be added to
   */
  public void updateCommentsWithColorsAndLinks(List<PdfComment> comments, Repo repo) {
    PDPageTree pages = document.getDocumentCatalog().getPages();
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
                        String newMessage = userComment.getMessageWithLink(repo);
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

                //Can't find a substitute for this, is it necessary?
                //page.updateLastModified();
            }
        }
    }

    /**
     * Makes a brand new text annotation that is almost exactly like the one passed in.
     * this is the only way I could get the annotations to actually change color.
     * @param comment original PDAnnotation object
     * @param userComment PdfComment object of original PDAnnotation object
     * @param messageWithLink comment message with link to issue in GitHub
     * @return PDAnnotationTextMarkup representing the original annotation with 
     * new colors and an issue link.
     */
    private PDAnnotationTextMarkup makeNewAnnotation(PDAnnotationTextMarkup comment, PdfComment userComment, String messageWithLink) {
        PDAnnotationTextMarkup newComment = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
        List<Tag> tags = userComment.getTags();
        if (tags.contains(Tag.CONSIDER_FIX) || tags.contains(Tag.POSITIVE)) {
            newComment.setColor(GREEN);
        } else if (tags.contains(Tag.MUST_FIX)) {
            newComment.setColor(ORANGE);
        } else {
            newComment.setColor(YELLOW);
        }
        newComment.setContents(messageWithLink);

        newComment.setRectangle(comment.getRectangle());   //both rectangle and quadpoints are needed... don't know why
        newComment.setQuadPoints(comment.getQuadPoints());
        newComment.setSubject(comment.getSubject());
        newComment.setTitlePopup(comment.getTitlePopup());    //author name
        return newComment;
    }

    /**
     * @return PDDocument object created from the raw PDF data
     */
    public PDDocument getDocument() {
        return document;
    }

    /**
     * Closes the PDDocument object
     */
    public void close() throws IOException {
	    document.close();
	}

  }

