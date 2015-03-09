package test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import edu.ncsu.dlf.model.PdfComment;
import edu.ncsu.dlf.model.PdfComment.Tag;

public class IssueTest {
	String aS;
	String bS;
	String cS;
	String dS;
	String eS;
	String fS;
	String gS;
	String hS;
	String iS;
	
	PdfComment a;
	PdfComment b;
	PdfComment c;
	PdfComment d;
	PdfComment e;
	PdfComment f;
	PdfComment g;
	PdfComment h;
	PdfComment i;
	
	@Before
	public void setup() {
		aS = "{{tag 1, mf}} [[https://github.com/mpeterson2/Pdf-Test/issues/1]] This is an issue";
		bS = "{{tag 1, mf}}[[https://github.com/mpeterson2/Pdf-Test/issues/2]]This is an issue";
		cS = "This is an [[https://github.com/mpeterson2/Pdf-Test/issues/3]]issue{{tag 1, mf}} ";
		dS = "This is an [[https://github.com/mpeterson2/Pdf-Test/issues/4]]issue {{tag 1, mf}}";
		eS = "This is [[https://github.com/mpeterson2/Pdf-Test/issues/5]]{{tag 1, mf}} an issue";
		fS = "This is [[https://github.com/mpeterson2/Pdf-Test/issues/6]]{{tag 1, mf}}an issue";
		gS = "This is  [[https://github.com/mpeterson2/Pdf-Test/issues/7]] an issue";
		hS = "This is not an [[https://github.com/mpeterson2/Pdf-Test/issues/19]] issue";
		iS = "{{tag, p}} This is a positive issue";
		
		a = new PdfComment(aS);
		b = new PdfComment(bS);
		c = new PdfComment(cS);
		d = new PdfComment(dS);
		e = new PdfComment(eS);
		f = new PdfComment(fS);
		g = new PdfComment(gS);
		h = new PdfComment(hS);
		i = new PdfComment(iS);
	}
	
	@Test
	public void testComment() {
		String aComment = a.getComment();
		assertEquals("This is an issue", aComment);
		assertEquals(aComment, b.getComment());
		assertEquals(aComment, c.getComment());
		assertEquals(aComment, d.getComment());
		assertEquals(aComment, e.getComment());
		assertEquals(aComment, f.getComment());
		assertEquals(aComment, g.getComment());
		assertNotEquals(aComment, h.getComment());
	}
	
	@Test
	public void testTitle() {
		String aTitle = a.getTitle();
		assertEquals("This is an issue", aTitle);
		assertEquals(aTitle, b.getTitle());
		assertEquals(aTitle, c.getTitle());
		assertEquals(aTitle, d.getTitle());
		assertEquals(aTitle, e.getTitle());
		assertEquals(aTitle, f.getTitle());
		assertEquals(aTitle, g.getTitle());
	}
	
	@Test
	public void testTags() {
		assertEquals(2, a.getTags().size());
		
		assertEquals(Tag.MUST_FIX, PdfComment.getTag("mf"));
		assertEquals(Tag.MUST_FIX, PdfComment.getTag("must-fix"));
		assertEquals(Tag.MUST_FIX, PdfComment.getTag("must fix"));
		assertEquals(Tag.MUST_FIX, PdfComment.getTag("mustfix"));
		assertEquals(Tag.MUST_FIX, PdfComment.getTag("mustFix"));
		assertEquals(Tag.SHOULD_FIX, PdfComment.getTag("sf"));
		assertEquals(Tag.SHOULD_FIX, PdfComment.getTag("should-fix"));
        assertEquals(Tag.SHOULD_FIX, PdfComment.getTag("should fix"));
        assertEquals(Tag.SHOULD_FIX, PdfComment.getTag("shouldfix"));
        assertEquals(Tag.SHOULD_FIX, PdfComment.getTag("shouldFix"));
		assertEquals(Tag.CONSIDER_FIX, PdfComment.getTag("cf"));
		assertEquals(Tag.CONSIDER_FIX, PdfComment.getTag("could-fix"));
		assertEquals(Tag.CONSIDER_FIX, PdfComment.getTag("could fix"));
		assertEquals(Tag.CONSIDER_FIX, PdfComment.getTag("couldFix"));
		assertEquals(Tag.CONSIDER_FIX, PdfComment.getTag("couldfix"));
		assertEquals(Tag.CUSTOM_TAG, PdfComment.getTag("Arbitrary Tag"));
	}
	
	@Test
	public void testPositive() {
		assertTrue(i.getTags().contains(PdfComment.Tag.POSITIVE));

		assertEquals(Tag.POSITIVE, PdfComment.getTag("g"));
		assertEquals(Tag.POSITIVE, PdfComment.getTag("good"));
		assertEquals(Tag.POSITIVE, PdfComment.getTag("p"));
		assertEquals(Tag.POSITIVE, PdfComment.getTag("positive"));
		assertEquals(Tag.POSITIVE, PdfComment.getTag("plus"));
		assertEquals(Tag.POSITIVE, PdfComment.getTag("pos"));
		assertEquals(Tag.POSITIVE, PdfComment.getTag("+"));
	}
	
	@Test
	public void testGettingFromString() {
		List<String> commentStrL = new ArrayList<>();
		commentStrL.add(aS);
		commentStrL.add(bS);
		commentStrL.add(cS);
		commentStrL.add(dS);
		commentStrL.add(eS);
		commentStrL.add(fS);
		commentStrL.add(gS);
		commentStrL.add(hS);
		commentStrL.add(iS);
		
		List<PdfComment> allComments = PdfComment.getComments(commentStrL);
		
		boolean containsPositive = false;
		for(PdfComment com : allComments) {
			if(com.getTags().contains(Tag.POSITIVE))
				containsPositive = true;
		}
		assertTrue(containsPositive);
		
		List<PdfComment> negComments = PdfComment.getNegComments(commentStrL);
		
		for(PdfComment com : negComments) {
			assertFalse(com.getTags().contains("Positive"));
		}
		
	}
	
	@Test
	public void testIssueNumber() {
		assertEquals(1, a.getIssueNumber());
		assertEquals(2, b.getIssueNumber());
		assertEquals(3, c.getIssueNumber());
		assertEquals(4, d.getIssueNumber());
		assertEquals(5, e.getIssueNumber());
		assertEquals(6, f.getIssueNumber());
		assertEquals(7, g.getIssueNumber());
		assertEquals(19, h.getIssueNumber());
		assertEquals(0, i.getIssueNumber());
	}
	
	@Test
	public void testContent() {
		assertEquals("{{CUSTOM_TAG, MUST_FIX}} [[https://github.com/mpeterson2/Pdf-Test/issues/1]] This is an issue", a.getMessageWithLink("mpeterson2", "Pdf-Test"));
		assertEquals("{{CUSTOM_TAG, MUST_FIX}} [[https://github.com/mpeterson2/Pdf-Test/issues/2]] This is an issue", b.getMessageWithLink("mpeterson2", "Pdf-Test"));
		assertEquals("{{CUSTOM_TAG, MUST_FIX}} [[https://github.com/mpeterson2/Pdf-Test/issues/3]] This is an issue", c.getMessageWithLink("mpeterson2", "Pdf-Test"));
		assertEquals("{{CUSTOM_TAG, MUST_FIX}} [[https://github.com/mpeterson2/Pdf-Test/issues/4]] This is an issue", d.getMessageWithLink("mpeterson2", "Pdf-Test"));
		assertEquals("{{CUSTOM_TAG, MUST_FIX}} [[https://github.com/mpeterson2/Pdf-Test/issues/5]] This is an issue", e.getMessageWithLink("mpeterson2", "Pdf-Test"));
		assertEquals("{{CUSTOM_TAG, MUST_FIX}} [[https://github.com/mpeterson2/Pdf-Test/issues/6]] This is an issue", f.getMessageWithLink("mpeterson2", "Pdf-Test"));
		assertEquals("[[https://github.com/mpeterson2/Pdf-Test/issues/7]] This is an issue", g.getMessageWithLink("mpeterson2", "Pdf-Test"));
		assertEquals("[[https://github.com/mpeterson2/Pdf-Test/issues/19]] This is not an issue", h.getMessageWithLink("mpeterson2", "Pdf-Test"));
		assertEquals("{{CUSTOM_TAG, POSITIVE}} This is a positive issue", i.getMessageWithLink("mpeterson2", "Pdf-Test"));
	}
}
