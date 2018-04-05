package team19Tests;

import static org.junit.Assert.*;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import edu.ncsu.dlf.model.PdfComment;
import edu.ncsu.dlf.model.Repo;

/**
 * Tests the functionality of the PdfComment object
 * @author Nicholas Anthony
 */
public class PdfCommentTest {

	/**
	 * Tests JSON object creation
	 * @throws JSONException 
	 */
	@Test
	public void testJSON() throws JSONException {
		PdfComment comment = new PdfComment("Fix this flagrant error");
		JSONObject obj1 = comment.toJSON();
		
		BufferedImage bi = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
		comment.setImage(bi);
		
		assertEquals(obj1.get("comment"), "Fix this flagrant error");
		assertEquals(obj1.get("issueNumber"), 0);
		
		assertEquals(comment.toString(), "PdfComment [tags=[], comment=Fix this flagrant error, issueNumber=0, image=100x100px]");
		assertEquals(comment.getImage(), bi);
		assertEquals(comment.getTitle(), "Fix this flagrant error");
	}

	/**
	 * Tests the creation of long comments
	 */
	@Test
	public void longComments() {
		PdfComment comment1 = new PdfComment("This comment is really long and should "
				+ "probably have some sort of puncution with it but we're not going to bother "
				+ "with that because this is a test and we aren't the most thorough of folk.");
		assertEquals(comment1.getTitle(), "This comment is really long and should probably...");
		
		PdfComment comment2 = new PdfComment("This comment is also very long but this one is different"
				+ "because it'll have both a tag and an issue number [[/4]] {{cf}}");
		assertEquals(comment2.getIssueNumber(), 4);
		assertEquals(comment2.getTitle(), "This comment is also very long but this one is...");
	}
	
	/**
	 * Tests the creation of tags
	 */
	@Test
	public void testTags() {
		PdfComment comment = new PdfComment("{{mf}} This is the worst thing I've ever seen.");
		assertEquals(comment.getTags().get(0).toString(), "MUST_FIX");
	}
	
	/**
	 * Tests the creation of positive tags
	 */
	@Test
	public void positiveTags() {
		List<String> comments = new ArrayList<String>();
		comments.add("This is a {{g}} intro");
		comments.add("sp");
		comments.add("This is the worst thing I've ever read {{fix}}");
		comments.add("Nice to see this {{ +}}");
		List<PdfComment> pdfs = PdfComment.getNegComments(comments);
		
		assertEquals(pdfs.get(0).getTitle(), "sp");
		assertEquals(pdfs.get(1).getTitle(), "This is the worst thing I've ever read");
	}
	
	/**
	 * Tests making a comment with an incorrect tag
	 */
	@Test
	public void brokenTags() {
		PdfComment comment1 = new PdfComment("What am I reading here? [[considerFix]]");
		assertEquals(comment1.getTags().get(0).toString(), "CONSIDER_FIX");
		
		PdfComment comment2 = new PdfComment("Here is a comment with a broken tag }}");
		assertEquals(comment2.getTags().size(), 0);
		
		PdfComment comment3 = new PdfComment("And another! {{ Hello");
		assertEquals(comment3.getTags().size(), 0);
		
		PdfComment comment4 = new PdfComment("This one opens [[ but never closes");
		assertEquals(comment4.getTags().size(), 0);
		
		PdfComment comment5 = new PdfComment("This one ]] closes but never opens!");
		assertEquals(comment5.getTags().size(), 0);
	}
	
	/**
	 * Tests altering the issue numbers
	 */
	@Test
	public void issueNumber() {
		PdfComment comment = new PdfComment("I'm not sure how issue numbers work yet [[/1]]");
		assertEquals(comment.getIssueNumber(), 1);
		comment.setIssueNumber(2);
		assertEquals(comment.getIssueNumber(), 2);
	}
	
	/**
	 * Tests the functions using Repo objects
	 */
	@Test
	public void testRepo() {
		PdfComment comment1 = new PdfComment("Literally never seen something this horrible in my life {{mustfix}} [[/5]]");

		//TODO: The repo name being master makes it confusing (probably should be renamed)
		Repo repo = new Repo("Dikolai", "master");
		String response = comment1.getMessageWithLink(repo);
		
		assertTrue(response.contains("MUST_FIX"));
		assertTrue(response.contains("https://github.com/Dikolai/master/issues/5"));
		
		PdfComment comment2 = new PdfComment("This one has no issues. You are wonderful");
		response = comment2.getMessageWithLink(repo);
		
		assertEquals(response, "This one has no issues. You are wonderful");
	}
}
