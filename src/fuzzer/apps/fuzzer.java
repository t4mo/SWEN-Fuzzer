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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebClientOptions;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;

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
	 * This code is for showing how you can get all the links on a given page,
	 * and visit a given URL
	 * 
	 * @param webClient
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	private static void discoverLinks(WebClient webClient, String url) {

		try {
			System.out.println("--------------------------------------");
			System.out.println("Discovering links...");
			System.out.println("--------------------------------------");
			HtmlPage page = webClient.getPage(url);
			// TODO dvwa
			List<HtmlAnchor> links = page.getAnchors();
			for (HtmlAnchor link : links) {
				System.out.println("[" + link.asText() + "] "
						+ link.getHrefAttribute());
			}

		} catch (FailingHttpStatusCodeException | IOException e) {
		}
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
		if (fuzzMode.equals("discover")) {
			System.out.println();
			System.out.println("Fuzz-discover on url: " + fuzzUrl);
			// Discover links
			discoverLinks(webClient, fuzzUrl);
			// Guess pages
			guessPages(webClient, fuzzUrl, fuzzWords);
			try {
				HtmlPage page = webClient.getPage(fuzzUrl);
				// Input discovery
				System.out.println(InputDiscovery.getUrlInputs(page.getUrl()));
				InputDiscovery.printInputs(webClient, page);
				// Custom authentication
				if (fuzzAuth != null) {
					PageLogin login = new PageLogin();
					login.printLogon(page, fuzzAuth);
					// PageLogin.printLogon(page, fuzzAuth);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if (fuzzMode.equals("test")) {
			// Fuzz-test code here.
		} else {
			System.out.println("Invalid mode \"" + fuzzMode + "\". "
					+ "Use \"discover\" or \"test\".");
		}
	}
}
