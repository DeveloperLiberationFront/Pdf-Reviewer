package edu.ncsu.dlf.model;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.Test;

import test.TestUtils;

public  class TestPdfCommentExtraction {

    @Test
    public void testBlankPDF() throws Exception {
        Pdf pdf = loadPdf("/blank.pdf");

        try {
            assertEquals(0, pdf.getPDFComments().size());
        } finally {
            pdf.close();
        }
    }
    
    @Test
    public void testHighlightedPDF() throws Exception {
        Pdf pdf = loadPdf("/highlights.pdf");

        try {
            List<PdfComment> pdfComments = pdf.getPDFComments();

            assertEquals(5, pdfComments.size());
            String[] expectedComments = new String[] { "Look at this Introduction", "Bad short words", "Paragraph",
                    "Multi-column", "[blank]" };
            BufferedImage[] expectedImages = loadExpectedImages("highlights", 5);

            compare(pdfComments, expectedComments, expectedImages, "highlights");
        } finally {
            pdf.close();
        }
        
    }

    @Test
    public void testPopupPDF() throws Exception {
        Pdf pdf = loadPdf("/popups.pdf");

        try {
            List<PdfComment> pdfComments = pdf.getPDFComments();
            assertEquals(4, pdfComments.size());
            String[] expectedComments = new String[] { "Edge one", "Edge two", "Middle of Page", "Corner" };
            BufferedImage[] expectedImages = loadExpectedImages("popups", 4);

            compare(pdfComments, expectedComments, expectedImages, "popups");
        } finally {
            pdf.close();
        }

    }
    
    @Test
    public void testDrawingPdf() throws Exception {
        Pdf pdf = loadPdf("/drawings.pdf");

        try {
            List<PdfComment> pdfComments = pdf.getPDFComments();
            assertEquals(4, pdfComments.size());
            String[] expectedComments = new String[] { "[blank]", "Fix Please", "Was designificated", "[blank]" };
            BufferedImage[] expectedImages = loadExpectedImages("drawings", 4);

            compare(pdfComments, expectedComments, expectedImages, "drawings");
        } finally {
            pdf.close();
        }

    }

    

    private Pdf loadPdf(String pathToPdf) throws IOException {
        InputStream fos = getClass().getResourceAsStream(pathToPdf);
        InputStream commentBoxStream = getClass().getResourceAsStream("/images/comment_box.PNG");
        assertNotNull(fos);
        assertNotNull(commentBoxStream);
        return new Pdf(fos, commentBoxStream);
    }

    private BufferedImage[] loadExpectedImages(String resourceDir, int numImages) throws IOException {
        ArrayList<BufferedImage> loadedImages = new ArrayList<>();
        for(int picName = 1; picName<=numImages; picName++) {
            BufferedImage loadedImage = ImageIO.read(getClass().getResourceAsStream("/images/"+resourceDir+"/"+picName+".png"));
            assertNotNull(loadedImage);
            loadedImages.add(loadedImage);
        }
        return loadedImages.toArray(new BufferedImage[loadedImages.size()]);
    }

    private void compare(List<PdfComment> pdfComments, String[] expectedComments, BufferedImage[] expectedImages, String foldername) {
        for(int i =0;i<pdfComments.size();i++) {
            PdfComment pdfComment = pdfComments.get(i);
            assertEquals("Failed Comment Comparison for comment "+i, expectedComments[i], pdfComment.getComment());
            double difference = TestUtils.imagePercentDiff(expectedImages[i], pdfComment.getImage());
            String possibleErrorMessage = String.format("Failed image Comparison for "+foldername+"/%d.png : difference %1.4f", i+1, difference);
            assertTrue(possibleErrorMessage, difference < 0.01);
        }
    }

}
