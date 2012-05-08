package com.stealthmountain;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;

@SuppressWarnings("serial")
public class SearchAndRespondServlet extends HttpServlet {
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		Long userId = Long.valueOf(req.getParameter("userId"));

		DatastoreService datastoreService = DatastoreServiceFactory
				.getDatastoreService();
		Query botQuery = new Query("Bot");
		botQuery.addFilter("userId", FilterOperator.EQUAL, userId);
		Entity botEntity = datastoreService.prepare(botQuery).asSingleEntity();
		Twitter twitter = new TwitterFactory().getInstance();
		twitter.setOAuthAccessToken(new AccessToken((String) botEntity
				.getProperty("token"), (String) botEntity
				.getProperty("tokenSercret")));

		String search = (String) botEntity.getProperty("search");
		twitter4j.Query twitter4jQuery = new twitter4j.Query("\"" + search
				+ "\"");
		twitter4jQuery.setLang("en");
		twitter4jQuery.setResultType(twitter4j.Query.RECENT);
		twitter4jQuery.setRpp(100);
		if (botEntity.hasProperty("lastRespondedTweetId")) {
			long lastRespondedTweetId = (Long) botEntity
					.getProperty("lastRespondedTweetId");
			twitter4jQuery.setSinceId(lastRespondedTweetId);
		}

		try {
			List<Tweet> tweets = twitter.search(twitter4jQuery).getTweets();
			for (ListIterator<Tweet> tweetListIterator = tweets
					.listIterator(tweets.size()); tweetListIterator
					.hasPrevious();) {
				Tweet tweet = tweetListIterator.previous();
				String tweetText = tweet.getText();
				if (!tweetText.contains("@")) {
					String lowercasedTweetText = tweetText.toLowerCase();
					if (lowercasedTweetText.contains(search)) {
						long fromUserId = tweet.getFromUserId();
						long tweetId = tweet.getId();

					}
				}
			}
		} catch (TwitterException e) {
			throw new ServletException(e);
		}
	}
}
