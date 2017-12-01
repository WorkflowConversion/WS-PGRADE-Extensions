package com.workflowconversion.portlet.ui.resource.upload;

import java.util.Collection;

/**
 * Interface for listeners interested in bulk upload events.
 * 
 * @author delagarza
 *
 */
public interface BulkUploadListener<T> {

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
	 * @param parsedElements
	 *            a collection containing all valid, parsed elements.
	 */
	void parsingCompleted(final Collection<T> parsedElements);
}
