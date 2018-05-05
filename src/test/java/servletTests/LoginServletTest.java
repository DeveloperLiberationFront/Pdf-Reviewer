package servletTests;

import static org.junit.Assert.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.junit.Test;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import edu.ncsu.dlf.servlet.LoginServlet;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests the functionality of the LoginServlet
 * @author Team 19
 */
public class LoginServletTest {

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
	 * Tests an invalid code
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	@Test
	public void invalidTest() throws FileNotFoundException, IOException {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		
		Mockito.when(request.getParameter("code")).thenReturn(null);
		Mockito.when(response.getWriter()).thenReturn(new PrintWriter("TestFilePleaseIgnore.txt"));
		
		LoginServlet servlet = new LoginServlet();
		try {
			servlet.doGet(request, response);
		} catch (ServletException | IOException | NullPointerException e) {
			fail("Something went wrong");
		}
		
		assertEquals(request.getHeader("accept"), null);
	}
	
	/**
	 * Tests a valid code
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	@Test
	public void validTest() throws FileNotFoundException, IOException {
		HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
		HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
		
		Mockito.when(request.getParameter("code")).thenReturn("12345");
		Mockito.when(request.getContextPath()).thenReturn("localhost:9090");
		Mockito.when(response.getWriter()).thenReturn(new PrintWriter("TestFilePleaseIgnore.txt"));
		
		LoginServlet servlet = new LoginServlet();
		try {
			servlet.doGet(request, response);
		} catch (ServletException | IOException | NullPointerException e) {
			fail("Something went wrong");
		}		
	}

}
