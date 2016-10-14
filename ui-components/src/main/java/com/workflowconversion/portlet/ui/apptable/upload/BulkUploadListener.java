package com.workflowconversion.portlet.ui.apptable.upload;

import java.util.Collection;

import com.workflowconversion.portlet.core.app.Application;

/**
 * Interface for listeners interested in bulk upload events.
 * 
 * @author delagarza
 *
 */
public interface BulkUploadListener {

	/**
	 * Parsing started.
	 */
	void parsingStarted();

	/**
	 * Parsing error occurred in a specific line.
	 * 
	 * @param error
	 *            a description of the error.
	 * @param lineNumber
	 *            the line number
	 */
	void parsingError(final String error, final long lineNumber);

	/**
	 * Parsing error occurred.
	 * 
	 * @param error
	 *            The description of the error.
	 */
	void parsingError(final String error);

	/**
	 * Parsing completed.
	 * 
	 * @param parsedApplications
	 *            the collection of applications that were parsed from the input file.
	 */
	void parsingCompleted(final Collection<Application> parsedApplications);
}
