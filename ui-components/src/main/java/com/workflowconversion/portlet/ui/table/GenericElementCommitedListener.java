package com.workflowconversion.portlet.ui.table;

/**
 * Notified whenever an element is committed into an add form in order to be inserted in a table.
 * 
 * @author delagarza
 *
 */
public interface GenericElementCommitedListener<T> {

	/**
	 * Notifies that an element is valid and is ready to be added.
	 * 
	 * @param committedElement
	 *            the valid element.
	 */
	void elementCommitted(final T committedElement);
}
