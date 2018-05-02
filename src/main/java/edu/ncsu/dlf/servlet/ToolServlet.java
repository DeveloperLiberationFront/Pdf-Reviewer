package edu.ncsu.dlf.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Renders the tool.html page at the /tool route
 */
public class ToolServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/**
 	* Renders the tool.html page on the view
 	*/
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		req.getRequestDispatcher("tool.html").forward(req, resp);
	}

}
