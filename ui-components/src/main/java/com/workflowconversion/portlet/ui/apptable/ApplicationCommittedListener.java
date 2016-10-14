package com.workflowconversion.portlet.ui.apptable;

import com.workflowconversion.portlet.core.app.Application;

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
	void applicationCommitted(final Application application);
}
