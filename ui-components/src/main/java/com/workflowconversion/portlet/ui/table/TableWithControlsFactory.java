package com.workflowconversion.portlet.ui.table;

import com.workflowconversion.portlet.core.resource.HasKey;

/**
 * Implementations of {@link TableWithControls} have several options. Using a simple abstract factory pattern makes
 * building these tables a bit easier.
 * 
 * @author delagarza
 *
 * @param <T>
 *            the type of elements to be displayed in the tables constructed by this factory.
 */
public interface TableWithControlsFactory<T extends HasKey> {

	/**
	 * Builds a {@link TableWithControls} with the set options.
	 * 
	 * @return a new instance of a {@link TableWithControls}.
	 */
	TableWithControls<T> build();

	/**
	 * Sets whether the instance to build will have a button to display details.
	 * 
	 * @param withElementDetailsButton
	 *            whether the instance will have a button to display details.
	 * @return a reference to {@code this} factory.
	 */
	TableWithControlsFactory<T> withDetails(final boolean withElementDetailsButton);

	/**
	 * Sets whether the instance to build will have edit controls (an "add element" button and a "remove element"
	 * button).
	 * 
	 * @param allowEdition
	 *            whether the instance will have edit controls.
	 * @return a reference to {@code this} factory.
	 */
	TableWithControlsFactory<T> allowEdition(final boolean allowEdition);

	/**
	 * Sets whether the instance to build will allow duplicates.
	 * 
	 * @param allowDuplicates
	 *            whether the instance will allow duplicates.
	 * @return a reference to {@code this} factory.
	 */
	TableWithControlsFactory<T> allowDuplicates(final boolean allowDuplicates);

	/**
	 * Sets the title of the table to build.
	 * 
	 * @param title
	 *            the title of the table to build.
	 * @return a reference to {@code this} factory.
	 */
	TableWithControlsFactory<T> withTitle(final String title);
}
