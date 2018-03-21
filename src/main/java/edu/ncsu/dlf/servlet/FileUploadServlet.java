package edu.ncsu.dlf.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

import edu.ncsu.dlf.model.Pdf;
import edu.ncsu.dlf.model.PdfComment;

public class FileUploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    String dataurl = req.getParameter("dataurl");
    dataurl = dataurl.replace("data:application/pdf;base64,", "");

    byte[] data = Base64.decodeBase64(dataurl);
		InputStream fileStream = new ByteArrayInputStream(data);
		//FileUtils.writeByteArrayToFile(new File("test.pdf"), data);

		Pdf test = new Pdf(fileStream, getServletContext());
		List<PdfComment> comments = test.getPDFComments();

		for(PdfComment comment: comments) {
			System.out.println(comment.getComment());
		}
	}
}
