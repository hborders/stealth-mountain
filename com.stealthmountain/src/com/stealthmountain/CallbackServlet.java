package com.stealthmountain;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;

@SuppressWarnings("serial")
public class CallbackServlet extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Twitter twitter = (Twitter) request.getSession()
				.getAttribute("twitter");
		RequestToken requestToken = (RequestToken) request.getSession()
				.getAttribute("requestToken");
		String verifier = request.getParameter("oauth_verifier");
		try {
			request.getSession().removeAttribute("requestToken");
			request.getSession().removeAttribute("twitter");
			AccessToken accessToken = twitter.getOAuthAccessToken(requestToken,
					verifier);
			Query botQuery = new Query("Bot");
			botQuery.addFilter("userId", FilterOperator.EQUAL,
					accessToken.getUserId());
			DatastoreService datastoreService = DatastoreServiceFactory
					.getDatastoreService();
			Entity botEntity = datastoreService.prepare(botQuery)
					.asSingleEntity();
			if (botEntity != null) {
				botEntity = new Entity("Bot");
				botEntity.setProperty("userId", accessToken.getUserId());
			}
			botEntity.setProperty("token", accessToken.getToken());
			botEntity.setProperty("tokenSecret", accessToken.getTokenSecret());

			datastoreService.put(botEntity);
		} catch (TwitterException e) {
			throw new ServletException(e);
		}
		response.sendRedirect(request.getContextPath() + "/setup.html");
	}

}
