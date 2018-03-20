package team19Tests;

import static org.junit.Assert.*;

import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.ServletContext;

import org.junit.Test;

import edu.ncsu.dlf.model.Pdf;
import edu.ncsu.dlf.servlet.ReviewSubmitServlet;

public class PdfTest {

	@Test
	public void test1() {
		ReviewSubmitServlet rss = new ReviewSubmitServlet();
		try {
			FileInputStream fos = new FileInputStream("test.pdf");
		    Pdf pdf = new Pdf(fos, null);
			fail("This should cause a problem");
		} catch (Exception e) {
			assertTrue(true);
		}
	}

}
