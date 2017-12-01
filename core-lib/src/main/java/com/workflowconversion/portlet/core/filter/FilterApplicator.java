package com.workflowconversion.portlet.core.filter;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.lang.Validate;

/**
 * Simple class that iterates over a collection and applies filters.
 * 
 * @author delagarza
 *
 */
public class FilterApplicator {

	/**
	 * Utility method to iterate over a collection and filter it out.
	 * 
	 * @param input
	 *            the input collection.
	 * @param filter
	 *            the filter to apply.
	 * @return the filtered elements.
	 */
	public static <T> Collection<T> applyFilter(final Collection<T> input, final Filter<T> filter) {
		Validate.notNull(input, "input cannot be null, this seems to be a coding problem and should be reported");
		Validate.notNull(filter, "filter cannot be null, this seems to be a coding problem and should be reported");
		final Collection<T> filteredOutput = new LinkedList<T>();
		for (final T element : input) {
			if (filter.passes(element)) {
				filteredOutput.add(element);
			}
		}
		return filteredOutput;
	}
}
