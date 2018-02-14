package edu.ncsu.dlf.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.UserService;
import org.json.JSONException;
import org.json.JSONObject;

public class UserServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		GitHubClient client = new GitHubClient();
		client.setOAuth2Token(req.getParameter("access_token"));
		UserService userService = new UserService(client);
		
		String login = req.getParameter("user");
		
		User user = userService.getUser(login);
		
		JSONObject userJ = new JSONObject();

		try {
			userJ.put("login", user.getLogin());
			userJ.put("email", user.getEmail());
			userJ.put("name", user.getName());
			
			resp.setContentType("application/json");
			resp.getWriter().write(userJ.toString());
		} catch(JSONException e) {
			e.printStackTrace();
		}
	}
}
