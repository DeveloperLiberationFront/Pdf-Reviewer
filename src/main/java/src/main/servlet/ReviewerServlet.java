package src.main.servlet;

import java.io.IOException;
import java.util.ArrayList;
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
import org.json.JSONException;
import org.json.JSONObject;

public class ReviewerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String login = req.getParameter("login");
		
		GitHubClient client = new GitHubClient();
		client.setOAuth2Token(req.getParameter("access_token"));
		
		UserService userService = new UserService(client);
		
		User user = userService.getUser(login);
		boolean isOrg = "Organization".equals(user.getType());
		
		List<User> reviewers = new ArrayList<>();
		reviewers.add(user);      //adds myself
		
		if(!isOrg) {
			List<User> followers = userService.getFollowers();
			List<User> following = userService.getFollowing();
			
			List<User> both = new ArrayList<>();
			both.addAll(followers);
			both.addAll(following);
			
			u1: for(User u : both) {
				for(User u2 : reviewers) {
					if(u2.getLogin().equals(u.getLogin())) {
						continue u1;
					}
				}
				
				reviewers.add(u);
			}
		}
		else {
			OrganizationService orgService = new OrganizationService(client);
			List<User> users = orgService.getMembers(user.getLogin());
			reviewers.addAll(users);
		}
		
		JSONArray json = new JSONArray();
		
		try {
			for(User u : reviewers) {
				u = userService.getUser(u.getLogin());
				JSONObject uJson = new JSONObject();
				uJson.put("login", u.getLogin());
				
				String name = u.getName();
				name = name == null? "" : name;
                if (user.getLogin().equals(u.getLogin())) {
				    uJson.put("name", name + " (Myself)");
				} else {
				    uJson.put("name", name);
				}
				
				
				uJson.put("email", u.getEmail());
				json.put(uJson);
			}
			
			resp.setContentType("application/json");
			resp.getWriter().write(json.toString(2));
		} catch(JSONException e) {}
	}
	
}
