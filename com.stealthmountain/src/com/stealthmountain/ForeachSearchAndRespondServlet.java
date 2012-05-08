package com.stealthmountain;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

@SuppressWarnings("serial")
public class ForeachSearchAndRespondServlet extends HttpServlet {
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		DatastoreService datastoreService = DatastoreServiceFactory
				.getDatastoreService();
		Query botQuery = new Query("Bot");
		botQuery.addFilter("searchable", FilterOperator.EQUAL, true);
		botQuery.addProjection(new PropertyProjection("userId", Long.class));
		Queue queue = QueueFactory.getQueue("searchAndRespond");
		for (Entity botEntity : datastoreService.prepare(botQuery).asIterable()) {
			queue.add(TaskOptions.Builder
					.withUrl("")
					.method(Method.POST)
					.param("userId",
							Long.toString((Long) botEntity
									.getProperty("userId"))));
		}
	}
}
