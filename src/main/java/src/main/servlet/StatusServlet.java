package src.main.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.UserService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public class StatusServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		GitHubClient client = new GitHubClient();
		client.setOAuth2Token(req.getParameter("access_token"));
		
		UserService userService = new UserService(client);
		User user = userService.getUser();
			
		try {
			JSONObject json = new JSONObject();
			json.put("requests", getPendingReviews(user, true, datastore, userService));
			json.put("reviews", getPendingReviews(user, false, datastore, userService));
			
			resp.setContentType("application/json");
			resp.getWriter().write(json.toString(2));
		} catch(JSONException e) {}
	}
	
	private JSONArray getPendingReviews(User user, boolean isWriter, DatastoreService datastore, UserService userService) throws JSONException, IOException {
		String login = user.getLogin();

		String key = isWriter ? "reviewer" : "requester";
		
		Query query = new Query("request");
		FilterPredicate filter = new FilterPredicate(key, FilterOperator.EQUAL, login);
		query.setFilter(filter);
		PreparedQuery preparedQuery = datastore.prepare(query);
		
		JSONArray requests = new JSONArray();

		for(Entity result : preparedQuery.asIterable()) {
			String writerLogin = (String) result.getProperty("writer");
			String reviewerLogin = (String) result.getProperty("reviewer");
			String repo = (String) result.getProperty("repo");
			String paper = (String) result.getProperty("paper");
			String link = (String) result.getProperty("link");
			
			User writer = userService.getUser(writerLogin);
			JSONObject writerJson = new JSONObject();
			writerJson.put("login", writer.getLogin());
			writerJson.put("email", writer.getEmail());
			writerJson.put("name", writer.getName());
			
			User reviewer = userService.getUser(reviewerLogin);
			JSONObject reviewerJSON = new JSONObject();
			reviewerJSON.put("login", reviewer.getLogin());
			reviewerJSON.put("email", reviewer.getEmail());
			reviewerJSON.put("name", reviewer.getName());

			JSONObject request = new JSONObject();
			request.put("writer", writerJson);
			request.put("reviewer", reviewerJSON);
			request.put("repo", repo);
			request.put("paper", paper);
			request.put("link", link);
			
			requests.put(request);
		}
		
		return requests;
	}
}
