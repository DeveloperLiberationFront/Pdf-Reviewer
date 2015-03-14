package edu.ncsu.dlf.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.ncsu.dlf.database.DBAbstraction;
import edu.ncsu.dlf.database.DatabaseFactory;
import edu.ncsu.dlf.model.Review;
import edu.ncsu.dlf.utils.JSONUtils;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.UserService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StatusServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		DBAbstraction database = DatabaseFactory.getDatabase();

		GitHubClient client = new GitHubClient();
		client.setOAuth2Token(req.getParameter("access_token"));
		
		UserService userService = new UserService(client);
		User user = userService.getUser();
			
		resp.setContentType("application/json");
		try {
			JSONObject json = new JSONObject();
			
			List<Review> reviewsWhereUserIsRequester = database.getReviewsWhereUserIsRequester(user);
			List<Review> reviewsWhereUserIsReviewer = database.getReviewsWhereUserIsReviewer(user);

			json.put("reviewsWhereUserIsReviewer", JSONUtils.toJSON(reviewsWhereUserIsReviewer));
			json.put("reviewsWhereUserIsRequester", JSONUtils.toJSON(reviewsWhereUserIsRequester));
			
			resp.getWriter().write(json.toString(2));
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("Trying to recover and send empty arrays");
			try {
                JSONObject json = new JSONObject();

                json.put("reviewsWhereUserIsReviewer", new JSONArray());
                json.put("reviewsWhereUserIsRequester", new JSONArray());

                resp.getWriter().write(json.toString(2));
            } catch (JSONException je) {
                je.printStackTrace();
                resp.sendError(500);
			}
		}
	}
}
