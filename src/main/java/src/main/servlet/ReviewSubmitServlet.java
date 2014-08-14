package src.main.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.ContentsService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import src.main.model.Pdf;
import src.main.model.PdfComment;

public class ReviewSubmitServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doPost(final HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		final ServletFileUpload upload = new ServletFileUpload();
		
		final String repoName = req.getParameter("repoName");
		final String writerLogin = req.getParameter("writer");
		final String accessToken = req.getParameter("access_token");
		
		if(repoName == null || writerLogin == null || accessToken == null) {
			resp.sendError(500);
			return;
		}

		SubmitTask task = new SubmitTask();
		String pdfUrl = "";
		
		try {
			FileItemIterator iter = upload.getItemIterator(req);
			FileItemStream file = iter.next();
			Pdf pdf = new Pdf(file.openStream());
			List<String> comments = pdf.getComments();
			
			GitHubClient client = new GitHubClient();
			client.setOAuth2Token(accessToken);
			UserService userService = new UserService(client);
			User reviewer = userService.getUser();
			
			updatePdf(comments, pdf, writerLogin, repoName, client);
			pdfUrl = addPdfToRepo(client, accessToken, writerLogin, repoName, pdf, reviewer);
			task.setter(comments, accessToken, writerLogin, repoName);
			
			pdf.close();
		} catch(FileUploadException e) {
			resp.sendError(500, "There has been an error uploading your Pdf.");
		}
		
		resp.getWriter().write(pdfUrl);
		
		Queue taskQueue = QueueFactory.getDefaultQueue();
		taskQueue.add(TaskOptions.Builder.withPayload(task));
	}
	
	public Issue createIssue(GitHubClient client, String writerLogin, String repoName, PdfComment comment) throws IOException {
		IssueService issueService = new IssueService(client);
		
		// If the issue does not already exist
		if(comment.getIssueNumber() == 0) { 
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
			issue = issueService.createIssue(writerLogin, repoName, issue);
			comment.setIssueNumber(issue.getNumber());
			return issue;
		}
		// If the issue already exists
		else {
			Issue issue = issueService.getIssue(writerLogin, repoName, comment.getIssueNumber());
			String issueText = comment.getComment();
			if(!issue.getBody().equals(issueText)) {
				issueService.createComment(writerLogin, repoName, comment.getIssueNumber(), issueText);
			}
			
			List<Label> existingLabels = issue.getLabels();
			List<Label> labels = new ArrayList<>();
			for(String tag : comment.getTags()) {
				Label l = new Label();
				l.setName(tag);
				labels.add(l);
			}
			
			boolean updateLabels = labels.size() != existingLabels.size();
			if(!updateLabels) {
				for(Label l1 : labels) {
					updateLabels = true;
					for(Label l2 : existingLabels) {
						if(l1.getName().equals(l2.getName())) {
							updateLabels = false;
							break;
						}
					}
					if(updateLabels)
						break;
				}
			}
			
			if(updateLabels) {
				issue.setLabels(labels);
				issueService.editIssue(writerLogin, repoName, issue);
			}
			
			return issue;
		}
	}
	
	public void createIssues(GitHubClient client, String writerLogin, String repoName, List<PdfComment> comments) throws IOException {
		for(PdfComment comment : comments) {
			createIssue(client, writerLogin, repoName, comment);
		}
	}
	
	public void updatePdf(List<String> comments, Pdf pdf, String login, String repo, GitHubClient client) throws IOException {
		
		if(!comments.isEmpty()) {
			List<PdfComment> pdfComments = PdfComment.getComments(comments);
			
			// Upload first issue
			Issue firstIssue = createIssue(client, login, repo, pdfComments.get(0));
			
			// Set the issue numbers
			int issueNumber = firstIssue.getNumber();
			for(PdfComment com : pdfComments) {
				if(com.getIssueNumber() == 0) {
					System.out.println(com.getIssueNumber());
					com.setIssueNumber(issueNumber++);
				}
			}
			
			// Update the comments
			pdf.setComments(pdfComments, login, repo);
		}
	}
	
	static public void closeReviewIssue(GitHubClient client, String writerLogin, String repoName, String reviewer, String comment) throws IOException {
		IssueService issueService = new IssueService(client);
		
		for(Issue issue : issueService.getIssues(writerLogin, repoName, null)) {
			if(issue.getAssignee() != null) {
			
				if(issue.getTitle().startsWith("Reviewer - ") && issue.getAssignee().getLogin().equals(reviewer)) {
					issueService.createComment(writerLogin, repoName, issue.getNumber(), comment);
					issue.setState("closed");
					issueService.editIssue(writerLogin, repoName, issue);
				}
			}
		}
	}
	
	public String addPdfToRepo(GitHubClient client, String accessToken, String writerLogin, String repoName, Pdf pdf, User reviewer) throws IOException {
		String filePath = "";
		
		try {
			
			filePath = "reviews/" + reviewer.getLogin() + ".pdf";
			String sha = null;
			
			ContentsService contents = new ContentsService(client);
			RepositoryService repoService = new RepositoryService(client);
			Repository repo = repoService.getRepository(writerLogin, repoName);
			try {
				List<RepositoryContents> files = contents.getContents(repo, filePath);
				if(!files.isEmpty()) {
					sha = files.get(0).getSha();
				}
			} catch(IOException e) {}
		
		
			URIBuilder builder = new URIBuilder("https://api.github.com/repos/" + writerLogin + "/" + repoName + "/contents/" + filePath);
			builder.addParameter("access_token", accessToken);
			
			HttpPut request = new HttpPut(builder.build());
			
			try {
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				pdf.getDoc().save(output);
				
				String content = DatatypeConverter.printBase64Binary(output.toByteArray());
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				md.update(content.getBytes(StandardCharsets.US_ASCII));
						
				JSONObject json = new JSONObject();
				json.put("message", reviewer.getLogin() + " has submitted their review.");
				json.put("path", filePath);
				json.put("content", content);
				json.put("sha", sha);
				
				StringEntity entity = new StringEntity(json.toString());
				entity.setContentType("application/json");
				request.setEntity(entity);
				
				HttpClient httpClient = HttpClients.createDefault();
				httpClient.execute(request);
				
			} catch(JSONException | NoSuchAlgorithmException e) {
				e.printStackTrace();
			} finally {
				request.releaseConnection();
			}
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		return filePath;
	}
	
	private final class SubmitTask implements DeferredTask {
		private static final long serialVersionUID = -603761725725342674L;
		private String accessToken;
		private List<String> commentStrs;
		private String writerLogin;
		private String repoName;
		
		public void setter(List<String> comments, String accessToken, String writerLogin, String repoName) {
			this.commentStrs = comments;
			this.accessToken = accessToken;
			this.writerLogin = writerLogin;
			this.repoName = repoName;
		}

		@Override
		public void run() {
				try {
					GitHubClient client = new GitHubClient();
					client.setOAuth2Token(accessToken);
					
					UserService userService = new UserService(client);
					User reviewer = userService.getUser();
					
					List<PdfComment> comments = PdfComment.getComments(commentStrs);
					
					createIssues(client, writerLogin, repoName, comments);
					
					String closeComment = "@" + reviewer.getLogin() + " has reviewed this paper.";
					closeReviewIssue(client, writerLogin, repoName, reviewer.getLogin(), closeComment);
					ReviewRequestServlet.removeReviewFromDatastore(reviewer.getLogin(), writerLogin, repoName);
					
				} catch(IOException e) {
					System.err.println("Error processing Pdf.");
				}
		}
	}
}
