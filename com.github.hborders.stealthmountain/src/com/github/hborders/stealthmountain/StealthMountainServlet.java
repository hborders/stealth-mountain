package com.github.hborders.stealthmountain;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

@SuppressWarnings("serial")
public class StealthMountainServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/html");
		resp.getWriter().println("<html>");
		resp.getWriter().println("<body>");

		String status = "Hello World " + System.currentTimeMillis();

		resp.getWriter().println("Attempting to tweet:" + status + "<br>");

		Twitter twitter = new TwitterFactory().getInstance();
		// Fill in these values below, not committing to github for security
		// reasons
		twitter.setOAuthConsumer("consumer-key", "consumer-secret");
		twitter.setOAuthAccessToken(new AccessToken("token", "token-secret"));
		try {
			Status updatedStatus = twitter.updateStatus(status);
			updatedStatus.getId();

			resp.getWriter().println("Success!");
		} catch (TwitterException e) {
			resp.getWriter().println("Fail!<br>");
			resp.getWriter().println("<pre>");
			e.printStackTrace(resp.getWriter());
			resp.getWriter().println("</pre>");
		}

		resp.getWriter().println("</body>");
		resp.getWriter().println("</html>");
	}
}
