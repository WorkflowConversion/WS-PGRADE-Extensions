package com.workflowconversion.portlet.ui.table;

/**
 * These listeners are notified for every element that has been committed and when a batch of elements has been
 * committed.
 * 
 * @author delagarza
 *
 */
public interface GenericBatchCommittedListener<T> extends GenericElementCommittedListener<T> {

	/**
	 * Notifies that a batch of elements has been committed.
	 */
	void batchCommitted();
}
