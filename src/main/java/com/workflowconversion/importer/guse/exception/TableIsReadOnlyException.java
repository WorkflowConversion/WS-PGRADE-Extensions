package com.workflowconversion.importer.guse.exception;

/**
 * Thrown if the application table is to be modified while read only.
 * 
 * @author delagarza
 *
 */
public class TableIsReadOnlyException extends ApplicationException {

	private static final long serialVersionUID = 9220408285519189440L;

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            a message.
	 */
	public TableIsReadOnlyException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * 
	 * @param cause
	 *            the cause.
	 */
	public TableIsReadOnlyException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            a message.
	 * @param cause
	 *            the cause.
	 */
	public TableIsReadOnlyException(String message, Throwable cause) {
		super(message, cause);
	}

}
