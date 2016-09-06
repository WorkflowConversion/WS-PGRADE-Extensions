package com.workflowconversion.importer.guse.middleware.filter.impl;

import com.workflowconversion.importer.guse.filter.Criterion;

import dci.data.Middleware;

/**
 * Middleware criterion based on its availability (i.e., whether it is enabled or not).
 * 
 * @author delagarza
 *
 */
class MiddlewareAvailabilityCriterion implements Criterion<Middleware> {

	private final boolean desiredAvailability;

	MiddlewareAvailabilityCriterion(final boolean desiredAvailability) {
		this.desiredAvailability = desiredAvailability;
	}

	@Override
	public boolean applies(final Middleware input) {
		return input.isEnabled() == desiredAvailability;
	}

}
