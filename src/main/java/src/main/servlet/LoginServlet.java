package src.main.servlet;

import java.io.IOException;
import java.net.URISyntaxException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;

import src.main.HttpUtils;
import src.main.SecretKeys;


public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String CLIENT_ID = "b08a834d3b797794e83f"; //The GitHub "public key"

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			URIBuilder builder = new URIBuilder("https://github.com/login/oauth/access_token");
			builder.addParameter("client_id", CLIENT_ID);
			builder.addParameter("client_secret", SecretKeys.GitHub);
			builder.addParameter("code", req.getParameter("code"));
			
			HttpPost request = new HttpPost(builder.build());
			
			request.setHeader("accept", "application/json");
	
			
			HttpClient client = HttpClients.createDefault();
			HttpResponse response = client.execute(request);
			String body = HttpUtils.getResponseBody(response);
			resp.setContentType("application/json");
			resp.getWriter().write(body);
		} catch(URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
}
