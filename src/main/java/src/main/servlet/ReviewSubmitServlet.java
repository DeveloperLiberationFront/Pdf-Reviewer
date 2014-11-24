package src.main.servlet;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

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
import src.main.model.PdfComment.Tag;

public class ReviewSubmitServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private transient HttpClient httpClient = HttpClients.createDefault();

    private String repoName;

    private String writerLogin;

    private String accessToken;

    private transient GitHubClient client;
	
	@Override
	protected void doPost(final HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		final ServletFileUpload upload = new ServletFileUpload();
		
		this.repoName = req.getParameter("repoName");
		this.writerLogin = req.getParameter("writer");
		this.accessToken = req.getParameter("access_token");
		
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
			
			
			
			this.client = new GitHubClient();
			client.setOAuth2Token(accessToken);
			UserService userService = new UserService(client);
			User reviewer = userService.getUser();
			
			
			 List<String> comments = updatePdf(pdf);
			pdfUrl = addPdfToRepo(pdf, reviewer);
			task.setter(comments, accessToken, writerLogin, repoName);
			
			pdf.close();
		} catch(FileUploadException e) {
			resp.sendError(500, "There has been an error uploading your Pdf.");
		}
		
		resp.getWriter().write(pdfUrl);
		
		Queue taskQueue = QueueFactory.getDefaultQueue();
		taskQueue.add(TaskOptions.Builder.withPayload(task));
	}
	
	public void createIssue(GitHubClient client, String writerLogin, String repoName, PdfComment comment) throws IOException {
		IssueService issueService = new IssueService(client);
		
		// If the issue does not already exist
		if(comment.getIssueNumber() == 0) { 
			Issue issue = new Issue();
			issue.setTitle(comment.getTitle());
			issue.setBody(comment.getComment());
			
			List<Label> labels = new ArrayList<>();
			
			for(Tag tag : comment.getTags()) {
				Label label = new Label();
				label.setName(tag.name());
				labels.add(label);
			}
			
			issue.setLabels(labels);
			//creates an issue remotely
			issue = issueService.createIssue(writerLogin, repoName, issue);
			comment.setIssueNumber(issue.getNumber());
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
			for(Tag tag : comment.getTags()) {
				Label l = new Label();
				l.setName(tag.name());
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
		}
	}
	
	public void createIssues(GitHubClient client, String writerLogin, String repoName, List<PdfComment> comments) throws IOException {
		for(PdfComment comment : comments) {
			createIssue(client, writerLogin, repoName, comment);
		}
	}
	
	public List<String> updatePdf(Pdf pdf) throws IOException {
	    List<String> comments = pdf.getComments();
		if(!comments.isEmpty()) {
			List<PdfComment> pdfComments = PdfComment.getComments(comments);
			
			// Set the issue numbers
			int issueNumber = getNumTotalIssues() + 1;
			for(PdfComment com : pdfComments) {
				if(com.getIssueNumber() == 0) {
					System.out.println(com.getIssueNumber());
					com.setIssueNumber(issueNumber++);
				}
			}
			
			// Update the comments
			pdf.updateComments(pdfComments, this.writerLogin, this.repoName);
		}
		return comments;
	}
	
	@SuppressWarnings("unused")
    private static void main(String[] args) throws IOException {
        Pdf pdf = new Pdf(new FileInputStream("C:\\Users\\KevinLubick\\Downloads\\test_anno.pdf"));
        ReviewSubmitServlet servlet = new ReviewSubmitServlet();
        List<String> comments = pdf.getComments();
        List<PdfComment> pdfComments = PdfComment.getComments(comments);

        // Set the issue numbers
        int issueNumber = 1;
        for (PdfComment com : pdfComments) {
            if (com.getIssueNumber() == 0) {
                System.out.println(com.getIssueNumber());
                com.setIssueNumber(issueNumber++);
            }
        }

        // Update the comments
        pdf.updateComments(pdfComments, servlet.writerLogin, servlet.repoName);

        pdf.getDoc().save("C:\\Users\\KevinLubick\\Downloads\\test_anno_1.pdf");
        
        pdf.close();
    }
	
	private int getNumTotalIssues() throws IOException {
	    IssueService issueService = new IssueService(client);

	    Map<String, String> prefs = new HashMap<String, String>();
	    //By default, only open issues are shown
	    prefs.put(IssueService.FILTER_STATE, "all");
	    //get all issus for this repo
	    List<Issue> issues = issueService.getIssues(getRepo(client), prefs);
	    
        return issues.size();
    }

    public static void closeReviewIssue(GitHubClient client, String writerLogin, String repoName, String reviewer, String comment) throws IOException {
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
	
    public String addPdfToRepo(Pdf pdf, User reviewer) throws IOException {
        String filePath = "reviews/" + reviewer.getLogin() + ".pdf";
        String sha = null;
        
        ContentsService contents = new ContentsService(client);
        Repository repo = getRepo(client);
        try {
            //list all the files in reviews.  We can't just fetch our paper, because it might be 
            //bigger than 1MB which breaks this API call
            List<RepositoryContents> files = contents.getContents(repo, "reviews/");
            for(RepositoryContents file: files) {
                if (file.getName().equals(reviewer.getLogin()+".pdf")) {
                    sha = file.getSha();
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }

        HttpPut request = new HttpPut(buildURIForFileUpload(accessToken, writerLogin, repoName, filePath));	
        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            pdf.getDoc().save(output);

            String content = DatatypeConverter.printBase64Binary(output.toByteArray());

            JSONObject json = new JSONObject();
            if (sha == null) {
                //if we are uploading the review for the first time
                json.put("message", reviewer.getLogin() + " has submitted their review.");
            } else {
                //updating review
                json.put("message", reviewer.getLogin() + " has updated their review.");
                json.put("sha", sha);
            }

            json.put("path", filePath);
            json.put("content", content);


            StringEntity entity = new StringEntity(json.toString());
            entity.setContentType("application/json");
            request.setEntity(entity);

            httpClient.execute(request);
        } catch(JSONException e) {
            e.printStackTrace();
        } finally {
            request.releaseConnection();
        }

        return filePath;
    }

    private Repository getRepo(GitHubClient client) throws IOException {
        RepositoryService repoService = new RepositoryService(client);
        Repository repo = repoService.getRepository(writerLogin, repoName);
        return repo;
    }
	
	private void readObject(ObjectInputStream inputStream) throws IOException, ClassNotFoundException
    {
        inputStream.defaultReadObject();
        //reinstantiate our transient variables.
        this.httpClient = HttpClients.createDefault();
        this.client = new GitHubClient();
        client.setOAuth2Token(accessToken);
    }

    private URI buildURIForFileUpload(String accessToken, String writerLogin, String repoName, String filePath) throws IOException {
        try {
            URIBuilder builder = new URIBuilder("https://api.github.com/repos/" + writerLogin + "/" + repoName + "/contents/" + filePath);
            builder.addParameter("access_token", accessToken);

            URI build = builder.build();
            return build;
        } catch (URISyntaxException e) {
            throw new IOException("Could not build uri", e);
        }
        
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
