package src.main.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CollaboratorService;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import src.main.HttpUtils;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public class ReviewRequestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			String body = HttpUtils.getRequestBody(req);
			
			JSONObject data = new JSONObject(body);
			JSONArray reviewersJson = data.getJSONArray("reviewers");
			String repoName = data.getString("repo");
			String paper = data.getString("paper");
			
			GitHubClient client = new GitHubClient();
			client.setOAuth2Token(req.getParameter("access_token"));
			
			UserService userService = new UserService(client);
			
			User writer = userService.getUser();
			
			List<User> reviewers = new ArrayList<>();
			
			
			for(int i=0; i<reviewersJson.length(); i++) {
				reviewers.add(userService.getUser(reviewersJson.getString(i)));
			}

			IssueService issueService = new IssueService(client);
			RepositoryService repoService = new RepositoryService(client);
			Repository repo = repoService.getRepository(writer.getLogin(), repoName);
			CollaboratorService collaboratorService = new CollaboratorService(client);
			
			for(User u : reviewers) {
				collaboratorService.addCollaborator(repo, u.getLogin());
				Issue issue = new Issue();
				issue.setTitle("Reviewer - " + u.getLogin());
				String link = "http://pdfreviewhub.appspot.com/?repoName=" + repoName + "&writer=" + writer.getLogin() + "&paper=" + paper;
				issue.setBody("@" + u.getLogin() + " has been requested to review this paper\n." +
							  "Click [here](" + link + ") to upload your review.");
				issue.setAssignee(u);
				issueService.createIssue(writer.getLogin(), repoName, issue);
				
				addReviewToDatastore(u.getLogin(), writer.getLogin(), repoName, paper, link);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	void addReviewToDatastore(String reviewer, String writer, String repo, String paper, String link) {
		Entity request = new Entity("request");
		request.setProperty("reviewer", reviewer);
		request.setProperty("writer", writer);
		request.setProperty("repo", repo);
		request.setProperty("paper", paper);
		request.setUnindexedProperty("link", link);
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		datastore.put(request);
	}
	
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		GitHubClient client = new GitHubClient();
		client.setOAuth2Token(req.getParameter("access_token"));
		String writer = req.getParameter("writer");
		String reviewer = req.getParameter("reviewer");
		String repo = req.getParameter("repo");
		
		String closeComment = "@" + reviewer + " is no longer reviewing this paper.";
		ReviewServlet.closeReviewIssue(client, writer, repo, reviewer, closeComment);
		
		removeReviewFromDatastore(reviewer, writer, repo);
	}
	
	static void removeReviewFromDatastore(String reviewer, String writer, String repo) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Query query = new Query("request");
		FilterPredicate filterReviewer = new FilterPredicate("reviewer", FilterOperator.EQUAL, reviewer);
		FilterPredicate filterWriter = new FilterPredicate("writer", FilterOperator.EQUAL, writer);
		FilterPredicate filterRepo = new FilterPredicate("repo", FilterOperator.EQUAL, repo);
		query.setFilter(CompositeFilterOperator.and(filterReviewer, filterWriter, filterRepo));
		PreparedQuery preparedQuery = datastore.prepare(query);
		for(Entity e : preparedQuery.asIterable()) {
			datastore.delete(e.getKey());
		}
	}
}
