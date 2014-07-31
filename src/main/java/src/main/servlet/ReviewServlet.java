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

import src.main.HttpUtils;

public class ReviewServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			String body = HttpUtils.getRequestBody(req);
			
			JSONObject data = new JSONObject(body);
			System.out.println(data.toString(2));
			JSONArray reviewersJson = data.getJSONArray("reviewers");
			String repoUrl = data.getString("repo");
			
			System.out.println(repoUrl);
			System.out.println(reviewersJson.toString(2));
			
			UserService userService = new UserService();
			userService.getClient().setOAuth2Token(req.getParameter("access_token"));
			
			List<User> reviewers = new ArrayList<>();
			
			
			for(int i=0; i<reviewersJson.length(); i++) {
				reviewers.add(userService.getUser(reviewersJson.getString(i)));
			}
			
			for(User u : reviewers) {
				System.out.println(u.getLogin());
				// Tell the user they have a request to review a paper.
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
