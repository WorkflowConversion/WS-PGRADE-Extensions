package com.workflowconversion.portlet.core.exception;

import com.workflowconversion.portlet.core.resource.Application;

/**
 * Exception thrown when an application does not satisfy certain constraints, such as having a non-null name, for
 * instance.
 * 
 * @author delagarza
 */
public class InvalidApplicationException extends ApplicationException {

	private static final long serialVersionUID = 4137745984964519338L;

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            A message.
	 * @param application
	 *            The invalid application.
	 */
	public InvalidApplicationException(final String message, final Application application) {
		super(message + ", invalid application: " + application);
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            A message.
	 * @param application
	 *            The invalid application.
	 * @param cause
	 *            The cause of this exception.
	 */
	public InvalidApplicationException(final String message, final Application application, final Throwable cause) {
		super(message + ", invalid application: " + application, cause);
	}

}
