package com.workflowconversion.importer.exception;

/**
 * Generic exception.
 * 
 * @author delagarza
 *
 */
public class ApplicationException extends RuntimeException {

	private static final long serialVersionUID = 8742655203645104243L;

	/**
	 * @param message
	 */
	public ApplicationException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public ApplicationException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ApplicationException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}
}
