package src.main.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
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

import src.main.model.Pdf;
import src.main.model.PdfComment;

public class ReviewServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		ServletFileUpload upload = new ServletFileUpload();
		
		String repoName = req.getParameter("repoName");
		String writerLogin = req.getParameter("writer");
		String accessToken = req.getParameter("access_token");
		
		if(repoName == null || writerLogin == null || accessToken == null) {
			resp.sendError(500);
			return;
		}
		
		try {
			FileItemIterator iter = upload.getItemIterator(req);
			FileItemStream file = iter.next();
			
			Pdf pdf = new Pdf(file.openStream());
			
			List<String> commentsStr = pdf.getComments();
			List<PdfComment> comments = PdfComment.getComments(commentsStr);	
			
			GitHubClient client = new GitHubClient();
			client.setOAuth2Token(accessToken);
			
			UserService userService = new UserService(client);
			User reviewer = userService.getUser();
			
			createIssues(client, writerLogin, repoName, comments);
			String pdfPath = addPdfToRepo(client, accessToken, writerLogin, repoName, pdf, reviewer);
			closeReviewIssue(client, writerLogin, repoName, reviewer.getLogin());
			ReviewRequestServlet.removeReviewFromDatastore(reviewer.getLogin(), writerLogin, repoName);

			pdf.close();
			resp.getWriter().write(pdfPath);
		} catch(FileUploadException e) {
			resp.sendError(500);
		}
	}
	
	public void createIssues(GitHubClient client, String writerLogin, String repoName, List<PdfComment> comments) throws IOException {
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
			issueService.createIssue(writerLogin, repoName, issue);
			
		}
		
	}
	
	static public void closeReviewIssue(GitHubClient client, String writerLogin, String repoName, String reviewer) throws IOException {
		IssueService issueService = new IssueService(client);
		
		for(Issue issue : issueService.getIssues(writerLogin, repoName, null)) {
			if(issue.getAssignee() != null) {
				System.out.println(issue.getState());
			
				if(issue.getTitle().startsWith("Reviewer - ") && issue.getAssignee().getLogin().equals(reviewer)) {
					issue.setState("closed");
					issueService.editIssue(writerLogin, repoName, issue);
				}
			}
		}
	}
	
	public String addPdfToRepo(GitHubClient client, String accessToken, String writerLogin, String repoName, Pdf pdf, User reviewer) throws IOException {
		String filePath = "";
		
		try {
			List<String> existingPaths = getReviewContents(client, writerLogin, repoName, reviewer);
	
			int num = 1;
			
			for(String path : existingPaths) {
				if(path.startsWith(reviewer.getLogin())) {
					num++;
				}
			}
			
			filePath = "reviews/" + reviewer.getLogin() + "-" + num + ".pdf";
		
		
			URIBuilder builder = new URIBuilder("https://api.github.com/repos/" + writerLogin + "/" + repoName + "/contents/" + filePath);
			builder.addParameter("access_token", accessToken);
			
			HttpPut request = new HttpPut(builder.build());
			
			try {
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				pdf.getDoc().save(output);
				
				String content = DatatypeConverter.printBase64Binary(output.toByteArray());
				MessageDigest md = MessageDigest.getInstance("SHA-256");
				md.update(content.getBytes("US-ASCII"));
				
				String sha = md.digest().toString(); 
						
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
				
			}
			
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		return filePath;
	}
	
	public List<String> getReviewContents(GitHubClient client, String writerName, String repoName, User reviewer) throws IOException {
		RepositoryService repoService = new RepositoryService(client);
		Repository repo = repoService.getRepository(writerName, repoName);
		
		ContentsService contentsService = new ContentsService(client);
		List<RepositoryContents> repoContents = null;
		try {
			repoContents = contentsService.getContents(repo, "/reviews");
		} catch(IOException e) {
			return new ArrayList<String>();
		}
		
		List<String> repoPaths = new ArrayList<>();
		
		for(RepositoryContents content : repoContents) {
			repoPaths.add(content.getName());
		}
		
		return repoPaths;
	}
}
