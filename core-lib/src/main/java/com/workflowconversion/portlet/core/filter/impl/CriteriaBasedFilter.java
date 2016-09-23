package com.workflowconversion.portlet.core.filter.impl;

import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.lang.Validate;

import com.workflowconversion.portlet.core.filter.Criterion;
import com.workflowconversion.portlet.core.filter.Filter;

/**
 * Filter for gUSE middlewares/items.
 * 
 * @author delagarza
 */
class CriteriaBasedFilter<T> implements Filter<T> {

	private final Collection<Criterion<T>> criteria;

	CriteriaBasedFilter(final Collection<Criterion<T>> criteria) {
		Validate.notEmpty(criteria, "criteria cannot be null or empty, this is probably a bug and should be reported.");
		this.criteria = criteria;
	}

	@Override
	public Collection<T> apply(final Collection<T> input) {
		final Collection<T> filteredInput = new LinkedList<T>();
		for (final T element : input) {
			boolean applies = true;
			for (final Criterion<T> criterion : criteria) {
				// if only one of the criteria is not satisfied, then we filter out this element
				if (!criterion.applies(element)) {
					applies = false;
					break;
				}
			}
			if (applies) {
				filteredInput.add(element);
			}
		}
		return filteredInput;
	}
}
