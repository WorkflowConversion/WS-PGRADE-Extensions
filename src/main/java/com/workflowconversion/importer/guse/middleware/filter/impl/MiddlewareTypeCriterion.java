package com.workflowconversion.importer.guse.middleware.filter.impl;

import com.workflowconversion.importer.guse.filter.Criterion;

import dci.data.Middleware;

/**
 * Criterion based on middleware type.
 * 
 * @author delagarza
 *
 */
class MiddlewareTypeCriterion implements Criterion<Middleware> {

	private final String type;

	MiddlewareTypeCriterion(final String type) {
		this.type = type;
	}

	@Override
	public boolean applies(final Middleware input) {
		return input.getType().equals(type);
	}

}
