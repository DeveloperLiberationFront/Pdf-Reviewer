package edu.ncsu.dlf.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;

import java.io.InputStream;
import java.io.ByteArrayInputStream;

import edu.ncsu.dlf.model.Pdf;
import edu.ncsu.dlf.model.PdfComment;
import edu.ncsu.dlf.model.PdfComment.Tag;

public class FileUploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final String pathToCommentBoxImage = "/images/comment_box.PNG";

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String dataurl = req.getParameter("dataurl");
		dataurl = dataurl.replace("data:application/pdf;base64,", "");

		byte[] data = Base64.decodeBase64(dataurl);
		InputStream fileStream = new ByteArrayInputStream(data);
		//FileUtils.writeByteArrayToFile(new File("test.pdf"), data);

		InputStream commentBoxImageStream = getServletContext().getResourceAsStream(pathToCommentBoxImage);
		
		Pdf test = new Pdf(fileStream, commentBoxImageStream);
		List<PdfComment> comments = test.getPDFComments();

		GitHubClient client = new GitHubClient();
		client.setOAuth2Token(req.getParameter("access_token"));
		
		for(PdfComment comment: comments) {
			System.out.println(comment.toString());
			
			IssueService issueService = new IssueService(client);
			
			// Setting title
			Issue issue = new Issue();
			issue.setTitle(comment.getTitle());

			// Setting body
			String body = comment.getComment();
			issue.setBody(body);
			
			// Setting lables
            List<Label> newLabels = new ArrayList<>();
            for(Tag tag : comment.getTags()) {
            	Label label = new Label();
            	label.setName(tag.name());
            	newLabels.add(label);
			}
			issue.setLabels(newLabels);

			// Create the issue
            issue = issueService.createIssue("pdf-reviewer-bot", "creating-issues", issue);
			
			System.out.println("created issue #" + issue.getNumber());
		}
	}
}
