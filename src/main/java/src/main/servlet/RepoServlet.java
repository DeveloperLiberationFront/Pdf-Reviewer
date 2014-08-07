package src.main.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class RepoServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String auth = req.getParameter("access_token");
		String login = req.getParameter("login");
		
		RepositoryService repoService = new RepositoryService();
		repoService.getClient().setOAuth2Token(auth);
		
		JSONArray reposJson = new JSONArray();
		
		for(Repository repo : repoService.getRepositories(login)) {
			JSONObject repoJson = new JSONObject();
			try {
				repoJson.put("name", repo.getName());
				repoJson.put("url", repo.getUrl());
				
				reposJson.put(repoJson);
			} catch(JSONException e) {}
		}
		
		try {
			resp.setContentType("application/json");
			resp.getWriter().write(reposJson.toString(2));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
