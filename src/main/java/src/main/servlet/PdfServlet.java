package src.main.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONArray;
import org.json.JSONException;

import src.main.model.Issue;
import src.main.model.Pdf;

public class PdfServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {		
		ServletFileUpload upload = new ServletFileUpload();
		
		String repoUrl = req.getParameter("repoUrl");
		System.out.println(repoUrl);
		
		try {
			FileItemIterator iter = upload.getItemIterator(req);
			FileItemStream file = iter.next();
			Pdf pdf = new Pdf(file.openStream());
			List<String> comments = pdf.getComments();
			List<Issue> issues = Issue.getIssues(comments);
			
			pdf.close();
			
			JSONArray issuesJson = new JSONArray();
			
			for(Issue issue : issues) {
				issuesJson.put(issue.toJson());
			}
			
			resp.getWriter().write(issuesJson.toString(2));
			
		} catch(IOException | FileUploadException | JSONException e){
			resp.sendError(500);
		}
	}
}
