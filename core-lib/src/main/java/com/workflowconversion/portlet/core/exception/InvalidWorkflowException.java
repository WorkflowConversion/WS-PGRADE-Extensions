package com.workflowconversion.portlet.core.exception;

import java.io.File;

/**
 * Exception thrown when a file doesn't contain the expected workflow.
 * 
 * @author delagarza
 *
 */
public class InvalidWorkflowException extends ApplicationException {
	private static final long serialVersionUID = 6662300874390961043L;

	/**
	 * Constructor.
	 * 
	 * @param archive
	 *            the file that allegedly contains a workflow.
	 */
	public InvalidWorkflowException(final File archive) {
		this("The provided file doesn't contain a valid WS-PGRADE workflow.", archive);
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            a message
	 * @param archive
	 *            the file that allegedly contains a workflow.
	 */
	public InvalidWorkflowException(final String message, final File archive) {
		super(message + " File location: " + archive.getAbsolutePath());
	}

	/**
	 * Constructor.
	 * 
	 * @param archive
	 *            the file that allegedly contains a workflow.
	 * @param cause
	 *            the root cause of this exception.
	 */
	public InvalidWorkflowException(final File archive, final Exception cause) {
		this("There was a problem while processing the workflow. Perhaps the provided file doesn't contain a valid WS-PGRADE workflow?",
				archive, cause);
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            the message.
	 * @param archive
	 *            the file that allegedly contains a workflow.
	 * @param cause
	 *            the root cause of this exception.
	 */
	public InvalidWorkflowException(final String message, final File archive, final Exception cause) {
		super(message + " File location: " + archive.getAbsolutePath(), cause);
	}
}
