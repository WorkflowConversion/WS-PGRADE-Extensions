package com.workflowconversion.importer.exception;

import com.workflowconversion.importer.user.PortletUser;

/**
 * Exception thrown when a non-authenticated user tries to access content.
 * 
 * @author delagarza
 *
 */
public class UserNotAuthenticatedException extends ApplicationException {
	private static final long serialVersionUID = -3895512191990359425L;

	/**
	 * Constructor.
	 * 
	 * @param user
	 *            The user.
	 */
	public UserNotAuthenticatedException(final PortletUser user) {
		this(user, "");
	}

	/**
	 * Constructor.
	 * 
	 * @param user
	 *            The user.
	 * @param message
	 *            A message.
	 */
	public UserNotAuthenticatedException(final PortletUser user, final String message) {
		super("The user [" + user.toString() + "] is not authenticated. " + message);
	}

}
