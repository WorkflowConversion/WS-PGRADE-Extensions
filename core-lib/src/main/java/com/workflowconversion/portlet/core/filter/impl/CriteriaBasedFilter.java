package com.workflowconversion.portlet.core.filter.impl;

import java.util.Collection;

import org.apache.commons.lang3.Validate;

import com.workflowconversion.portlet.core.filter.Criterion;
import com.workflowconversion.portlet.core.filter.Filter;

/**
 * Filter for gUSE middlewares/items. For an element (middleware/item) to pass through the filter, all of the criteria
 * must be satisfied.
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
	public boolean passes(final T element) {
		for (final Criterion<T> criterion : criteria) {
			// if only one of the criteria is not satisfied, then we filter out this element
			if (!criterion.applies(element)) {
				return false;
			}
		}
		return true;
	}
}
