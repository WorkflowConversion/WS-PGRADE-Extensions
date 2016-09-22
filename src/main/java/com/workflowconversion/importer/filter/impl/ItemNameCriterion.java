package com.workflowconversion.importer.filter.impl;

import com.workflowconversion.importer.filter.Criterion;

import dci.data.Item;

/**
 * Name-based item criterion.
 * 
 * @author delagarza
 *
 */
class ItemNameCriterion implements Criterion<Item> {

	private final String name;

	ItemNameCriterion(final String name) {
		this.name = name;
	}

	@Override
	public boolean applies(final Item input) {
		return input.getName().equals(name);
	}

}
