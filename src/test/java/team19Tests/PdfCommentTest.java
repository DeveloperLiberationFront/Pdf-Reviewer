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
	public void longComment() {
		PdfComment comment = new PdfComment("This comment is really long and should "
				+ "probably have some sort of puncution with it but we're not going to bother "
				+ "with that because this is a test and we aren't the most thorough of folk.");
		assertEquals(comment.getTitle(), "This comment is really long and should probably...");
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
	public void brokenTag() {
		PdfComment comment = new PdfComment("What am I reading here? [[considerFix]]");
		assertEquals(comment.getTags().get(0).toString(), "CONSIDER_FIX");
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
		PdfComment comment = new PdfComment("Literally never seen something this horrible in my life {{mustfix}} [[/5]]");
		Repo repo = new Repo();
		repo.repoName = "master";
		repo.repoOwner = "Dikolai";
		String response = comment.getMessageWithLink(repo);
		
		assertTrue(response.contains("MUST_FIX"));
		assertTrue(response.contains("https://github.com/Dikolai/master/issues/5"));
	}
}
