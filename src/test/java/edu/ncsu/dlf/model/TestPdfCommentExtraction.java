package edu.ncsu.dlf.model;

import static org.junit.Assert.*;

import java.io.InputStream;

import org.junit.Test;

public  class TestPdfCommentExtraction {

    @Test
    public void testBlankPDF() throws Exception {
        InputStream fos = getClass().getResourceAsStream("/blank.pdf");
        InputStream commentBoxStream = getClass().getResourceAsStream("/images/comment_box.PNG");
        assertNotNull(fos);
        assertNotNull(commentBoxStream);
        Pdf pdf = new Pdf(fos, commentBoxStream);

        assertEquals(0, pdf.getPDFComments().size());
    }
    

}
