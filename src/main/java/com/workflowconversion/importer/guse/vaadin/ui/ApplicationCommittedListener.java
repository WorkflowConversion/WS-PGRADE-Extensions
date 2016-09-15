package com.workflowconversion.importer.guse.vaadin.ui;

import com.workflowconversion.importer.guse.appdb.Application;

/**
 * Listener invoked whenever the a new valid application is to be added.
 * 
 * @author delagarza
 *
 */
public interface ApplicationCommittedListener {

	/**
	 * Invoked when a valid application is about to be added.
	 * 
	 * @param application
	 *            the new valid application
	 */
	public void applicationCommitted(final Application application);
}
