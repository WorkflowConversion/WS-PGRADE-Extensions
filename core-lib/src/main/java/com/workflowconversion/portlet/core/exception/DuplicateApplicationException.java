package com.workflowconversion.portlet.core.exception;

import com.workflowconversion.portlet.core.app.Application;
import com.workflowconversion.portlet.core.app.ResourceProvider;

/**
 * Thrown by instances of {@link ResourceProvider} when an insertion/edition results in a duplicate.
 * 
 * @author delagarza
 *
 */
public class DuplicateApplicationException extends ApplicationException {

	private static final long serialVersionUID = -7388349157090017138L;

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            A message.
	 * @param application
	 *            The duplicate application.
	 */
	public DuplicateApplicationException(final String message, final Application application) {
		super(message + ", duplicate application: " + application);
	}

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            A message.
	 * @param The
	 *            duplicate application.
	 * @param cause
	 *            The underlying cause of this exception.
	 */
	public DuplicateApplicationException(final String message, final Application application, final Throwable cause) {
		super(message + ", duplicate application: " + application, cause);
	}

}
