package com.stealthmountain;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;

@SuppressWarnings("serial")
public class SigninServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		Twitter twitter = new TwitterFactory().getInstance();
		// Fill in these values below, not committing to github for security
		// reasons
		twitter.setOAuthConsumer("QOx1JMxO5Ypvs7Ay3Hvx9Q",
				"xctArzc76L9j1ItFI80UxWz7oOyVRtjc5cDRt8");
		request.getSession().setAttribute("twitter", twitter);
		try {
			StringBuffer callbackUrlStringBuffer = request.getRequestURL();
			int index = callbackUrlStringBuffer.lastIndexOf("/");
			callbackUrlStringBuffer.replace(index,
					callbackUrlStringBuffer.length(), "").append("/callback");

			RequestToken requestToken = twitter
					.getOAuthRequestToken(callbackUrlStringBuffer.toString());
			request.getSession().setAttribute("requestToken", requestToken);
			response.sendRedirect(requestToken.getAuthenticationURL());
		} catch (TwitterException e) {
			throw new ServletException(e);
		}
	}
}
