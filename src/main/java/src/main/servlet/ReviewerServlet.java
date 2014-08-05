package src.main.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.service.UserService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ReviewerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		UserService userService = new UserService();
		userService.getClient().setOAuth2Token(req.getParameter("access_token"));
		
		List<User> followers = userService.getFollowers();
		List<User> following = userService.getFollowing();
		
		List<User> both = new ArrayList<>();
		both.addAll(followers);
		both.addAll(following);
		
		List<User> reviewers = new ArrayList<>();
		
		u1: for(User u : both) {
			for(User u2 : reviewers) {
				if(u2.getLogin().equals(u.getLogin())) {
					continue u1;
				}
			}
			
			reviewers.add(u);
		}
		
		JSONArray json = new JSONArray();
		
		try {
			for(User u : reviewers) {
				u = userService.getUser(u.getLogin());
				JSONObject uJson = new JSONObject();
				uJson.put("login", u.getLogin());
				uJson.put("name", u.getName());
				uJson.put("email", u.getEmail());
				json.put(uJson);
			}
			
			resp.setContentType("application/json");
			resp.getWriter().write(json.toString(2));
		} catch(JSONException e) {}
	}
	
}
