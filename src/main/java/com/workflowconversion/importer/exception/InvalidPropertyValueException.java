package com.workflowconversion.importer.exception;

/**
 * Exception thrown when a property has an invalid value.
 * 
 * @author delagarza
 */
public class InvalidPropertyValueException extends ApplicationException {

	private static final long serialVersionUID = -5316212383773748430L;

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            A message.
	 */
	public InvalidPropertyValueException(final String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * 
	 * @param cause
	 *            The cause of this exception.
	 */
	public InvalidPropertyValueException(final Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            A message.
	 * @param cause
	 *            The cause of this exception.
	 */
	public InvalidPropertyValueException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
