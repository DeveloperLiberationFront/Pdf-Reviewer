 package modelTests;

 import static org.junit.Assert.*;

 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;

 import org.junit.Test;

 import edu.ncsu.dlf.model.Pdf;
 import edu.ncsu.dlf.model.PdfComment;
 import edu.ncsu.dlf.model.Repo;

 /**
  * Tests the functionality of the PDF class
  * @author Team 19
  */
 public class PdfTest {

 	@Test
 	public void testConstructor() throws IOException {
 		File initialFile = new File("src/test/resources/test-files/Test_Document_A1.pdf");
 		InputStream fileStream = null;
 		try {
 			fileStream = new FileInputStream(initialFile);
 		} catch (FileNotFoundException e) {
 			fail("We got a problem");
 		}
		
 		File imageFile = new File("src/test/resources/images/comment_box.PNG");
 		InputStream imageFileStream = null;
 		try {
 			imageFileStream = new FileInputStream(imageFile);
 		} catch (FileNotFoundException e) {
 			fail("We got a problem");
 		}
		
 		Pdf pdf = null;
 		try {
 			pdf = new Pdf(fileStream, imageFileStream);
 		} catch (IOException e) {
 			fail("IO exception");
 		}
		
 		assertFalse(pdf.getDocument() == null);
 		pdf.close();
 	}

 	/**
 	 * Tests getting a list of highlights comments from the file
 	 * @throws IOException 
 	 */
 	@Test
 	public void testGetHighlightComments() throws IOException {
 		File initialFile = new File("src/test/resources/test-files/Test_Document_A1.pdf");
 		InputStream fileStream = null;
 		try {
 			fileStream = new FileInputStream(initialFile);
 		} catch (FileNotFoundException e) {
 			fail("We got a problem");
 		}
		
 		File imageFile = new File("src/test/resources/images/comment_box.PNG");
 		InputStream imageFileStream = null;
 		try {
 			imageFileStream = new FileInputStream(imageFile);
 		} catch (FileNotFoundException e) {
 			fail("We got a problem");
 		}
		
 		Pdf pdf = null;
 		try {
 			pdf = new Pdf(fileStream, imageFileStream);
 		} catch (IOException e) {
 			fail("IO exception");
 		}
		
 		List<PdfComment> comments = pdf.getPDFComments();
 		assertTrue(comments.size() == 4);
 		assertTrue(comments.get(0).getTitle().equals("Please make this actually relevant to the subje..."));
 		pdf.close();
 	}
	
 	/**
 	 * Tests uploading blank comments
 	 * @throws IOException
 	 */
 	@Test
 	public void testBlankHighlights() throws IOException {
 		File initialFile = new File("src/test/resources/test-files/Test_Document_H1.pdf");
 		InputStream fileStream = null;
 		try {
 			fileStream = new FileInputStream(initialFile);
 		} catch (FileNotFoundException e) {
 			fail("We got a problem");
 		}
		
 		File imageFile = new File("src/test/resources/images/comment_box.PNG");
 		InputStream imageFileStream = null;
 		try {
 			imageFileStream = new FileInputStream(imageFile);
 		} catch (FileNotFoundException e) {
 			fail("We got a problem");
 		}
		
 		Pdf pdf = null;
 		try {
 			pdf = new Pdf(fileStream, imageFileStream);
 		} catch (IOException e) {
 			fail("IO exception");
 		}
		
 		List<PdfComment> comments = pdf.getPDFComments();
 		assertTrue(comments.get(0).toString().contains("[blank]"));

 		pdf.close();
 	}
	
 	/**
 	 * Tests comment generation on annotations
 	 * @throws IOException 
 	 */
 	@Test
 	public void testAnnotations() throws IOException {
 		File initialFile = new File("src/test/resources/test-files/Test_Document_L1.pdf");
 		InputStream fileStream = null;
 		try {
 			fileStream = new FileInputStream(initialFile);
 		} catch (FileNotFoundException e) {
 			fail("We got a problem");
 		}
		
 		File imageFile = new File("src/test/resources/images/comment_box.PNG");
 		InputStream imageFileStream = null;
 		try {
 			imageFileStream = new FileInputStream(imageFile);
 		} catch (FileNotFoundException e) {
 			fail("We got a problem");
 		}
		
 		Pdf pdf = null;
 		try {
 			pdf = new Pdf(fileStream, imageFileStream);
 		} catch (IOException e) {
 			fail("IO exception");
 		}
		
 		List<PdfComment> comments = pdf.getPDFComments();
 		assertTrue(comments.size() == 3);
 		assertTrue(comments.get(0).getTitle().equals("Is this some kind of joke?"));
 		assertTrue(comments.get(2).toString().contains("[blank]"));

 		pdf.close();
 	}
	
 	/**
 	 * Tests comment generation with tags
 	 * @throws IOException 
 	 */
 	@Test
 	public void testTags() throws IOException {
 		File initialFile = new File("src/test/resources/test-files/Test_Document_K1.pdf");
 		InputStream fileStream = null;
 		try {
 			fileStream = new FileInputStream(initialFile);
 		} catch (FileNotFoundException e) {
 			fail("We got a problem");
 		}
		
 		File imageFile = new File("src/test/resources/images/comment_box.PNG");
 		InputStream imageFileStream = null;
 		try {
 			imageFileStream = new FileInputStream(imageFile);
 		} catch (FileNotFoundException e) {
 			fail("We got a problem");
 		}
		
 		Pdf pdf = null;
 		try {
 			pdf = new Pdf(fileStream, imageFileStream);
 		} catch (IOException e) {
 			fail("IO exception");
 		}
		
 		List<PdfComment> comments = pdf.getPDFComments();
 		assertTrue(comments.size() == 4);
		
 		Repo repo = new Repo("Nicholas Anthony", "Testing");

		
 		pdf.updateCommentsWithColorsAndLinks(comments, repo);

 		assertTrue(comments.get(0).toString().contains("[MUST_FIX]"));
 		assertTrue(comments.get(1).toString().contains("[POSITIVE]"));
 		assertTrue(comments.get(2).toString().contains("[CONSIDER_FIX]"));
		
 		pdf.close();
 	}
 }
