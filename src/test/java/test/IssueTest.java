package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.Test;

import src.main.model.Issue;

public class IssueTest {
	
	Issue a;
	Issue b;
	Issue c;
	Issue d;
	Issue e;
	Issue f;
	Issue g;
	Issue h;
	
	@Before
	public void setup() {
		a = new Issue("{tag 1, mf} This is an issue");
		b = new Issue("{tag 1, mf}This is an issue");
		c = new Issue("This is an issue{tag 1, mf} ");
		d = new Issue("This is an issue {tag 1, mf}");
		e = new Issue("This is {tag 1, mf} an issue");
		f = new Issue("This is {tag 1, mf}an issue");
		g = new Issue("This is an issue");
		h = new Issue("This is not an issue");
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
		
		assertEquals("Must Fix", Issue.getTag("mf"));
		assertEquals("Should Fix", Issue.getTag("sf"));
		assertEquals("Consider Fixing", Issue.getTag("cf"));
		assertEquals("Arbitrary Tag", Issue.getTag("Arbitrary Tag"));
	}
}
