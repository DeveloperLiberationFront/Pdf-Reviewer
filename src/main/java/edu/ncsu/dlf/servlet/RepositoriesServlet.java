package edu.ncsu.dlf.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.json.JSONArray;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class RepositoriesServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		GitHubClient client = new GitHubClient();
		client.setOAuth2Token(req.getParameter("access_token"));

		TreeMap<Date, String> sortedRepos = new TreeMap<Date, String>();
		RepositoryService service = new RepositoryService(client);
		for (Repository repo : service.getRepositories()) {
			sortedRepos.put(repo.getUpdatedAt(), repo.getName());
		}

		JSONArray reposJSON = new JSONArray();
		for(Map.Entry<Date, String> entry : sortedRepos.entrySet()) {
			// Date key = entry.getKey();
			// String value = entry.getValue();
			// System.out.println("key is: "+ key + " & Value is: " + value);
			
			reposJSON.put(entry.getValue());
		}
		// System.out.println(reposJSON);
		resp.setContentType("application/json");
		resp.getWriter().write(reposJSON.toString());
	}

}
