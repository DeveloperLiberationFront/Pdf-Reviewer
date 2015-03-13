package edu.ncsu.dlf.servlet;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import edu.ncsu.dlf.utils.HttpUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONException;
import org.json.JSONObject;


public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if (req.getParameter("code") == null) {
		    resp.setContentType("application/json");
		    JSONObject jobj = new JSONObject();
		    try {
		        jobj.put("client_id", System.getenv("GITHUB_ID"));
	            jobj.write(resp.getWriter()); 
		    } catch (JSONException e) {
		        e.printStackTrace();
		        resp.sendError(500);
		    }
		    return;
		}
	    
	    
	    HttpPost request = null;
		try {
			URIBuilder builder = new URIBuilder("https://github.com/login/oauth/access_token");
			builder.addParameter("client_id", System.getenv("GITHUB_ID"));
			builder.addParameter("client_secret", System.getenv("GITHUB_API"));
			builder.addParameter("code", req.getParameter("code"));
			
			request = new HttpPost(builder.build());
			
			request.setHeader("accept", "application/json");
	
			
			HttpClient client = HttpClients.createDefault();
			HttpResponse response = client.execute(request);
			String body = HttpUtils.getResponseBody(response);
			resp.setContentType("application/json");
			resp.getWriter().write(body);
		} catch(URISyntaxException e) {
			e.printStackTrace();
		} finally{
			if (request != null) {
				request.releaseConnection();
			}
		}
	}
	
}
