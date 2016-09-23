package com.workflowconversion.portlet.core.exception;

import com.workflowconversion.portlet.core.app.ApplicationField;

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
	 * @param field
	 *            the invalid field.
	 */
	public InvalidApplicationFieldException(final ApplicationField field) {
		super("Invalid application field: " + field.toString());
	}

	/**
	 * Constructor.
	 * 
	 * @param field
	 *            the invalid field
	 */
	public InvalidApplicationFieldException(final String field) {
		super("Invalid application field: " + field);
	}

}
