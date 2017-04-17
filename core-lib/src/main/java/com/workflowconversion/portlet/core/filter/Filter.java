package com.workflowconversion.portlet.core.filter;

/**
 * Generic interface that serves as guideline for filter implementations. Implementations can decide whether to sort out
 * the filtered input.
 * 
 * @author delagarza
 *
 */
public interface Filter<T> {

	/**
	 * States whether the passed element passes the filter criteria.
	 * 
	 * @param element
	 *            the element to check.
	 * 
	 * @return {@code true} if the passed element passes the criteria of this filter.
	 */
	public boolean passes(final T element);
}
