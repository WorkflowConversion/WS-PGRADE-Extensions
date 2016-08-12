package com.workflowconversion.importer.guse.permission;

import com.workflowconversion.importer.guse.user.PortletUser;

/**
 * Simple permission manager that determines if a user has read/write access to certain content.
 * 
 * @author delagarza
 *
 */
public interface PermissionManager {

	/**
	 * Whether the given user has write access. If any user has write access, he/she automatically has read access.
	 * 
	 * @param user
	 *            The user.
	 * @return {@code true} if the user has write access.
	 */
	public boolean hasWriteAccess(final PortletUser user);

	/**
	 * Whether the given user has read access.
	 * 
	 * @param user
	 *            The user.
	 * @return {@code true} if the user has read access.
	 */
	public boolean hasReadAccess(final PortletUser user);
}
