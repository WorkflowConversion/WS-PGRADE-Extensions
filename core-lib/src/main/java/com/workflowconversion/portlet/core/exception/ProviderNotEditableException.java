package com.workflowconversion.portlet.core.exception;

/**
 * Exception thrown when it is attempted to edit a read-only application provider.
 * 
 * @author delagarza
 *
 */
public class ProviderNotEditableException extends ApplicationException {

	private static final long serialVersionUID = -4721655382750206432L;

	/**
	 * Constructor.
	 * 
	 * @param message
	 *            A message.
	 */
	public ProviderNotEditableException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}
}
