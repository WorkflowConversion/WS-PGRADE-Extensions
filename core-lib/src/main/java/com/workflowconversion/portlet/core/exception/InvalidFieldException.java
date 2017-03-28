package com.workflowconversion.portlet.core.exception;

import com.workflowconversion.portlet.core.resource.FormField;

/**
 * Thrown when an invalid field is accessed or used.
 * 
 * @author delagarza
 *
 */
public class InvalidFieldException extends ApplicationException {

	private static final long serialVersionUID = -2238171432102321851L;

	/**
	 * Constructor.
	 * 
	 * @param field
	 *            the invalid field.
	 */
	public InvalidFieldException(final FormField field) {
		super("Invalid application field: " + field.toString()
				+ ", this seems to be a coding problem and should be reported.");
	}

	/**
	 * Constructor.
	 * 
	 * @param field
	 *            the invalid field
	 */
	public InvalidFieldException(final String field) {
		super("Invalid application field: " + field + ", this seems to be a coding problem and should be reported.");
	}

}
