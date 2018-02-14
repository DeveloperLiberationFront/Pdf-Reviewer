package edu.ncsu.dlf.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.OrganizationService;
import org.eclipse.egit.github.core.service.UserService;
import org.json.JSONArray;

public class RepoSourceServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		GitHubClient client = new GitHubClient();
		client.setOAuth2Token(req.getParameter("access_token"));
		
		UserService userService = new UserService(client);
		User user = userService.getUser();
		OrganizationService orgService = new OrganizationService(client);
		List<User> users = orgService.getOrganizations(user.getLogin());
		
		JSONArray logins = new JSONArray();
		logins.put(user.getLogin());
		for(User u : users) {
			logins.put(u.getLogin());
		}
		
		resp.setContentType("application/json");
		resp.getWriter().write(logins.toString());
	}

}
