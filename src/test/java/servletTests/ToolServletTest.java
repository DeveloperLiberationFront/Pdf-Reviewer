package servletTests;

import static org.junit.Assert.*;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import edu.ncsu.dlf.servlet.ToolServlet;

/**
 * Tests the functionality of the ToolServlet
 * @author Nicholas Anthony
 */
public class ToolServletTest {

	 @Mock
	 HttpServletRequest request;
	 @Mock
	 HttpServletResponse response;
	 @Mock
	 HttpSession session;
	
	/**
	 * Initializes Mockito
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}
	 
	/**
	 * The one test
	 */
	@Test
	public void testServlet() {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		RequestDispatcher reqDis = Mockito.mock(RequestDispatcher.class);
		
		Mockito.when(request.getRequestDispatcher("tool.html")).thenReturn(reqDis);
		try {
			Mockito.doNothing().when(reqDis).forward(request, response);
		} catch (ServletException | IOException e1) {
			fail("Something bad shouldn't happen");
		}
		
		ToolServlet servlet = new ToolServlet();
		
		try {
			servlet.doGet(request, response);
		} catch (ServletException | IOException | NullPointerException e) {
			fail("Something bad shouldn't happen");
		}
		assertEquals(request.getHeader("accept"), null);
	}

}
