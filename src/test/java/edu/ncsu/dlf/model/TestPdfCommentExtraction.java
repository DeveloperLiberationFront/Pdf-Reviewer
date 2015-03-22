package edu.ncsu.dlf.model;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageIO;

import edu.ncsu.dlf.model.PdfComment.Tag;

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

    @Test
    public void testCommentsOfVariousIntensities() throws Exception {
        Pdf pdf = loadPdf("/intenseFixes.pdf");

        try {
            List<PdfComment> pdfComments = pdf.getPDFComments();
            assertEquals(4, pdfComments.size());
            PDFCommentMaker comments = new PDFCommentMaker()
            .fillComments("[blank]", "Fix Please", "You might consider fixing this", "[blank]", "[blank]", "Fix Please", "You might consider fixing this")
            .fillImages(loadExpectedImages("intenseFixes", 6))
            .fillTitlesWithDefault();
            comments.setTags(1, PdfComment.Tag.MUST_FIX);
            comments.setTags(2, PdfComment.Tag.CONSIDER_FIX);
            comments.setTags(4, PdfComment.Tag.MUST_FIX);
            comments.setTags(5, PdfComment.Tag.CONSIDER_FIX);
            
            compare(comments.asList(), pdfComments, "intenseFixes");
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
    
    
    private void compare(List<ExpectedPdfComment> expected, List<PdfComment> actual, String folderName) {
        assertEquals(expected.size(), actual.size());
        for (int i = 0; i < actual.size(); i++) {
            PdfComment a = actual.get(i);
            ExpectedPdfComment e = expected.get(i);
            assertEquals("Failed Comment Comparison for comment " + i, e.expectedComment, a.getComment());
            assertEquals("Failed Title Comparison for comment " + i, e.expectedTitle, a.getTitle());
            double difference = TestUtils.imagePercentDiff(e.expectedImage, a.getImage());
            String possibleErrorMessage = String.format("Failed image Comparison for " + folderName
                    + "/%d.png : difference %1.4f", i + 1, difference);
            assertTrue(possibleErrorMessage, difference < 0.01);
            assertEquals("Failed tag comparison for comment "+i, e.expectedTags, a.getTags());
            
        }
    }


    private static class PDFCommentMaker {
        
        private List<ExpectedPdfComment> builtComments = new ArrayList<>();

        public PDFCommentMaker fillComments(String... expectedComments) {
            for (int i = 0; i < expectedComments.length; i++) {
                String expectedComment = expectedComments[i];
                if (builtComments.size() <= i) {
                    builtComments.add(new ExpectedPdfComment());
                }
                builtComments.get(i).expectedComment = expectedComment;
            }
            return this;
        }

        public List<ExpectedPdfComment> asList() {
            validateComments();
            return builtComments;
        }

        private void validateComments() {
            for(ExpectedPdfComment e : builtComments) {
                assertNotNull(e.expectedComment);
                assertNotNull(e.expectedImage);
                assertNotNull(e.expectedTitle);
            }
        }

        public void setTags(int index, Tag... expectedTags) {
            builtComments.get(index).expectedTags = Arrays.asList(expectedTags);
        }

        public PDFCommentMaker fillTitlesWithDefault() {
            for(ExpectedPdfComment e : builtComments) {
                e.expectedTitle = e.expectedComment;
            }
            return this;
        }

        public PDFCommentMaker fillImages(BufferedImage... expectedImages) {
            for (int i = 0; i < expectedImages.length; i++) {
                BufferedImage expectedImage = expectedImages[i];
                if (builtComments.size() <= i) {
                    builtComments.add(new ExpectedPdfComment());
                }
                builtComments.get(i).expectedImage = expectedImage;
            }
            return this;
        }
        
    }
    
    private static class ExpectedPdfComment {

        public List<Tag> expectedTags = new ArrayList<>();
        public BufferedImage expectedImage;
        public String expectedTitle;
        public String expectedComment;
        
    }

}
