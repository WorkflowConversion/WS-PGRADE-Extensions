package com.workflowconversion.portlet.ui.table;

/**
 * Listener for row clicks.
 * 
 * @author delagarza
 *
 * @param <T>
 *            the type of elements that the table holds.
 */
public interface SelectedElementChangedListener<T> {

	/**
	 * Event fired when a row is clicked.
	 * 
	 * @param element
	 *            the selected element.
	 * 
	 */
	void elementSelected(final T element);
}
