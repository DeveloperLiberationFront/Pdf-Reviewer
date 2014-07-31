package src.main.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ReviewServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		
		InputStream inst = getServletContext().getResourceAsStream("/index.html");
		BufferedReader in = new BufferedReader(new InputStreamReader(inst));
		
		StringBuilder builder = new StringBuilder();
		String line;
		while((line = in.readLine()) != null) {
			builder.append(line);
		}
		
		in.close();
		
		resp.setContentType("text/html");
		resp.getWriter().write(builder.toString());
	}
	
}
