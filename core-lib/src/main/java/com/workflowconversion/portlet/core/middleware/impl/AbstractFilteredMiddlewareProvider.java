package com.workflowconversion.portlet.core.middleware.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.workflowconversion.portlet.core.filter.Filter;
import com.workflowconversion.portlet.core.filter.impl.FilterFactory;
import com.workflowconversion.portlet.core.middleware.MiddlewareProvider;

import dci.data.Item;
import dci.data.Middleware;

/**
 * Middleware provider that uses filters to return the objects of interest. Subclasses must implement the
 * {@link #getAllMiddlewares()} method.
 * 
 * @author delagarza
 *
 */
public abstract class AbstractFilteredMiddlewareProvider implements MiddlewareProvider {

	private static final long serialVersionUID = -5954904339394289681L;

	@Override
	public final Collection<Middleware> getAvailableMiddlewares() {
		// filter only using availability
		final FilterFactory middlewareFilter = new FilterFactory();
		middlewareFilter.setEnabled(true);
		return middlewareFilter.newMiddlewareFilter().apply(getAllMiddlewares());
	}

	@Override
	public final Collection<Middleware> getAvailableMiddlewares(final String middlewareType) {
		Validate.isTrue(StringUtils.isNotBlank(middlewareType),
				"middlewareType cannot be null, empty or contain only whitespaces, this is probably a bug and should be reported.");
		// filter using availability and type
		final FilterFactory middlewareFilter = new FilterFactory();
		middlewareFilter.setEnabled(true).setType(middlewareType);
		return middlewareFilter.newMiddlewareFilter().apply(getAllMiddlewares());
	}

	@Override
	public final Collection<Item> getAvailableItems(final String middlewareType) {
		// get the middlewares first
		Validate.isTrue(StringUtils.isNotBlank(middlewareType),
				"middlewareType cannot be null, empty or contain only whitespaces, this is probably a bug and should be reported.");
		// filter using availability and type
		final Filter<Middleware> middlewareFilter = new FilterFactory().setEnabled(true).setType(middlewareType)
				.newMiddlewareFilter();
		final Collection<Middleware> availableMiddlewares = middlewareFilter.apply(getAllMiddlewares());

		// filter the available items for each of the available middlewares
		final Collection<Item> availableItems = new LinkedList<Item>();
		final Filter<Item> itemFilter = new FilterFactory().setEnabled(true).newInstance();
		for (final Middleware availableMiddleware : availableMiddlewares) {
			availableItems.addAll(itemFilter.apply(availableMiddleware.getItem()));
		}

		return availableItems;
	}

	@Override
	public Set<String> getAllMiddlewareTypes() {
		final Set<String> middlewareTypes = new TreeSet<String>();
		for (final Middleware middleware : getAllMiddlewares()) {
			middlewareTypes.add(middleware.getType());
		}
		return middlewareTypes;
	}
}
