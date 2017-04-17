package com.workflowconversion.portlet.ui.table;

/**
 * Base class for table factories. Concrete implementations must implement the {@link #newInstance()} method.
 * 
 * @author delagarza
 *
 * @param <T>
 *            the element type.
 */
public abstract class AbstractTableWithControlsFactory<T> implements TableWithControlsFactory<T> {

	protected String title;

	@Override
	public TableWithControlsFactory<T> withTitle(final String title) {
		this.title = title;
		return this;
	}
}
