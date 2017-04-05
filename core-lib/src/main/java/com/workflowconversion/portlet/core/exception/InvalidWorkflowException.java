package com.workflowconversion.portlet.core.exception;

import java.nio.file.Path;

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
	public InvalidWorkflowException(final Path archive) {
		this("The provided archive doesn't contain a valid WS-PGRADE workflow.", archive);
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            a message
	 * @param archive
	 *            the file that allegedly contains a workflow.
	 */
	public InvalidWorkflowException(final String message, final Path archive) {
		super(message + ", archive location: " + archive.toString());
	}

	/**
	 * Constructor.
	 * 
	 * @param archive
	 *            the file that allegedly contains a workflow.
	 * @param cause
	 *            the root cause of this exception.
	 */
	public InvalidWorkflowException(final Path archive, final Exception cause) {
		this("There was a problem while processing the workflow. Perhaps the provided archive doesn't contain a valid WS-PGRADE workflow?",
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
	public InvalidWorkflowException(final String message, final Path archive, final Exception cause) {
		super(message + ", archive location: " + archive.toString(), cause);
	}
}
