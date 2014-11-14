package src.main.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.eclipse.egit.github.core.service.UserService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RepoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String auth = req.getParameter("access_token");
		String login = req.getParameter("login");
		
		GitHubClient client = new GitHubClient();
		client.setOAuth2Token(auth);
		RepositoryService repoService = new RepositoryService(client);
		UserService userService = new UserService(client);
		User owner = userService.getUser(login);

		List<Repository> repos;
		if("Organization".equals(owner.getType()))
			repos = repoService.getOrgRepositories(login);
		else
			repos = repoService.getRepositories();
		
		
		JSONArray reposJson = new JSONArray();
		
		for(Repository repo : repos) {
			JSONObject repoJson = new JSONObject();
			try {
				repoJson.put("name", repo.getName());
				repoJson.put("url", repo.getUrl());
				
				reposJson.put(repoJson);
			} catch(JSONException e) {
				e.printStackTrace();
			}
		}
		
		try {
			resp.setContentType("application/json");
			resp.getWriter().write(reposJson.toString(2));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
