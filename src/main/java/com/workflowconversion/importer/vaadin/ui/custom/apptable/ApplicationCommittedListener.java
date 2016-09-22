package com.workflowconversion.importer.vaadin.ui.custom.apptable;

import com.workflowconversion.importer.app.Application;

/**
 * Listener invoked whenever the a new valid application is to be added.
 * 
 * @author delagarza
 *
 */
interface ApplicationCommittedListener {

	/**
	 * Invoked when a valid application is about to be added.
	 * 
	 * @param application
	 *            the new valid application
	 */
	public void applicationCommitted(final Application application);
}
