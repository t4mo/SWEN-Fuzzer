package fuzzer.apps;

/**
 * 
 * @authors Connor <csh6900>, Gabriel <gem5597>, Joe <jak3122>
 *
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

import com.gargoylesoftware.htmlunit.CookieManager;
import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

import fuzzer.apps.VVector.BufferOverflowVector;
import fuzzer.apps.VVector.SanitizationVector;
import fuzzer.apps.VVector.SanitizationVectorTest;
import fuzzer.apps.VVector.VVector;
import fuzzer.apps.VVector.XSS_SQLVector;
import fuzzer.apps.VVector.XSS_SQLVectorTest;

public class fuzzer {

	/**
	 * Function to receive a list of strings to try and guess from a given file
	 * 
	 * @return - A list of strings with potential links to find
	 */
	public static ArrayList<String> getGuesses(String path) {
		File file = new File(path);
		BufferedReader reader;
		ArrayList<String> lines = new ArrayList<String>();
		try {
			reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return lines;
	}
	
	/**
	 * The recursive calling function for discoverLinks()
	 * 
	 *  Will only follow links associated with a web application and if they have not already been found
	 *  
	 * @param webClient 
	 * @param url
	 * @param foundLinks
	 * @return
	 */
	private static HashSet<String> discoverLinksRecursively(WebClient webClient, String url, HashSet<String> foundLinks) {

		try {
			// Avoid URLs that are actually email addresses
			URL tryUrl = new URL(url);
			if ("mailto".equals(tryUrl.getProtocol()))
			{
				return foundLinks;
			}
			HtmlPage page = webClient.getPage(tryUrl);
			List<HtmlAnchor> links = page.getAnchors();
			for (HtmlAnchor link : links) {
				// Return a String representation of the HtmlAnchor
				URL nextURLobj = page.getFullyQualifiedUrl(link.getHrefAttribute());
                String nextURL = nextURLobj.toString();
				// Check to make sure the link is not visisted and is associated with the fuzzUrl
				if ( (!foundLinks.contains(nextURL) && (nextURL.contains(tryUrl.getHost()))) ) {
					foundLinks.add(nextURL);
					System.out.println("[" + link.asText() + "] "
						+ nextURLobj.getPath());
					foundLinks = discoverLinksRecursively(webClient, nextURL, foundLinks);
				}
			}

		} catch (FailingHttpStatusCodeException | IOException e) {}
		
		return foundLinks;
	}

	/**
		 * This code is for showing how you can get all the links on a given page,
		 * and visit a given URL
		 * 
		 * @param webClient
		 * @throws IOException
		 * @throws MalformedURLException
		 */
		private static void discoverLinks(WebClient webClient, String url) {

			System.out.println("--------------------------------------");
			System.out.println("Discovering links...");
			System.out.println("--------------------------------------");
				
			HashSet<String> links = discoverLinksRecursively(webClient, url, new HashSet<String>());
		}

	/**
	 * 
	 * @param webClient
	 */
	private static void guessPages(WebClient webClient, String url,
			String wordsPath) {

		try {
			System.out.println("--------------------------------------");
			System.out.println("Guessing common pages...");
			System.out.println("--------------------------------------");
			ArrayList<String> lines = getGuesses(wordsPath);
			for (String line : lines) {
				HtmlPage guess = webClient.getPage(url + line);
				WebResponse response = guess.getWebResponse();
				int statusCode = response.getStatusCode();
				if (guess.isHtmlPage() && statusCode != 404) {
					System.out.println("Page discovered: " + guess.getUrl());
				}
			}

		} catch (FailingHttpStatusCodeException | IOException e) {
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// Turn off those CSS errors and warnings
		java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(
				Level.OFF);
		System.setProperty("org.apache.commons.logging.Log",
				"org.apache.commons.logging.impl.NoOpLog");

		CLIParser commandParser = new CLIParser(args); // See CLIParser.get()
														// for description of
														// how to get parameters
		WebClient webClient = new WebClient();
		WebClientOptions options = webClient.getOptions();
		options.setThrowExceptionOnFailingStatusCode(false);
		options.setPrintContentOnFailingStatusCode(false);

		String fuzzMode = commandParser.get("mode");
		String fuzzUrl = commandParser.get("url");
		String fuzzAuth = commandParser.get("cauth");
		String fuzzWords = commandParser.get("cwords");
        String fuzzVectors = commandParser.get("vectors");
        String fuzzSensitive = commandParser.get("sensitive");
        String fuzzRandom = commandParser.get("random");
        String fuzzSlow = commandParser.get("slow");
        HtmlPage page = null;
        
        if ("".equals(fuzzMode) && "".equals(fuzzUrl)) {
        	System.out.println("Invalid mode and/or parameters received.");
        	commandParser.printHelp("fuzzer MODE URL ARGS");
        	System.exit(1);
        }
        
        try {
        	page = webClient.getPage(fuzzUrl);
        } catch (IOException e) {
				System.err.println(fuzzUrl + " could not be opened.");
                System.exit(1);
        }
		
        //Do authentication first
        if (!"".equals(fuzzAuth)) {
            PageLogin login = new PageLogin();
            login.printLogon(page, fuzzAuth);
            
            //If we succeed login in, use the new page as our source
            if (login.isLoggedIn())
            {
                page = login.getNextPage();
                fuzzUrl = page.getUrl().toString();
            }
        }
        

        System.out.println();
        System.out.println("Fuzz-discover on url: " + fuzzUrl);
        // Discover links
        discoverLinks(webClient, fuzzUrl);
        // Guess pages
        guessPages(webClient, fuzzUrl, fuzzWords);
        // Input discovery
        System.out.println(InputDiscovery.getUrlInputs(page.getUrl()));
        InputDiscovery.printInputs(webClient, page);

        
		if (fuzzMode.equals("test")) {
			// Fuzz-test code here.
            if (!"".equals(fuzzRandom)) {
            
            }
            if (!"".equals(fuzzVectors)) {
    			System.out.println("--------------------------------------");
    			System.out.println("Executing attack vectors...");
    			System.out.println("--------------------------------------");
                ExecuteVectors exec;
                //Build Vector list
                String[] elem = fuzzVectors.split(",");
                List<VVector> vectors = buildVectors(page, Arrays.asList(elem));
                
                exec = new ExecuteVectors(vectors);
                List<Boolean> results = exec.execute();
                for (int i = 0; i < results.size(); i++)
                {
                	System.out.println(vectors.get(i).getDescription());
                }
            }
            if (!"".equals(fuzzSensitive)) {
                SensitiveDataSearch searcher = new SensitiveDataSearch(page, fuzzSensitive);
                ArrayList<String> sensitiveResults = searcher.search();
                System.out.println("Sensitive data:");
                for (String s : sensitiveResults) {
                    System.out.println(s);
                }
            }
		}
	}
	
	private static List<VVector> buildVectors(HtmlPage aPage, List<String> aVectors)
	{
		List<VVector> result = new ArrayList<VVector>();
		for (String vec : aVectors)
		{
			switch (vec.toLowerCase()){
			case "sanitize":
				result.add(new SanitizationVector(aPage, "FIXME"));
				break;
			case "bufferoverflow":
				result.add(new BufferOverflowVector(aPage));
				break;
				
			case "sqlinjection":
				result.add(new XSS_SQLVector(aPage, "' OR 'a'='a' OR '"));
				break;
				
			case "xss":
				result.add(new XSS_SQLVector(aPage, "<script>alert(\"hello\")</script>"));
				break;
			}
		}
		return result;
	}
}
