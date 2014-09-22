package fuzzer.apps;
/**
 * 
 * @authors Connor <csh6900>, Gabriel <gem5597>, Joe <jak3122>
 *
 */

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

public class fuzzer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		CLIParser commandParser = new CLIParser(args); //See CLIParser.get() for description of how to get parameters
		
		WebClient webClient = new WebClient();
		try {
			HtmlPage page = webClient.getPage("http://localhost:8080/bodgeit/login.jsp?username=test&password=hello");
            System.out.println("URL:");
            System.out.println(page.getUrl());
            System.out.println("URL inputs:");
            System.out.println(InputDiscovery.getUrlInputs(page.getUrl()));
			InputDiscovery.printInputs(webClient, page);
		} catch (FailingHttpStatusCodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
