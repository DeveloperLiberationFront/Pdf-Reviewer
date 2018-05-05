package edu.ncsu.dlf.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryBranch;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * RepositoriesServlet is responsible for getting list of repositories and their branches
 */
public class RepositoriesServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		GitHubClient client = new GitHubClient();
		client.setOAuth2Token(req.getParameter("access_token"));

		TreeMap<Date, Repository> sortedRepos = new TreeMap<Date, Repository>();
		RepositoryService service = new RepositoryService(client);
		for (Repository repo : service.getRepositories()) {
			sortedRepos.put(repo.getUpdatedAt(), repo);
		}

		// Building the JSON response
		JSONArray reposJSON = new JSONArray();
		for(Map.Entry<Date, Repository> entry : sortedRepos.entrySet()) {

			JSONArray branchArray = new JSONArray();
			for(RepositoryBranch branch: service.getBranches(entry.getValue())){
				branchArray.put(branch.getName());
			}

			JSONObject repoJSON;
			try {
				repoJSON = new JSONObject().put("repoName", entry.getValue().getOwner().getLogin()  + "/" + entry.getValue().getName()).put("branches", branchArray);
				reposJSON.put(repoJSON);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		// System.out.println(reposJSON);
		resp.setContentType("application/json");
		resp.getWriter().write(reposJSON.toString());
	}

}
