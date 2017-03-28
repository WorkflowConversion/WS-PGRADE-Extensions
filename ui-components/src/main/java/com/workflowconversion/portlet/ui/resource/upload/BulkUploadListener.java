package com.workflowconversion.portlet.ui.resource.upload;

import java.util.Collection;

import com.workflowconversion.portlet.core.resource.Resource;

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
	 * Parsing warning occurred.
	 * 
	 * @param warning
	 *            a description of the warning.
	 * @param lineNumber
	 *            the line number.
	 */
	void parsingWarning(final String warning, final long lineNumber);

	/**
	 * Parsing warning occurred.
	 * 
	 * @param warning
	 *            a description of the warning.
	 */
	void parsingWarning(final String warning);

	/**
	 * Parsing completed.
	 * 
	 * @param parsedResources
	 *            the collection of resources that were parsed from the input file.
	 */
	void parsingCompleted(final Collection<Resource> parsedResources);
}
