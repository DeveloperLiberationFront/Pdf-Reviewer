package edu.ncsu.dlf.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.ncsu.dlf.database.DBAbstraction;
import edu.ncsu.dlf.database.DatabaseFactory;
import edu.ncsu.dlf.model.Repo;
import edu.ncsu.dlf.model.Review;
import edu.ncsu.dlf.utils.HttpUtils;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.LabelService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ReviewRequestServlet extends HttpServlet {
	private static final String SERVICE_URL = "http://pdfreview-ncsudlf.rhcloud.com/";
    private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			String body = HttpUtils.getRequestBody(req);
			
			GitHubClient client = new GitHubClient();
            client.setOAuth2Token(req.getParameter("access_token"));
            UserService userService = new UserService(client);
            List<Review> newReviews = createNewReviews(body, userService);   
            
            IssueService issueService = new IssueService(client);
            RepositoryService repoService = new RepositoryService(client);
            CollaboratorService collaboratorService = new CollaboratorService(client);
            
            Review aReview = newReviews.get(0);     //all these reviews have the same requester and repo involved
            User reviewRequester = userService.getUser(aReview.requester.login);
            Repository gitRepo = repoService.getRepository(aReview.repo.repoOwner, aReview.repo.repoName);

			boolean isOrg = "Organization".equals(reviewRequester.getType());

			//addCommentsToPDF(pathToPaper, paper);
			JSONObject returnJson = new JSONObject();

			for(Review review : newReviews) {
				if(!isOrg) {
				    try {
				        collaboratorService.addCollaborator(gitRepo, review.reviewer.login);
                    } catch (Exception e) {
                        if (!returnJson.has("message"))
                            returnJson.put("message", "However, we could not add the reviewer(s) as collaborator(s).  "
                                + "This happens if you don't own the repo you are making a request for.");
                        System.out.println("Could not add collaborator, maybe you don't own the repo?");
                    }
				}
				try {
				    Label reviewRequestLabel = makeOrGetReviewRequestLabel(client, review.repo);
				    createReviewRequestIssue(reviewRequestLabel, review, issueService, userService);
					DBAbstraction database = DatabaseFactory.getDatabase();
		        
					database.addReviewToDatastore(review);
				} catch(IOException e) {
					resp.sendError(417);
					return;
				}
			}
			resp.setStatus(200);
            resp.setContentType("application/json");
            returnJson.write(resp.getWriter()); 
		} catch (Exception e) {
			e.printStackTrace();
			resp.setStatus(500);
		}
	}

    private Label makeOrGetReviewRequestLabel(GitHubClient client, Repo repo) {
        Label reviewRequestLabel = new Label().setColor("009800").setName("Review Request");
        
        try {
            LabelService labelService = new LabelService(client);
            Label label = labelService.getLabel(repo.repoOwner, repo.repoName, "Review Request");
            if (label == null) {
                labelService.createLabel(repo.repoOwner, repo.repoName, reviewRequestLabel);
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
        return reviewRequestLabel;
    }

    private String createReviewRequestIssue(Label reviewRequestLabel, Review review, IssueService issueService, UserService userService) throws IOException {
        Issue issue = new Issue();
        issue.setTitle("Reviewer - " + review.reviewer.login);
        List<Label> labels = new ArrayList<>();
        labels.add(reviewRequestLabel);
        issue.setLabels(labels);
        issue.setAssignee(userService.getUser(review.reviewer.login));
        
        String linkToRespondToReview = SERVICE_URL + "?repoName=" + review.repo.repoName + "&writer=" + review.repo.repoOwner + "&paper=" + review.pathToPaperInRepo;

        issue.setBody("@" + review.reviewer.login + " has been requested to review this paper by @"+review.requester.login+".\n" +
        			  "Click [here](" + review.downloadPaperLink + ") to download the paper\n" +
        			  "Click [here](" + linkToRespondToReview + ") to upload your review.");
        
        issueService.createIssue(review.repo.repoOwner, review.repo.repoName, issue);
        return linkToRespondToReview;
    }

    private List<Review> createNewReviews(String body, UserService userService) throws JSONException, IOException { 
        JSONObject data = new JSONObject(body);
        JSONArray reviewersJson = data.getJSONArray("reviewers");
        String repoOwner = data.getString("owner");
        String repoName = data.getString("repo");
        String pathToPaper = data.getString("pathToPaper");
        String paper = data.getString("paper");
        String requesterLogin = data.getString("login");
        String customLabels = data.getString("customLabels");
        
        List<String> parsedCustomLabels = parseCustomLabels(customLabels); //all reviews will have the same labels
        
        String pathToPaperInRepo = pathToPaper + "/" + paper;
        String downloadLink = "https://github.com/" + repoOwner + '/' + repoName + "/raw/master/" + pathToPaperInRepo;
        List<Review> newReviews = new ArrayList<>();
        
        for(int i=0; i<reviewersJson.length(); i++) {
            String reviewerLogin = reviewersJson.getString(i);
            Review newReview = new Review(requesterLogin, repoOwner, reviewerLogin, repoName, pathToPaperInRepo, downloadLink, userService);
            newReview.setCustomLabels(parsedCustomLabels);
            newReviews.add(newReview);
            
        }
        return newReviews;
    }

	private static List<String> parseCustomLabels(String customLabels) {
	    Pattern pattern = Pattern.compile("\\[.+?\\]");
	    Matcher m = pattern.matcher(customLabels);
	    List<String> retVal = new ArrayList<>();
	    while (m.find()) {
	        retVal.add(m.group());
	    }
        return retVal;
    }

    @SuppressWarnings("unused")
    private void addCommentsToPDF(String pathToPaper, String paper) {
        // TODO Auto-generated method stub
        // Should add a comment to the front of the pdf with a memo about tags
    }
	
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		// handle the DELETE request
	    GitHubClient client = new GitHubClient();
		client.setOAuth2Token(req.getParameter("access_token"));
		String writer = req.getParameter("writer");
		String reviewer = req.getParameter("reviewer");
		String repo = req.getParameter("repo");
		
		String closeComment = "@" + reviewer + " is no longer reviewing this paper.";
		ReviewSubmitServlet.closeReviewIssue(client, writer, repo, reviewer, closeComment);
		
		DBAbstraction database = DatabaseFactory.getDatabase();
		database.removeReviewFromDatastore(reviewer, writer, repo);
	}

}
