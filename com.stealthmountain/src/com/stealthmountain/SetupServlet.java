package com.stealthmountain;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;

@SuppressWarnings("serial")
public class SetupServlet extends HttpServlet {

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		long userId = (Long) req.getSession().getAttribute("userId");

		String search = req.getParameter("search");
		String response = req.getParameter("response");

		Query botQuery = new Query("Bot");
		botQuery.addFilter("userId", FilterOperator.EQUAL, userId);
		DatastoreService datastoreService = DatastoreServiceFactory
				.getDatastoreService();
		Entity botEntity = datastoreService.prepare(botQuery).asSingleEntity();
		botEntity.setProperty("search", search);
		botEntity.setProperty("response", response);
		botEntity.setProperty("searchable", true);

		datastoreService.put(botEntity);
	}

}
