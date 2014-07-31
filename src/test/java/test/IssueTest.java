package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

import src.main.model.PdfComment;

public class IssueTest {
	
	PdfComment a;
	PdfComment b;
	PdfComment c;
	PdfComment d;
	PdfComment e;
	PdfComment f;
	PdfComment g;
	PdfComment h;
	
	@Before
	public void setup() {
		a = new PdfComment("{tag 1, mf} This is an issue");
		b = new PdfComment("{tag 1, mf}This is an issue");
		c = new PdfComment("This is an issue{tag 1, mf} ");
		d = new PdfComment("This is an issue {tag 1, mf}");
		e = new PdfComment("This is {tag 1, mf} an issue");
		f = new PdfComment("This is {tag 1, mf}an issue");
		g = new PdfComment("This is an issue");
		h = new PdfComment("This is not an issue");
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
}
