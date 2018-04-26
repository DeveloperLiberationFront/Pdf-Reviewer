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
import javax.imageio.ImageIO;

import org.apache.pdfbox.rendering.ImageType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.InputStream;
import java.awt.image.BufferedImage;

import edu.ncsu.dlf.utils.ImageUtils;

import edu.ncsu.dlf.model.PdfComment.Tag;

public class Pdf {

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


  public Pdf(InputStream fileStream, InputStream commentBoxImageStream) throws IOException {
    System.setProperty("sun.java2d.cmm", "sun.java2d.cmm.kcms.KcmsServiceProvider");
    document = PDDocument.load(fileStream);
    renderer = new PDFRenderer(document);

    commentBoxImage = ImageIO.read(commentBoxImageStream);
  }

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

    //Makes a brand new text annotation that is almost exactly like the one passed in.
	// this is the only way I could get the annotations to actually change color.
    private PDAnnotationTextMarkup makeNewAnnotation(PDAnnotationTextMarkup comment, PdfComment userComment, String messageWithLink) {
        PDAnnotationTextMarkup newComment = new PDAnnotationTextMarkup(PDAnnotationTextMarkup.SUB_TYPE_HIGHLIGHT);
        List<Tag> tags = userComment.getTags();
        if (tags.contains(Tag.CONSIDER_FIX) || tags.contains(Tag.POSITIVE)) {

            //setColour() moved to setColor()
            //https://pdfbox.apache.org/docs/2.0.8/javadocs/org/apache/pdfbox/pdmodel/interactive/annotation/PDAnnotation.html#setColor(org.apache.pdfbox.pdmodel.graphics.color.PDColor)
            newComment.setColor(GREEN);
        } else if (tags.contains(Tag.MUST_FIX)) {
            newComment.setColor(ORANGE);
        } else {
            newComment.setColor(YELLOW);
        }
        newComment.setContents(messageWithLink);

        //System.out.println(newComment.getContents());

        newComment.setRectangle(comment.getRectangle());   //both rectangle and quadpoints are needed... don't know why
        newComment.setQuadPoints(comment.getQuadPoints());
        newComment.setSubject(comment.getSubject());
        newComment.setTitlePopup(comment.getTitlePopup());    //author name
        return newComment;
    }

    public PDDocument getDocument() {
        return document;
    }

    public void close() throws IOException {
	    document.close();
	}

  }

