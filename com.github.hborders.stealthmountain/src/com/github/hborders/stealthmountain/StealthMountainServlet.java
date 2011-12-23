package com.github.hborders.stealthmountain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterRuntimeException;
import twitter4j.auth.AccessToken;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query.FilterOperator;

@SuppressWarnings("serial")
public class StealthMountainServlet extends HttpServlet {
	private static final int MAX_DATASTORE_QUERY_FILTER_LIST_SIZE = 30;
	private static final int MAX_CORRECTION_TWEET_COUNT = 1;

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
			DatastoreService datastoreService = DatastoreServiceFactory
					.getDatastoreService();
			com.google.appengine.api.datastore.Query lastSneakPeakTweetIdDatastoreQuery = new com.google.appengine.api.datastore.Query(
					"LastSneakPeakTweetId");
			List<Entity> lastSneakPeakTweetIdEntities = datastoreService
					.prepare(lastSneakPeakTweetIdDatastoreQuery).asList(
							FetchOptions.Builder.withLimit(1));
			Entity lastSneakPeakTweetIdEntity;
			if (lastSneakPeakTweetIdEntities.size() > 0) {
				lastSneakPeakTweetIdEntity = lastSneakPeakTweetIdEntities
						.get(0);
			} else {
				lastSneakPeakTweetIdEntity = new Entity("LastSneakPeakTweetId");
			}

			Long lastSneakPeakTweetId = (Long) lastSneakPeakTweetIdEntity
					.getProperty("lastSneakPeakTweetId");

			Query sneakPeakQuery = new Query("sneak peak");
			if (lastSneakPeakTweetId != null) {
				sneakPeakQuery.setSinceId(lastSneakPeakTweetId);
			}
			sneakPeakQuery.setResultType(Query.RECENT);
			sneakPeakQuery.setRpp(100);
			QueryResult sneakPeakQueryResult = twitter.search(sneakPeakQuery);

			if (!sneakPeakQueryResult.getTweets().isEmpty()) {
				Map<Long, SneakPeak> sneakPeakUserIdsToSneakPeaks = new HashMap<Long, SneakPeak>();
				List<Long> datastoreQueryFilterSneakPeakUserIds = new ArrayList<Long>(
						MAX_DATASTORE_QUERY_FILTER_LIST_SIZE);

				List<Tweet> oldestTweets = new ArrayList<Tweet>(
						sneakPeakQueryResult.getTweets());
				Collections.reverse(oldestTweets);

				for (Tweet sneakPeakTweet : oldestTweets) {
					if (!sneakPeakTweet.getText().contains("RT")) {
						String sneakPeakUser = sneakPeakTweet.getFromUser();
						long sneakPeakUserId = sneakPeakTweet.getFromUserId();
						long sneakPeakTweetId = sneakPeakTweet.getId();
						String sneakPeakText = sneakPeakTweet.getText();

						if (!sneakPeakUserIdsToSneakPeaks
								.containsKey(sneakPeakUserId)) {
							sneakPeakUserIdsToSneakPeaks.put(sneakPeakUserId,
									new SneakPeak(sneakPeakUser,
											sneakPeakUserId, sneakPeakTweetId,
											sneakPeakText));
							datastoreQueryFilterSneakPeakUserIds
									.add(sneakPeakUserId);
						}

						if (datastoreQueryFilterSneakPeakUserIds.size() == MAX_DATASTORE_QUERY_FILTER_LIST_SIZE) {
							removeCorrectedUserSneakPeaks(
									sneakPeakUserIdsToSneakPeaks,
									datastoreQueryFilterSneakPeakUserIds);

							if (sneakPeakUserIdsToSneakPeaks.size() >= MAX_CORRECTION_TWEET_COUNT) {
								break;
							}
						}
					}
				}
				if (datastoreQueryFilterSneakPeakUserIds.size() > 0) {
					removeCorrectedUserSneakPeaks(sneakPeakUserIdsToSneakPeaks,
							datastoreQueryFilterSneakPeakUserIds);
				}

				List<SneakPeak> correctingSneakPeaks = new ArrayList<SneakPeak>(
						sneakPeakUserIdsToSneakPeaks.values());
				Collections.sort(correctingSneakPeaks);
				if (correctingSneakPeaks.size() > MAX_CORRECTION_TWEET_COUNT) {
					correctingSneakPeaks = correctingSneakPeaks.subList(0,
							MAX_CORRECTION_TWEET_COUNT);
				}

				for (SneakPeak correctingSneakPeak : correctingSneakPeaks) {
					String correctingSneakPeakTweetLog = "correcting: "
							+ correctingSneakPeak + "<br>";
					resp.getWriter().println(correctingSneakPeakTweetLog);
					System.out.println(correctingSneakPeakTweetLog);
				}

				boolean updatedLastSneakPeakTweetIdEntity = false;
				for (SneakPeak correctingSneakPeak : correctingSneakPeaks) {
					String correctionStatusText = "@"
							+ correctingSneakPeak.sneakPeakUser
							+ " I think you mean \"sneak peek\"";
					StatusUpdate correctionStatusUpdate = new StatusUpdate(
							correctionStatusText);
					correctionStatusUpdate
							.setInReplyToStatusId(correctingSneakPeak.sneakPeakTweetId);
					String correctionStatusUpdateLog = "Correction StatusUpdate: "
							+ correctionStatusUpdate + "<br>";
					resp.getWriter().println(correctionStatusUpdateLog);
					System.out.println(correctionStatusUpdateLog);
					Status correctionStatus = twitter
							.updateStatus(correctionStatusUpdate);
					try {
						correctionStatus.getId(); // throws
													// TwitterRuntimeException
													// if correctionStatusUpdate
													// failed

						Entity correctedSneakPeakUserIdEntity = new Entity(
								"CorrectedSneakPeakUserId");
						correctedSneakPeakUserIdEntity.setProperty(
								"correctedSneakPeakUserId",
								correctingSneakPeak.sneakPeakUserId);
						datastoreService.put(correctedSneakPeakUserIdEntity);
						lastSneakPeakTweetIdEntity.setUnindexedProperty(
								"lastSneakPeakTweetId",
								correctingSneakPeak.sneakPeakTweetId);
						updatedLastSneakPeakTweetIdEntity = true;
					} catch (TwitterRuntimeException e) {
						e.printStackTrace(System.err);
						resp.getWriter().println("Fail!<br>");
						resp.getWriter().println("<pre>");
						e.printStackTrace(resp.getWriter());
						resp.getWriter().println("</pre>");
						break;
					}
				}

				if (updatedLastSneakPeakTweetIdEntity) {
					datastoreService.put(lastSneakPeakTweetIdEntity);
				}
			} else {
				resp.getWriter().println("No tweets found!");
				System.out.println("No tweets found!");
			}
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

	private void removeCorrectedUserSneakPeaks(
			Map<Long, SneakPeak> sneakPeakUserIdsToSneakPeaks,
			List<Long> datastoreQueryFilterSneakPeakUserIds) {
		com.google.appengine.api.datastore.Query sneakPeakUserIdsDatastoreQuery = new com.google.appengine.api.datastore.Query(
				"CorrectedSneakPeakUserId");
		sneakPeakUserIdsDatastoreQuery.addFilter("correctedSneakPeakUserId",
				FilterOperator.IN, datastoreQueryFilterSneakPeakUserIds);
		DatastoreService datastoreService = DatastoreServiceFactory
				.getDatastoreService();
		List<Entity> correctedSneakPeakUserIdEntities = datastoreService
				.prepare(sneakPeakUserIdsDatastoreQuery).asList(
						FetchOptions.Builder.withDefaults());
		for (Entity correctedSneakPeakUserIdEntity : correctedSneakPeakUserIdEntities) {
			sneakPeakUserIdsToSneakPeaks.remove(correctedSneakPeakUserIdEntity
					.getProperty("correctedSneakPeakUserId"));
		}
		datastoreQueryFilterSneakPeakUserIds.clear();
	}
}
