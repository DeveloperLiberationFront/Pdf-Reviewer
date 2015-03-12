package edu.ncsu.dlf.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.ncsu.dlf.JSONUtils;
import edu.ncsu.dlf.database.DBAbstraction;
import edu.ncsu.dlf.database.DatabaseFactory;
import edu.ncsu.dlf.model.Review;

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
			
			List<Review> reviewsWhereUserIsRequester = database.getReviewsWhereUserIsRequester(user, userService);
			List<Review> reviewsWhereUserIsReviewer = database.getReviewsWhereUserIsReviewer(user, userService);

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
	
//	private JSONArray getPendingReviews(User user, boolean isWriter, DatastoreService datastore, UserService userService) throws JSONException, IOException {
//		String login = user.getLogin();
//
//		String key = isWriter ? "reviewer" : "requester";
//		
//		Query query = new Query("request");
//		FilterPredicate filter = new FilterPredicate(key, FilterOperator.EQUAL, login);
//		query.setFilter(filter);
//		PreparedQuery preparedQuery = datastore.prepare(query);
//		
//		JSONArray requests = new JSONArray();
//
//		for(Entity result : preparedQuery.asIterable()) {
//			String requesterLogin = (String) result.getProperty("requester");
//			String writerLogin = (String) result.getProperty("writer");
//			String reviewerLogin = (String) result.getProperty("reviewer");
//			String repo = (String) result.getProperty("repo");
//			String paper = (String) result.getProperty("paper");
//			String link = (String) result.getProperty("link");
//			
//			User requester = userService.getUser(requesterLogin);
//			JSONObject requesterJson = userToJson(requester);
//			
//			User writer = userService.getUser(writerLogin);
//			JSONObject writerJson = userToJson(writer);
//			
//			User reviewer = userService.getUser(reviewerLogin);
//			JSONObject reviewerJSON = userToJson(reviewer);
//
//			JSONObject request = new JSONObject();
//			request.put("requester", requesterJson);
//			request.put("writer", writerJson);
//			request.put("reviewer", reviewerJSON);
//			request.put("repo", repo);
//			request.put("paper", paper);
//			request.put("link", link);
//			
//			requests.put(request);
//		}
//		
//		return requests;
//	}
//	
//	JSONObject userToJson(User user) throws JSONException {
//		JSONObject json = new JSONObject();
//		json.put("login", user.getLogin());
//		json.put("email", user.getEmail());
//		json.put("name", user.getName());
//
//		return json;
//	}
}
