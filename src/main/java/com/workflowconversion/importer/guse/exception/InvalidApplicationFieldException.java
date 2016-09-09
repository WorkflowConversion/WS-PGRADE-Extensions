package com.workflowconversion.importer.guse.exception;

import com.workflowconversion.importer.guse.appdb.ApplicationField;

/**
 * Thrown when an invalid application field is accessed or used.
 * 
 * @author delagarza
 *
 */
public class InvalidApplicationFieldException extends ApplicationException {

	private static final long serialVersionUID = -2238171432102321851L;

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            a message.
	 * @param field
	 *            the invalid field.
	 */
	public InvalidApplicationFieldException(final ApplicationField field) {
		super("Invalid application field: " + field.toString());
	}

}
