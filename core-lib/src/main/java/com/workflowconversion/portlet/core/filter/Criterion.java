package com.workflowconversion.portlet.core.filter;

/**
 * Similar to a filter, but applies to single instances.
 * 
 * @author delagarza
 *
 */
public interface Criterion<T> {

	/**
	 * Whether this criterion applies to the given input.
	 * 
	 * @param input
	 *            the input.
	 * @return {@code true} if the input satisfies the criterion.
	 */
	public boolean applies(final T input);
}
