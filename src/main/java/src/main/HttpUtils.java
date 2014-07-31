package src.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpResponse;

public class HttpUtils {
	public static String getResponseBody(HttpResponse response) throws IOException, UnsupportedEncodingException
	{
		StringBuilder sb = new StringBuilder();
		InputStream ips  = response.getEntity().getContent();
		try(BufferedReader buf = new BufferedReader(new InputStreamReader(ips,"UTF-8"));)
		{
		    String s;
			while(true )
		    {
		        s = buf.readLine();
		        if(s==null || s.length()==0)
		            break;
		        sb.append(s);
	
		    }
		}
		return sb.toString();
	}
	
	public static String getRequestBody(HttpServletRequest request) {
    	StringBuilder sb = new StringBuilder();
		BufferedReader reader;
	    try {
	    	reader = new BufferedReader(new InputStreamReader(request.getInputStream()));
	        String line;
	        while ((line = reader.readLine()) != null) {
	            sb.append(line).append('\n');
	        }
	        reader.close();
	    } catch(IOException e) {	
	    }
	    
	    return sb.toString();
	}
}
