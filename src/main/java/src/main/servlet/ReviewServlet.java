package src.main.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;

import src.main.model.Pdf;
import src.main.model.PdfComment;

public class ReviewServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ServletFileUpload upload = new ServletFileUpload();
		
		String repoName = req.getParameter("repoName");
		String writerName = req.getParameter("writer");
		String accessToken = req.getParameter("access_token");
		
		if(repoName == null || writerName == null || accessToken == null) {
			resp.sendError(500);
			return;
		}
		
		try {
			FileItemIterator iter = upload.getItemIterator(req);
			FileItemStream file = iter.next();
			
			Pdf pdf = new Pdf(file.openStream());
			
			List<String> commentsStr = pdf.getComments();
			List<PdfComment> comments = PdfComment.getComments(commentsStr);
			
			pdf.close();
			
			GitHubClient client = new GitHubClient();
			client.setOAuth2Token(accessToken);
			
			createIssues(client, writerName, repoName, comments);
			closeReviewIssue(client, writerName, repoName);
			
		} catch(FileUploadException e) {
			resp.sendError(500);
		}
	}
	
	public void createIssues(GitHubClient client, String writerName, String repoName, List<PdfComment> comments) throws IOException {
		IssueService issueService = new IssueService(client);
		
		for(PdfComment comment : comments) {
			Issue issue = new Issue();
			issue.setTitle(comment.getTitle());
			issue.setBody(comment.getComment());
			
			List<Label> labels = new ArrayList<>();
			
			for(String tag : comment.getTags()) {
				Label label = new Label();
				label.setName(tag);
				labels.add(label);
			}
			
			issue.setLabels(labels);
			issueService.createIssue(writerName, repoName, issue);
			
		}
		
	}
	
	public void closeReviewIssue(GitHubClient client, String writerName, String repoName) throws IOException {
		IssueService issueService = new IssueService(client);
		RepositoryService repoService = new RepositoryService(client);
		Repository repo = repoService.getRepository(writerName, repoName);
		UserService userService = new UserService(client);
		User reviewer = userService.getUser();
		
		for(Issue issue : issueService.getIssues(repo, null)) {
			if(issue.getAssignee() != null) {
				System.out.println(issue.getState());
			
				if(issue.getTitle().startsWith("Reviewer - ") && issue.getAssignee().getLogin().equals(reviewer.getLogin())) {
					issue.setState("closed");
					issueService.editIssue(writerName, repoName, issue);
				}
			}
		}
	}
}
