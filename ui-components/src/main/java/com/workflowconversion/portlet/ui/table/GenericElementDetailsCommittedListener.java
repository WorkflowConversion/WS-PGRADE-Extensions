package com.workflowconversion.portlet.ui.table;

/**
 * Listener that handles details being saved.
 * 
 * @author delagarza
 *
 */
public interface GenericElementDetailsCommittedListener<T> {

	/**
	 * Called when the details of an element have been committed/saved.
	 * 
	 * @param itemId
	 *            the id of the element whose details were modified.
	 * @param element
	 *            the element.
	 */
	void elementDetailsCommitted(final Object itemId, final T element);
}
