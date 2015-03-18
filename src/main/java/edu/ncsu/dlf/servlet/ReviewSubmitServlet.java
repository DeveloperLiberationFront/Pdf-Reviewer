package edu.ncsu.dlf.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import edu.ncsu.dlf.database.DBAbstraction;
import edu.ncsu.dlf.database.DatabaseFactory;
import edu.ncsu.dlf.model.Pdf;
import edu.ncsu.dlf.model.PdfComment;
import edu.ncsu.dlf.model.PdfComment.Tag;
import edu.ncsu.dlf.model.Repo;
import edu.ncsu.dlf.model.Review;
import edu.ncsu.dlf.utils.ImageUtils;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryContents;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.ContentsService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;
import org.json.JSONException;
import org.json.JSONObject;

public class ReviewSubmitServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
    @Override
    protected void doPost(final HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        final ServletFileUpload upload = new ServletFileUpload();

        Repo repo = new Repo(req.getParameter("writer"), req.getParameter("repoName"));
        String accessToken = req.getParameter("access_token");

        if (repo.repoOwner == null || repo.repoName == null || accessToken == null) {
            System.out.println("Something blank");
            resp.sendError(500);
            return;
        }
        
        GitHubClient client = new GitHubClient();
        client.setOAuth2Token(accessToken);
        UserService userService = new UserService(client);
        User reviewer = userService.getUser();
        
        Review fulfilledReview = DatabaseFactory.getDatabase().findReview(reviewer.getLogin(), repo);
        if (fulfilledReview == null) {
            //either they already uploaded the pdf or it doesn't exist
            resp.sendError(409);  //409 = conflict
            return;
        }

        UploadIssuesRunnable task = new UploadIssuesRunnable();
        String urlToPdfInRepo = "";
        Pdf pdf = null;
        try {
            FileItemIterator iter = upload.getItemIterator(req);
            FileItemStream file = iter.next();
            pdf = new Pdf(file.openStream(), getServletContext());
            
            int totalIssues = getNumTotalIssues(client, repo);      //TODO perhaps involve database to avoid race conditions

            List<PdfComment> comments = updatePdfWithNumberedAndColoredAnnotations(pdf, repo, totalIssues);
            urlToPdfInRepo = addPdfToRepo(pdf, reviewer, client, repo, accessToken);
            task.setter(comments, accessToken, repo, totalIssues, fulfilledReview.customLabels);
            
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(500, "There has been an error uploading your Pdf.");
            return;
        } finally {
            if (pdf != null) {
                pdf.close();
            }
        }

        resp.getWriter().write(urlToPdfInRepo);
        Thread thread = new Thread(task);
        thread.start();
    }
	
	private List<PdfComment> updatePdfWithNumberedAndColoredAnnotations(Pdf pdf, Repo repo, int totalIssues) throws IOException {
	    List<PdfComment> pdfComments = pdf.getPDFComments();
		if(!pdfComments.isEmpty()) {
			// Set the issue numbers
			int issueNumber = totalIssues + 1;
			for(PdfComment com : pdfComments) {
				if(com.getIssueNumber() == 0) {
					com.setIssueNumber(issueNumber++);
				}
			}
			
			// Update the comments to link to the repository and their newly assigned issue number
			pdf.updateComments(pdfComments, repo);
		}
		return pdfComments;
	}
	
	private int getNumTotalIssues(GitHubClient client, Repo repo) throws IOException {
	    IssueService issueService = new IssueService(client);

	    Map<String, String> prefs = new HashMap<String, String>();
	    //By default, only open issues are shown
	    prefs.put(IssueService.FILTER_STATE, "all");
	    //get all issues for this repo
	    List<Issue> issues = issueService.getIssues(repo.repoOwner, repo.repoName, prefs);
	    
        return issues.size();
    }

    static void closeReviewIssue(GitHubClient client, Repo repo, String reviewer, String comment) throws IOException {
		IssueService issueService = new IssueService(client);
		
		for(Issue issue : issueService.getIssues(repo.repoOwner, repo.repoName, null)) {
			if(issue.getAssignee() != null) {
			
				if(issue.getTitle().startsWith("Reviewer - ") && issue.getAssignee().getLogin().equals(reviewer)) {
					issueService.createComment(repo.repoOwner, repo.repoName, issue.getNumber(), comment);
					issue.setState("closed");
					issueService.editIssue(repo.repoOwner, repo.repoName, issue);
				}
			}
		}
	}
	
    private String addPdfToRepo(Pdf pdf, User reviewer, GitHubClient client, Repo repo, String accessToken) throws IOException {
        String filePath = "reviews/" + reviewer.getLogin() + ".pdf";
        String sha = null;
        ContentsService contents = new ContentsService(client);
        try {
            //list all the files in reviews.  We can't just fetch our paper, because it might be 
            //bigger than 1MB which breaks this API call
            List<RepositoryContents> files = contents.getContents(getRepo(client, repo), "reviews/");
            for(RepositoryContents file: files) {
                if (file.getName().equals(reviewer.getLogin()+".pdf")) {
                    sha = file.getSha();
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }

        HttpPut request = new HttpPut(buildURIForFileUpload(accessToken, repo.repoOwner, repo.repoName, filePath));	
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

            HttpClients.createDefault().execute(request);
        } catch(JSONException | COSVisitorException e) {
            e.printStackTrace();
        } finally {
            request.releaseConnection();
        }

        return filePath;
    }

    private Repository getRepo(GitHubClient client, Repo repo) throws IOException {
        RepositoryService repoService = new RepositoryService(client);
        return repoService.getRepository(repo.repoOwner, repo.repoName);
    }

    private URI buildURIForFileUpload(String accessToken, String writerLogin, String repoName, String filePath) throws IOException {
        try {
            URIBuilder builder = new URIBuilder("https://api.github.com/repos/" + writerLogin + '/' + repoName + "/contents/" + filePath);
            builder.addParameter("access_token", accessToken);

            return builder.build();
        } catch (URISyntaxException e) {
            throw new IOException("Could not build uri", e);
        }
        
    }

    private final static class UploadIssuesRunnable implements Runnable {
		private String accessToken;
		private List<PdfComment> comments;
        private int issueCount;
        private Repo repo;
        private List<String> customLabelStrings;
		
		public void setter(List<PdfComment> comments, String accessToken, Repo repo, int issueCount, List<String> customLabels) {
			this.comments = comments;
			this.accessToken = accessToken;
			this.repo = repo;
			this.issueCount = issueCount;
			this.customLabelStrings = customLabels;
		}

		@Override
		public void run() {
				try {
					GitHubClient client = new GitHubClient();
					client.setOAuth2Token(accessToken);
					
					DBAbstraction database = DatabaseFactory.getDatabase();
					UserService userService = new UserService(client);
					User reviewer = userService.getUser();
					List<Label> customLabels = createCustomLabels(client);
					
					createIssues(client, customLabels);
					
					String closeComment = "@" + reviewer.getLogin() + " has reviewed this paper.";
					
                    closeReviewIssue(client, repo, reviewer.getLogin(), closeComment);
					
					database.removeReviewFromDatastore(reviewer.getLogin(), repo);
					
				} catch(IOException e) {
				    e.printStackTrace();
					System.err.println("Error processing Pdf.");
				}
		}

        private List<Label> createCustomLabels(GitHubClient client) {
            List<Label> labels = new ArrayList<>();
            LabelService labelService = new LabelService(client);
            for (String customLabel : customLabelStrings) {
                Label newLabel = new Label().setColor(randomColor()).setName(customLabel);
                try {
                    Label alreadyExistingLabel = labelService.getLabel(repo.repoOwner, repo.repoName, customLabel);
                    labels.add(alreadyExistingLabel);
                } catch(IOException e) {
                    System.out.println("new label " +customLabel + " not found.  Going to create it");
                    try {
                        newLabel = labelService.createLabel(repo.repoOwner, repo.repoName, newLabel);
                    } catch (IOException e1) {
                        e1.printStackTrace();   //this is a bigger problem
                    }
                    labels.add(newLabel);
                }
            }
            return labels;
        }

        private String randomColor() {
            StringBuilder sb = new StringBuilder(6);
            Random r = new Random();
            for(int i = 0; i< 6; i++) {
                sb.append("0123456789abcdef".charAt(r.nextInt(16)));
            }
            return sb.toString();
        }

        public void createIssues(GitHubClient client, List<Label> customLabels) throws IOException {
        	for(PdfComment comment : comments) {
        	    System.out.println(comment);
        		createOrUpdateIssue(client, repo, comment, customLabels);
        	}
        }

        public void createOrUpdateIssue(GitHubClient client, Repo repo, PdfComment comment, List<Label> customLabels) throws IOException {
        	IssueService issueService = new IssueService(client);
        	
        	// If the issue does not already exist
        	if(comment.getIssueNumber() > issueCount) { 
        		createIssue(repo, comment, issueService, customLabels);
        	}
        	// If the issue already exists, update it
        	else {
        		updateIssue(repo, comment, issueService);
        	}
        }

        private void createIssue(Repo repo, PdfComment comment, IssueService issueService, List<Label> customLabels) throws IOException {
            Issue issue = new Issue();
            issue.setTitle(comment.getTitle());
            
            String body = comment.getComment();
            try {
                String imageURL = ImageUtils.uploadPhoto(comment.getImage());
                body = String.format("![snippet](%s)%n%n%s", imageURL, body);
            } catch (IOException e) {
                e.printStackTrace();
                // could not upload image, but carry on anyway
            }
            
            issue.setBody(body); 
            List<Label> newLabels = new ArrayList<>(customLabels);
            //add tags to labels
            for(Tag tag : comment.getTags()) {
            	Label label = new Label();
            	label.setName(tag.name());     // these tags are, by default, the normal grey color.  
            	newLabels.add(label);             // User can change these to the severity whey want
            }
            
            
            issue.setLabels(newLabels);
            //creates an issue remotely
            issue = issueService.createIssue(repo.repoOwner, repo.repoName, issue);
            comment.setIssueNumber(issue.getNumber());
        }

        private void updateIssue(Repo repo, PdfComment comment, IssueService issueService)
                throws IOException {
            System.out.println("Looking for "+repo.repoOwner+'/'+repo.repoName);
            Issue issue = issueService.getIssue(repo.repoOwner, repo.repoName, comment.getIssueNumber());
            String issueText = comment.getComment();
            if(!issue.getBody().equals(issueText)) {
                // makes a comment if the text has changed
            	issueService.createComment(repo.repoOwner, repo.repoName, comment.getIssueNumber(), issueText);
            }
            
            List<Label> existingLabels = issue.getLabels();
            List<Label> labels = new ArrayList<>();
            for(Tag tag : comment.getTags()) {
            	Label l = new Label();
            	l.setName(tag.name());
            	labels.add(l);
            }
            
            boolean shouldUpdateLabels = labels.size() != existingLabels.size();
            if(!shouldUpdateLabels) {
            	for(Label l1 : labels) {
            		shouldUpdateLabels = true;
            		for(Label l2 : existingLabels) {
            			if(l1.getName().equals(l2.getName())) {
            				shouldUpdateLabels = false;
            				break;
            			}
            		}
            		if(shouldUpdateLabels)
            			break;
            	}
            }
            
            if(shouldUpdateLabels) {
            	issue.setLabels(labels);
            	issueService.editIssue(repo.repoOwner, repo.repoName, issue);
            }
        }
	}
}
