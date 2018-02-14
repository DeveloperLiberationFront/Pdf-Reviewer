package edu.ncsu.dlf.servlet;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.ncsu.dlf.utils.HttpUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String search = req.getParameter("search");
		String accessToken = req.getParameter("access_token");
		
		// Search for users...
		List<String> users = new ArrayList<>();
		
		try {
			HttpClient client = HttpClients.createDefault();
			URIBuilder builder = new URIBuilder("https://api.github.com/search/users");
			builder.addParameter("q", search);
			builder.addParameter("access_token", accessToken);
			HttpGet request = new HttpGet(builder.build());
			request.setHeader("accept", "application/json");
			
			HttpResponse response = client.execute(request);
			
			String body = HttpUtils.getResponseBody(response);
			JSONObject json = new JSONObject(body);
			JSONArray usersJ = json.getJSONArray("items");
			for(int i=0; i<usersJ.length(); i++) {
				JSONObject userJ = usersJ.getJSONObject(i);
				users.add(userJ.getString("login"));
			}
		} catch(URISyntaxException | JSONException e) {}
		
		
		JSONArray logins = new JSONArray();
		
		for(String user : users) {
			logins.put(user);
		}
		
		resp.setContentType("application/json");
		resp.getWriter().write(logins.toString());
	}
}
