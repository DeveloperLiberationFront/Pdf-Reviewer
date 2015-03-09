package edu.ncsu.dlf.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.UserService;
import org.json.JSONException;
import org.json.JSONObject;

public class AvatarServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		UserService userService = new UserService();
		userService.getClient().setOAuth2Token(req.getParameter("access_token"));
		User user = userService.getUser();
		
		String avatarUrl = user.getAvatarUrl();
		String login = user.getLogin();
		
		JSONObject json = new JSONObject();
		try {
			json.put("avatar", avatarUrl);
			json.put("login", login);
		} catch(JSONException e) {}
		
		resp.setContentType("application/json");
		resp.getWriter().write(json.toString());
	}
}
