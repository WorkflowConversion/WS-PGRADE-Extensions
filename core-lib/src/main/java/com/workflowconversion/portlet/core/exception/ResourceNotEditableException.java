package com.workflowconversion.portlet.core.exception;

/**
 * Resource thrown when it is attempted to edit a read-only resource.
 * 
 * @author delagarza
 *
 */
public class ResourceNotEditableException extends ApplicationException {

	private static final long serialVersionUID = 9177207244367838146L;

	/**
	 * @param message
	 *            the message
	 */
	public ResourceNotEditableException(final String message) {
		super(message);
	}

	/**
	 * Default constructor.
	 */
	public ResourceNotEditableException() {
		this("This resource is not editable.");
	}

}
