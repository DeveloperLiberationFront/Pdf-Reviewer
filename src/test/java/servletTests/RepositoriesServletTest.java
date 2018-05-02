package servletTests;

import static org.junit.Assert.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import edu.ncsu.dlf.servlet.RepositoriesServlet;

/**
 * Attempts to test the RepositoriesServlet class
 * @author Team 19
 */
public class RepositoriesServletTest {

	 @Mock
	 HttpServletRequest request;
	 @Mock
	 HttpServletResponse response;
	 @Mock
	 HttpSession session;
	
	 //https://www.tutorialspoint.com/mockito/mockito_environment.htm
	 	
	/**
	 * Initializes Mockito
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}
	
	/**
	 * Attempts to test the servlet
	 */
	@Test
	public void testServlet() {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		
		Mockito.when(request.getParameter("access_token")).thenReturn(null);
		
		RepositoriesServlet servlet = new RepositoriesServlet();
		
		try {
			servlet.doGet(request, response);
			fail("There should be an error");
		} catch (Exception e) {
			
		}
		
	}

}
