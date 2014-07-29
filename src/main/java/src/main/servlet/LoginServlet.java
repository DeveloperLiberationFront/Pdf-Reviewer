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


public class LoginServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String CLIENT_ID = "afa90e71a06d85c5fcb5";
	private static final String CLIENT_SECRET = "36f1f7ba3f89b45df77776b1454ceb1d8a513289";

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			URIBuilder builder = new URIBuilder("https://github.com/login/oauth/access_token");
			builder.addParameter("client_id", CLIENT_ID);
			builder.addParameter("client_secret", CLIENT_SECRET);
			builder.addParameter("code", req.getParameter("code"));
			
			HttpPost request = new HttpPost(builder.build());
			
			request.setHeader("accept", "application/json");
	
			
			HttpClient client = HttpClients.createDefault();
			HttpResponse response = client.execute(request);
			String body = HttpUtils.getResponseBody(response);
			System.out.println(body);		
		} catch(URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
}
