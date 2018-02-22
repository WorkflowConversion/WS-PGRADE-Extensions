package com.workflowconversion.portlet.core.middleware.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.Validate;

import com.workflowconversion.portlet.core.filter.Filter;
import com.workflowconversion.portlet.core.filter.FilterApplicator;
import com.workflowconversion.portlet.core.filter.impl.SimpleFilterFactory;
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
	public final Collection<Middleware> getEnabledMiddlewares() {
		// filter only using availability
		final SimpleFilterFactory middlewareFilter = new SimpleFilterFactory();
		middlewareFilter.setEnabled(true);
		return FilterApplicator.applyFilter(getAllMiddlewares(), middlewareFilter.newMiddlewareFilter());
	}

	@Override
	public final Collection<Middleware> getEnabledMiddlewares(final String middlewareType) {
		Validate.notBlank(middlewareType,
				"middlewareType cannot be null, empty or contain only whitespaces, this is probably a bug and should be reported.");
		// filter using availability and type
		final SimpleFilterFactory middlewareFilter = new SimpleFilterFactory();
		middlewareFilter.setEnabled(true).withType(middlewareType);
		return FilterApplicator.applyFilter(getAllMiddlewares(), middlewareFilter.newMiddlewareFilter());
	}

	@Override
	public final Collection<Item> getEnabledItems(final String middlewareType) {
		// get the middlewares first
		Validate.notBlank(middlewareType,
				"middlewareType cannot be null, empty or contain only whitespaces, this is probably a bug and should be reported.");
		// filter using availability and type
		final Collection<Middleware> availableMiddlewares = getEnabledMiddlewares(middlewareType);

		// filter the available items for each of the available middlewares
		final Collection<Item> availableItems = new LinkedList<Item>();
		final Filter<Item> itemFilter = new SimpleFilterFactory().setEnabled(true).newItemFilter();
		for (final Middleware availableMiddleware : availableMiddlewares) {
			availableItems.addAll(FilterApplicator.applyFilter(availableMiddleware.getItem(), itemFilter));
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
