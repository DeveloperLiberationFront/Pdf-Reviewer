// package edu.ncsu.dlf.model;
//
// import static org.junit.Assert.*;
//
// import java.awt.image.BufferedImage;
// import java.io.ByteArrayInputStream;
// import java.io.IOException;
// import java.io.InputStream;
// import java.util.ArrayList;
// import java.util.Arrays;
// import java.util.List;
//
// import javax.imageio.ImageIO;
//
// import edu.ncsu.dlf.model.PdfComment.Tag;
//
// import org.apache.pdfbox.io.IOUtils;
// import org.junit.Test;
//
// import test.TestUtils;
//
// public  class TestPdfCommentExtraction {
//
//     @Test
//     public void testBlankPDF() throws Exception {
//         Pdf pdf = loadCopyOfPdf("/blank.pdf");
//
//         try {
//             assertEquals(0, pdf.getPDFComments().size());
//         } finally {
//             pdf.close();
//         }
//     }
//
//     @Test
//     public void testExoticAnnotations() throws Exception {
//         Pdf pdf = loadCopyOfPdf("/strangeAnnotations.pdf");
//         String comparisonImageDir = "strangeAnnotations";
//         try {
//             List<PdfComment> pdfComments = pdf.getPDFComments();
//
//             assertEquals(7, pdfComments.size());
//             PDFCommentMaker expectedCommentsMaker = new PDFCommentMaker()
//             .fillComments("delete this", "Good!", "Typically standard", "Could be section 3", "[blank]", "delete this", "[blank]")
//             .fillImages(loadExpectedImages(comparisonImageDir, 7))
//             .fillTitlesWithDefault();
//
//             List<ExpectedPdfComment> expectedComments = expectedCommentsMaker.asList();
//
//             expectedComments.get(4).imageDifferenceThreshold = 2.9;
//             compare(expectedComments, pdfComments, comparisonImageDir);
//         } finally {
//             pdf.close();
//         }
//
//     }
//
//     @Test
//     public void testHighlightedPDF() throws Exception {
//         Pdf pdf = loadCopyOfPdf("/highlights.pdf");
//
//         try {
//             List<PdfComment> pdfComments = pdf.getPDFComments();
//
//             assertEquals(5, pdfComments.size());
//             String[] expectedComments = new String[] { "Look at this Introduction", "Bad short words", "Paragraph",
//                     "Multi-column", "[blank]" };
//             BufferedImage[] expectedImages = loadExpectedImages("highlights", 5);
//
//             compare(pdfComments, expectedComments, expectedImages, "highlights");
//         } finally {
//             pdf.close();
//         }
//
//     }
//
//     @Test
//     public void testPopupPDF() throws Exception {
//         Pdf pdf = loadCopyOfPdf("/popups.pdf");
//
//         try {
//             List<PdfComment> pdfComments = pdf.getPDFComments();
//             assertEquals(4, pdfComments.size());
//             String[] expectedComments = new String[] { "Edge one", "Edge two", "Middle of Page", "Corner" };
//             BufferedImage[] expectedImages = loadExpectedImages("popups", 4);
//
//             compare(pdfComments, expectedComments, expectedImages, "popups");
//         } finally {
//             pdf.close();
//         }
//
//     }
//
//     @Test
//     public void testDrawingPdf() throws Exception {
//         Pdf pdf = loadCopyOfPdf("/drawings.pdf");
//
//         try {
//             List<PdfComment> pdfComments = pdf.getPDFComments();
//             assertEquals(4, pdfComments.size());
//             String[] expectedComments = new String[] { "[blank]", "Fix Please", "Was designificated", "[blank]" };
//             BufferedImage[] expectedImages = loadExpectedImages("drawings", 4);
//
//             compare(pdfComments, expectedComments, expectedImages, "drawings");
//         } finally {
//             pdf.close();
//         }
//
//     }
//
//     @Test
//     public void testCommentsOfVariousIntensities() throws Exception {
//         Pdf pdf = loadCopyOfPdf("/intenseFixes.pdf");
//
//         try {
//             List<PdfComment> pdfComments = pdf.getPDFComments();
//             assertEquals(7, pdfComments.size());
//             String comparisonImageDir = "intenseFixes";
//
//             PDFCommentMaker expectedCommentsMaker = new PDFCommentMaker()
//             .fillComments("[blank]", "Fix Please", "You might consider fixing this", "[blank]", "[blank]", "Fix Please", "You might consider fixing this")
//             .fillImages(loadExpectedImages(comparisonImageDir, 7))
//             .fillTitlesWithDefault();
//             expectedCommentsMaker.setTags(1, PdfComment.Tag.MUST_FIX);
//             expectedCommentsMaker.setTags(2, PdfComment.Tag.CONSIDER_FIX);
//             expectedCommentsMaker.setTags(5, PdfComment.Tag.MUST_FIX);
//             expectedCommentsMaker.setTags(6, PdfComment.Tag.CONSIDER_FIX);
//
//             List<ExpectedPdfComment> expectedComments = expectedCommentsMaker.asList();
//             compare(expectedComments, pdfComments, comparisonImageDir);
//             // check the colors created by updating the pdf
//             updateCommentsWithColorsAndLinks(pdf, pdfComments);
//             //TODO check colors or at least the updated tags.  I tried rendering the page, but
//             //PDFBox won't reflect the new colors.  It has something to do with the fact that I don't
//             //set an appearance stream in PDF.makeNewAnnotation, but that's complex and maybe a lot of effort for
//             // little gain, as SumatraPDF and Reader can handle what is currently done.
//         } finally {
//             pdf.close();
//         }
//     }
//
//
//
//     private void updateCommentsWithColorsAndLinks(Pdf pdf, List<PdfComment> pdfComments) {
//         int issueNumber = 1;
//         for (PdfComment comment: pdfComments) {
//             comment.setIssueNumber(issueNumber);
//             issueNumber++;
//         }
//         Repo repo = new Repo("test-owner", "test-repo");
//         pdf.updateCommentsWithColorsAndLinks(pdfComments, repo);
//     }
//
//
//     private Pdf loadCopyOfPdf(String pathToPdf) throws IOException {
//         InputStream fos = getClass().getResourceAsStream(pathToPdf);
//         assertNotNull(fos);
//         // this makes sure that any changes we make to the pdf are not persisted between tests
//         ByteArrayInputStream copyOfPDF = new ByteArrayInputStream(IOUtils.toByteArray(fos));
//         InputStream commentBoxStream = getClass().getResourceAsStream("/images/comment_box.PNG");
//         assertNotNull(commentBoxStream);
//         return new Pdf(copyOfPDF, commentBoxStream);
//     }
//
//     private BufferedImage[] loadExpectedImages(String resourceDir, int numImages) throws IOException {
//         ArrayList<BufferedImage> loadedImages = new ArrayList<>();
//         for(int picName = 1; picName<=numImages; picName++) {
//             BufferedImage loadedImage = ImageIO.read(getClass().getResourceAsStream("/images/"+resourceDir+"/"+picName+".png"));
//             assertNotNull(loadedImage);
//             loadedImages.add(loadedImage);
//         }
//         return loadedImages.toArray(new BufferedImage[loadedImages.size()]);
//     }
//
//     private void compare(List<PdfComment> pdfComments, String[] expectedComments, BufferedImage[] expectedImages, String foldername) {
//         for(int i =0;i<pdfComments.size();i++) {
//             PdfComment pdfComment = pdfComments.get(i);
//             assertEquals("Failed Comment Comparison for comment "+i, expectedComments[i], pdfComment.getComment());
//             double difference = TestUtils.imagePercentDiff(expectedImages[i], pdfComment.getImage());
//             String possibleErrorMessage = String.format("Failed image Comparison for "+foldername+"/%d.png : difference %1.4f", i+1, difference);
//             assertTrue(possibleErrorMessage, difference < 0.01);
//         }
//     }
//
//
//     private void compare(List<ExpectedPdfComment> expected, List<PdfComment> actual, String folderName) {
//         assertEquals(expected.size(), actual.size());
//         for (int i = 0; i < actual.size(); i++) {
//             PdfComment a = actual.get(i);
//             ExpectedPdfComment e = expected.get(i);
//             assertEquals("Failed Comment Comparison for comment " + i, e.expectedComment, a.getComment());
//             assertEquals("Failed Title Comparison for comment " + i, e.expectedTitle, a.getTitle());
//             double difference = TestUtils.imagePercentDiff(e.expectedImage, a.getImage());
//             String possibleErrorMessage = String.format("Failed image Comparison for " + folderName
//                     + "/%d.png : difference %1.4f", i + 1, difference);
//             assertTrue(possibleErrorMessage, difference < e.imageDifferenceThreshold);
//             assertEquals("Failed tag comparison for comment "+i, e.expectedTags, a.getTags());
//
//         }
//     }
//
//
//     private static class PDFCommentMaker {
//
//         private List<ExpectedPdfComment> builtComments = new ArrayList<>();
//
//         public PDFCommentMaker fillComments(String... expectedComments) {
//             for (int i = 0; i < expectedComments.length; i++) {
//                 String expectedComment = expectedComments[i];
//                 if (builtComments.size() <= i) {
//                     builtComments.add(new ExpectedPdfComment());
//                 }
//                 builtComments.get(i).expectedComment = expectedComment;
//             }
//             return this;
//         }
//
//         public List<ExpectedPdfComment> asList() {
//             validateComments();
//             return builtComments;
//         }
//
//         private void validateComments() {
//             for(ExpectedPdfComment e : builtComments) {
//                 assertNotNull(e.expectedComment);
//                 assertNotNull(e.expectedImage);
//                 assertNotNull(e.expectedTitle);
//             }
//         }
//
//         public void setTags(int index, Tag... expectedTags) {
//             builtComments.get(index).expectedTags = Arrays.asList(expectedTags);
//         }
//
//         public PDFCommentMaker fillTitlesWithDefault() {
//             for(ExpectedPdfComment e : builtComments) {
//                 e.expectedTitle = e.expectedComment;
//             }
//             return this;
//         }
//
//         public PDFCommentMaker fillImages(BufferedImage... expectedImages) {
//             for (int i = 0; i < expectedImages.length; i++) {
//                 BufferedImage expectedImage = expectedImages[i];
//                 if (builtComments.size() <= i) {
//                     builtComments.add(new ExpectedPdfComment());
//                 }
//                 builtComments.get(i).expectedImage = expectedImage;
//             }
//             return this;
//         }
//
//     }
//
//     private static class ExpectedPdfComment {
//
//         public List<Tag> expectedTags = new ArrayList<>();
//         public BufferedImage expectedImage;
//         public String expectedTitle;
//         public String expectedComment;
//         public double imageDifferenceThreshold = 0.01;
//
//     }
//
// }
