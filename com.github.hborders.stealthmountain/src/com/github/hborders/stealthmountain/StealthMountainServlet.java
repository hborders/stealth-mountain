package com.github.hborders.stealthmountain;

import java.io.IOException;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitter4j.Query;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterRuntimeException;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query.FilterOperator;

@SuppressWarnings("serial")
public class StealthMountainServlet extends HttpServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		resp.setContentType("text/html");
		resp.getWriter().println("<html>");
		resp.getWriter().println("<body>");

		try {
			resp.getWriter().println("Attempting to search<br>");

			ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
			configurationBuilder.setGZIPEnabled(false);
			Configuration configuration = configurationBuilder.build();
			Twitter twitter = new TwitterFactory(configuration).getInstance();
			// Fill in these values below, not committing to github for security
			// reasons
			twitter.setOAuthConsumer("consumer key", "consumer secret");
			twitter.setOAuthAccessToken(new AccessToken("token", "token secret"));

			try {
				DatastoreService datastoreService = DatastoreServiceFactory
						.getDatastoreService();
				Entity lastSneakPeakTweetIdEntity = findOrCreateLastSneakPeakTweetIdEntity(datastoreService);

				Long lastSneakPeakTweetId = (Long) lastSneakPeakTweetIdEntity
						.getProperty("lastSneakPeakTweetId");
				List<Tweet> sneakPeakTweets = findSneakPeakTweets(twitter,
						lastSneakPeakTweetId);

				Entity resultEntity = new Entity("Result");
				resultEntity.setProperty("searchTimeMillis",
						System.currentTimeMillis());
				resultEntity.setProperty("count", sneakPeakTweets.size());

				if (sneakPeakTweets.isEmpty()) {
					resp.getWriter().println("No tweets found!");
					System.out.println("No tweets found!");
				} else {
					boolean corrected = false;
					for (ListIterator<Tweet> sneakPeakTweetsListIterator = sneakPeakTweets
							.listIterator(sneakPeakTweets.size()); sneakPeakTweetsListIterator
							.hasPrevious();) {
						Tweet sneakPeakTweet = sneakPeakTweetsListIterator
								.previous();
						String sneakPeakTweetText = sneakPeakTweet.getText();
						if (!sneakPeakTweetText.contains("@")) {
							String sneakPeakTweetLowercasedText = sneakPeakTweetText
									.toLowerCase();
							if (sneakPeakTweetLowercasedText
									.contains("sneak peak")) {
								long sneakPeakUserId = sneakPeakTweet
										.getFromUserId();
								long sneakPeakTweetId = sneakPeakTweet.getId();

								if (notAlreadyCorrectedSneakPeakUserId(
										datastoreService, twitter,
										sneakPeakUserId)) {
									corrected = true;
									resultEntity.setProperty("chosenOffset",
											sneakPeakTweetsListIterator
													.previousIndex() + 1);
									correctSneakPeakTweet(resp,
											datastoreService, twitter,
											lastSneakPeakTweetIdEntity,
											sneakPeakTweet, sneakPeakUserId,
											sneakPeakTweetId);
									break;
								}
							}
						}
					}

					datastoreService.put(resultEntity);

					if (!corrected) {
						System.out.println("Couldn't find someone to correct");
						resp.getWriter().println(
								"Couldn't find someone to correct<br>");
					}
				}
			} catch (TwitterException e) {
				e.printStackTrace(System.err);
				resp.getWriter().println("Fail!<br>");
				resp.getWriter().println("<pre>");
				e.printStackTrace(resp.getWriter());
				resp.getWriter().println("</pre>");
			}
		} catch (Exception e) {
			for (Throwable cause = e; cause != null; cause = cause.getCause()) {
				e.printStackTrace(System.err);
				resp.getWriter().println("Completely unexpected fail");
				resp.getWriter().println("<pre>");
				e.printStackTrace(resp.getWriter());
				resp.getWriter().println("</pre>");
				resp.getWriter().println("which was caused by...");
			}
		}

		resp.getWriter().println("</body>");
		resp.getWriter().println("</html>");
	}

	private Entity findOrCreateLastSneakPeakTweetIdEntity(
			DatastoreService datastoreService) {
		com.google.appengine.api.datastore.Query lastSneakPeakTweetIdDatastoreQuery = new com.google.appengine.api.datastore.Query(
				"LastSneakPeakTweetId");
		Entity lastSneakPeakTweetIdEntity = datastoreService.prepare(
				lastSneakPeakTweetIdDatastoreQuery).asSingleEntity();
		if (lastSneakPeakTweetIdEntity == null) {
			lastSneakPeakTweetIdEntity = new Entity("LastSneakPeakTweetId");
		}

		return lastSneakPeakTweetIdEntity;
	}

	private List<Tweet> findSneakPeakTweets(Twitter twitter,
			Long lastSneakPeakTweetId) throws TwitterException {
		Query sneakPeakQuery = new Query("\"sneak peak\"");
		sneakPeakQuery.setLang("en");
		sneakPeakQuery.setResultType(Query.RECENT);
		sneakPeakQuery.setRpp(100);
		if (lastSneakPeakTweetId != null) {
			sneakPeakQuery.setSinceId(lastSneakPeakTweetId);
		}

		return twitter.search(sneakPeakQuery).getTweets();
	}

	private boolean notAlreadyCorrectedSneakPeakUserId(
			DatastoreService datastoreService, Twitter twitter,
			long sneakPeakUserId) {
		com.google.appengine.api.datastore.Query sneakPeakUserIdsDatastoreQuery = new com.google.appengine.api.datastore.Query(
				"CorrectedSneakPeakUserId");
		sneakPeakUserIdsDatastoreQuery.addFilter("correctedSneakPeakUserId",
				FilterOperator.EQUAL, sneakPeakUserId);
		return datastoreService.prepare(sneakPeakUserIdsDatastoreQuery)
				.countEntities(FetchOptions.Builder.withLimit(1)) == 0;
	}

	private void correctSneakPeakTweet(HttpServletResponse resp,
			DatastoreService datastoreService, Twitter twitter,
			Entity lastSneakPeakTweetIdEntity, Tweet sneakPeakTweet,
			long sneakPeakUserId, long sneakPeakTweetId) throws IOException,
			TwitterException {
		String correctingSneakPeakTweetLog = "correcting: " + sneakPeakTweet
				+ "<br>";
		resp.getWriter().println(correctingSneakPeakTweetLog);
		System.out.println(correctingSneakPeakTweetLog);

		String correctionStatusText = "@" + sneakPeakTweet.getFromUser()
				+ " I think you mean \"sneak peek\"";
		StatusUpdate correctionStatusUpdate = new StatusUpdate(
				correctionStatusText);
		correctionStatusUpdate.setInReplyToStatusId(sneakPeakTweetId);
		String correctionStatusUpdateLog = "Correction StatusUpdate: "
				+ correctionStatusUpdate + "<br>";
		resp.getWriter().println(correctionStatusUpdateLog);
		System.out.println(correctionStatusUpdateLog);
		Status correctionStatus = twitter.updateStatus(correctionStatusUpdate);
		try {
			correctionStatus.getId(); // throws
										// TwitterRuntimeException
										// if
										// correctionStatusUpdate
										// failed

			saveNewCorrectedSneakPeakUserId(datastoreService, sneakPeakUserId);
			updateLastSneakPeakTweetIdEntity(datastoreService,
					lastSneakPeakTweetIdEntity, sneakPeakTweetId);
		} catch (TwitterRuntimeException e) {
			e.printStackTrace(System.err);
			resp.getWriter().println("Fail!<br>");
			resp.getWriter().println("<pre>");
			e.printStackTrace(resp.getWriter());
			resp.getWriter().println("</pre>");
		}
	}

	private void saveNewCorrectedSneakPeakUserId(
			DatastoreService datastoreService, long correctedSneakPeakUserId) {
		Entity correctedSneakPeakUserIdEntity = new Entity(
				"CorrectedSneakPeakUserId");
		correctedSneakPeakUserIdEntity.setProperty("correctedSneakPeakUserId",
				correctedSneakPeakUserId);
		datastoreService.put(correctedSneakPeakUserIdEntity);
	}

	private void updateLastSneakPeakTweetIdEntity(
			DatastoreService datastoreService,
			Entity lastSneakPeakTweetIdEntity, long lastSneakPeakTweetId) {
		lastSneakPeakTweetIdEntity.setUnindexedProperty("lastSneakPeakTweetId",
				lastSneakPeakTweetId);
		datastoreService.put(lastSneakPeakTweetIdEntity);
	}
}
