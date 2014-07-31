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

public class ReviewRequestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			String body = HttpUtils.getRequestBody(req);
			
			JSONObject data = new JSONObject(body);
			System.out.println(data.toString(2));
			JSONArray reviewersJson = data.getJSONArray("reviewers");
			String repoName = data.getString("repo");
			
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
				issue.setBody("@" + u.getLogin() + " has been requested to review this paper\n." +
							  "Click [here](http://pdfreviewhub.appspot.com/?repoName=" + repoName + "&writer=" + writer.getLogin() + ") to upload your review.");
				issue.setAssignee(u);
				issueService.createIssue(writer.getLogin(), repoName, issue);
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
