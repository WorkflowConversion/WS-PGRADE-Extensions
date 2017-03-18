package com.workflowconversion.portlet.ui.table;

import com.workflowconversion.portlet.core.resource.HasKey;

/**
 * Base class for table factories. Concrete implementations must implement the {@link #build()} method.
 * 
 * @author delagarza
 *
 * @param <T>
 *            the element type.
 */
public abstract class AbstractTableWithControlsFactory<T extends HasKey> implements TableWithControlsFactory<T> {

	protected boolean allowEdition;
	protected boolean withDetails;
	protected boolean allowDuplicates;
	protected boolean allowMultipleSelection;
	protected String title;

	@Override
	public TableWithControlsFactory<T> allowEdition(final boolean allowEdition) {
		this.allowEdition = allowEdition;
		return this;
	}

	@Override
	public TableWithControlsFactory<T> withDetails(final boolean withDetails) {
		this.withDetails = withDetails;
		return this;
	}

	@Override
	public TableWithControlsFactory<T> withTitle(final String title) {
		this.title = title;
		return this;
	}

	@Override
	public TableWithControlsFactory<T> allowDuplicates(final boolean allowDuplicates) {
		this.allowDuplicates = allowDuplicates;
		return this;
	}

	@Override
	public TableWithControlsFactory<T> allowMultipleSelection(final boolean allowMultipleSelection) {
		this.allowMultipleSelection = allowMultipleSelection;
		return this;
	}
}
