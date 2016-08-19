package com.workflowconversion.importer.guse.exception;

import com.workflowconversion.importer.guse.appdb.Application;
import com.workflowconversion.importer.guse.appdb.ApplicationProvider;

/**
 * Thrown by instances of {@link ApplicationProvider} when an insertion/edition results in a duplicate.
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
		// TODO Auto-generated constructor stub
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
		// TODO Auto-generated constructor stub
	}

}
