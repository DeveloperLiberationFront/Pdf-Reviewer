package edu.ncsu.dlf.model;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageTree;

import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationTextMarkup;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationText;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationRubberStamp;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationMarkup;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.color.PDGamma;


import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import javax.imageio.ImageIO;
import org.apache.pdfbox.rendering.ImageType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.InputStream;
import java.awt.image.BufferedImage;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Collections;



public class Pdf {

  private PDDocument document = null;
  private PDFRenderer renderer;
  private BufferedImage pageImage;

  private Color highlightColor = new Color(234, 249, 35, 140);

  // Not sure what the purpose of these are, also included in old constructor??
  public static final String pathToCommentBoxImage = "/images/comment_box.PNG";
  private BufferedImage commentBoxImage;
  private boolean DEBUG = Boolean.parseBoolean(System.getenv("DEBUG"));

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

  public Pdf(InputStream fileStream) throws IOException {
  	  document = PDDocument.load(fileStream);
      renderer = new PDFRenderer(document);
  }

  public List<PdfComment> getPDFComments() throws IOException {
	  if(document == null) {
		  // throw Exception();
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
              pdfComment.setImage(makeHighlightedSubImage(pageImage, comment.getQuadPoints()));
          } else {
              pdfComment.setImage(makePlainSubImage(pageImage, comment.getRectangle()));
          }
      }
      else if (anno instanceof PDAnnotationText) {
          String writtenComment = anno.getContents();
          if (writtenComment == null || writtenComment.isEmpty()) {
              writtenComment = "[blank]";
          }
          pdfComment = new PdfComment(writtenComment);

          pdfComment.setImage(makePopupSubImage(pageImage, anno.getRectangle()));
      }
      else if (anno.getContents() != null || anno.getAppearance() != null) {
          String writtenComment = anno.getContents();
          if (writtenComment == null || writtenComment.isEmpty()) {
              writtenComment = "[blank]";
          }
          pdfComment = new PdfComment(writtenComment);

          pdfComment.setImage(makePlainSubImage(pageImage, anno.getRectangle()));

      }
      return pdfComment;
  }

  private BufferedImage makePlainSubImage(BufferedImage image, PDRectangle r) {
      float[] convertedQuadPoints = rectToQuadArray(r);
      return makeSubImage(image, convertedQuadPoints, PostExtractMarkup.NONE);
  }

  private BufferedImage makeHighlightedSubImage(BufferedImage img, float[] quadPoints) {
      return makeSubImage(img, quadPoints, PostExtractMarkup.HIGHLIGHTS);
  }

  private BufferedImage makePopupSubImage(BufferedImage img, PDRectangle r) {
      float[] convertedQuadPoints = rectToQuadArray(r);
      return makeSubImage(img, convertedQuadPoints, PostExtractMarkup.POPUP);
  }

  private float[] rectToQuadArray(PDRectangle r) {
      float[] convertedQuadPoints = new float[]{r.getLowerLeftX(),r.getLowerLeftY(),
              r.getUpperRightX(), r.getLowerLeftY(), r.getLowerLeftX(), r.getUpperRightY(),
              r.getUpperRightX(), r.getUpperRightY()};
      return convertedQuadPoints;
  }

  private enum PostExtractMarkup {
      NONE(1), HIGHLIGHTS(1), POPUP(2);

      public final float subImageContextMultiplier;

      PostExtractMarkup(float subImageContextMultiplier) {
          this.subImageContextMultiplier = subImageContextMultiplier;
      }
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

      Rectangle subImageRect = scaleAndTransformSubImageQuad(quadPoints, img, markup);

      BufferedImage subImage = img.getSubimage(subImageRect.x, subImageRect.y, subImageRect.width, subImageRect.height);

      BufferedImage newImage = new BufferedImage(subImage.getWidth(), subImage.getHeight(), img.getType());
      Graphics2D g2 = newImage.createGraphics();

      g2.drawImage(subImage, 0, 0, null);

      if (markup == PostExtractMarkup.HIGHLIGHTS) {
          for (int n = 0; n < quadPoints.length; n += 8) {
              float[] oneQuad = Arrays.copyOfRange(quadPoints, n, n + 8);

              paintHighlight(g2, scaleAndTransformAnnotationQuad(oneQuad, subImageRect, img.getHeight()));
          }
      } else if (markup == PostExtractMarkup.POPUP) {
          //we know quadPoints will be only one quad because that's how makePopupSubImage defines it.
          paintCommentBox(g2, scaleAndTransformAnnotationQuad(quadPoints, subImageRect, img.getHeight()));
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

  private Rectangle scaleAndTransformSubImageQuad(float[] quadPoints, RenderedImage img, PostExtractMarkup markup) {
      // we find the upper left corner
      int minX = getMinXFromQuadPoints(quadPoints);
      int minY = getMinYFromQuadPoints(quadPoints);

      // allows us to make
      float scaledBorder = BORDER_WIDTH * markup.subImageContextMultiplier;

      int x = Math.round((minX - scaledBorder) * SCALE_UP_FACTOR);
      x = Math.max(x, 0); // keep subimage on screen
      int y = Math.round((minY - scaledBorder) * SCALE_UP_FACTOR);
      y = Math.max(y, 0); // keep subimage on screen

      int width = Math.round((getMaxXFromQuadPoints(quadPoints) - minX + 2 * scaledBorder) * SCALE_UP_FACTOR);
      width = Math.min(width, img.getWidth() - x); // clamp width
      int height = Math.round((getMaxYFromQuadPoints(quadPoints) - minY + 2 * scaledBorder) * SCALE_UP_FACTOR);
      height = Math.min(height, img.getHeight() - y); // clamp height

      // the y is counted from the bottom, so we have to flip our coordinate
      y = (img.getHeight() - y - height);
      return new Rectangle(x, y, width, height);
  }

  private Rectangle scaleAndTransformAnnotationQuad(float[] oneQuad, Rectangle boundingRect, int imageHeight) {
      int x = getMinXFromQuadPoints(oneQuad);
      int y = getMinYFromQuadPoints(oneQuad);

      int width = Math.round((getMaxXFromQuadPoints(oneQuad) - x) * SCALE_UP_FACTOR);
      int height = Math.round((getMaxYFromQuadPoints(oneQuad) - y) * SCALE_UP_FACTOR);

      x *= SCALE_UP_FACTOR;
      y *= SCALE_UP_FACTOR;

      x -= boundingRect.x;
      // invert y again
      y = imageHeight - y - boundingRect.y - height;

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


}
