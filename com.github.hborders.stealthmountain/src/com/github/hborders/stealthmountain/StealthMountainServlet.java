package com.github.hborders.stealthmountain;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.StatusUpdate;
import twitter4j.Tweet;
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

		resp.getWriter().println("Attempting to search<br>");

		Twitter twitter = new TwitterFactory().getInstance();
		// Fill in these values below, not committing to github for security
		// reasons
		twitter.setOAuthConsumer("consumer key", "consumer secret");
		twitter.setOAuthAccessToken(new AccessToken("token", "token secret"));
		try {
			Query sneakPeakQuery = new Query("sneak peak");
			sneakPeakQuery.setResultType(Query.RECENT);
			sneakPeakQuery.setRpp(100);
			QueryResult sneakPeakQueryResult = twitter.search(sneakPeakQuery);

			for (Tweet sneakPeakTweet : sneakPeakQueryResult.getTweets()) {
				resp.getWriter().println(
						"From " + sneakPeakTweet.getFromUser() + ": "
								+ sneakPeakTweet.getText() + "<br>");
				String correctionStatus = "@" + sneakPeakTweet.getFromUser()
						+ " I think you mean \"sneak peek\"";
				StatusUpdate correctionStatusUpdate = new StatusUpdate(
						correctionStatus);
				correctionStatusUpdate.setInReplyToStatusId(sneakPeakTweet
						.getId());
				twitter.updateStatus(correctionStatusUpdate);
			}

			resp.getWriter().println("Success!");
		} catch (TwitterException e) {
			e.printStackTrace(System.err);
			resp.getWriter().println("Fail!<br>");
			resp.getWriter().println("<pre>");
			e.printStackTrace(resp.getWriter());
			resp.getWriter().println("</pre>");
		}

		resp.getWriter().println("</body>");
		resp.getWriter().println("</html>");
	}
}
