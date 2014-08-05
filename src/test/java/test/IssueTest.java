package test;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import src.main.model.PdfComment;

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
		aS = "{tag 1, mf} This is an issue";
		bS = "{tag 1, mf}This is an issue";
		cS = "This is an issue{tag 1, mf} ";
		dS = "This is an issue {tag 1, mf}";
		eS = "This is {tag 1, mf} an issue";
		fS = "This is {tag 1, mf}an issue";
		gS = "This is an issue";
		hS = "This is not an issue";
		iS = "{tag, p} This is a positive issue";
		
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
		assertEquals(a.getComment(), b.getComment());
		assertEquals(a.getComment(), c.getComment());
		assertEquals(a.getComment(), d.getComment());
		assertEquals(a.getComment(), e.getComment());
		assertEquals(a.getComment(), f.getComment());
		assertEquals(a.getComment(), g.getComment());
		assertFalse(a.getComment() == h.getComment());
	}
	
	@Test
	public void testTitle() {
		assertEquals(a.getTitle(), b.getTitle());
		assertEquals(a.getTitle(), c.getTitle());
		assertEquals(a.getTitle(), d.getTitle());
		assertEquals(a.getTitle(), e.getTitle());
		assertEquals(a.getTitle(), f.getTitle());
		assertEquals(a.getTitle(), g.getTitle());
	}
	
	@Test
	public void testTags() {
		assertEquals(2, a.getTags().size());
		
		assertEquals("Must Fix", PdfComment.getTag("mf"));
		assertEquals("Should Fix", PdfComment.getTag("sf"));
		assertEquals("Consider Fixing", PdfComment.getTag("cf"));
		assertEquals("Arbitrary Tag", PdfComment.getTag("Arbitrary Tag"));
	}
	
	@Test
	public void testPositive() {
		assertTrue(i.getTags().contains(PdfComment.POSITIVE));

		assertEquals("Positive", PdfComment.getTag("g"));
		assertEquals("Positive", PdfComment.getTag("p"));
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
		
		boolean contains = false;
		for(PdfComment com : allComments) {
			if(com.getTags().contains("Positive"))
				contains = true;
		}
		assertTrue(contains);
		
		List<PdfComment> negComments = PdfComment.getNegComments(commentStrL);
		
		for(PdfComment com : negComments) {
			assertFalse(com.getTags().contains("Positive"));
		}
		
	}
}
