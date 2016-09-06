package com.workflowconversion.importer.guse.filter;

import java.util.Collection;

/**
 * Generic interface that serves as guideline for filter implementations. Implementations can decide whether to sort out
 * the filtered input.
 * 
 * @author delagarza
 *
 */
public interface Filter<T> {

	/**
	 * Applies the filter to the passed collection.
	 * 
	 * @param input
	 *            a collection of objects to filter.
	 * @return a collection with the objects that <i>passed</i> the criteria of the filter. Whether this output
	 *         collection is sorted or not, is left up to the implementation.
	 */
	public Collection<T> apply(final Collection<T> input);
}
