package com.workflowconversion.portlet.ui.table;

/**
 * Implementations of {@link TableWithControls} have several options. Using a simple abstract factory pattern makes
 * building these tables a bit easier.
 * 
 * @author delagarza
 *
 * @param <T>
 *            the type of elements to be displayed in the tables constructed by this factory.
 */
public interface TableWithControlsFactory<T> {

	/**
	 * Builds a {@link TableWithControls} with the set options.
	 * 
	 * @return a new instance of a {@link TableWithControls}.
	 */
	TableWithControls<T> newInstance();

	/**
	 * Sets the title of the table to build.
	 * 
	 * @param title
	 *            the title of the table to build.
	 * @return a reference to {@code this} factory.
	 */
	TableWithControlsFactory<T> withTitle(final String title);
}
