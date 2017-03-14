package com.workflowconversion.portlet.core.user;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;

/**
 * Simple class that contains the few things we need to know about users, namely, if they have access to certain parts
 * of the portlet.
 * 
 * @author delagarza
 *
 */
public class PortletUser implements Serializable {

	private static final long serialVersionUID = 2414547246279102482L;

	protected final static Logger LOG = LoggerFactory.getLogger(PortletUser.class);

	private final User liferayUser;
	private final Set<String> roles;

	/**
	 * Constructor.
	 * 
	 * @param liferayUser
	 *            The <i>real</i> liferay user.
	 */
	public PortletUser(final User liferayUser) {
		this.liferayUser = liferayUser;
		this.roles = new TreeSet<String>();
		fillRoles();
	}

	private void fillRoles() {
		// guest users don't have any roles
		if (isAuthenticated()) {
			try {
				for (final com.liferay.portal.model.Role role : liferayUser.getRoles()) {
					// convert between liferay role name and our enums
					roles.add(role.getName());
				}
			} catch (final SystemException e) {
				throw new RuntimeException("Could not retrieve user roles.", e);
			}
		}
	}

	/**
	 * Only authenticated users have a <i>real</i> liferay user.
	 * 
	 * @return {@code true} the liferay user is not {@code null}.
	 */
	public boolean isAuthenticated() {
		return liferayUser != null;
	}

	/**
	 * Returns the full name of the user.
	 * 
	 * @return The full name of the user.
	 */
	public String getFullName() {
		if (isAuthenticated()) {
			return liferayUser.getFullName();
		} else {
			return "(non-authenticated) Guest User";
		}
	}

	/**
	 * Returns the user id.
	 * 
	 * @return the user id.
	 */
	public long getUserId() {
		return liferayUser.getUserId();
	}

	/**
	 * Whether this user has the passed role.
	 * 
	 * @param role
	 *            The role.
	 * @return {@code true} if this user has the queried role.
	 */
	public boolean hasRole(final String role) {
		return roles.contains(role);
	}
}
