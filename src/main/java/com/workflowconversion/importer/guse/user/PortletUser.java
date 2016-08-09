package com.workflowconversion.importer.guse.user;

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
public class PortletUser {

	protected final static Logger LOG = LoggerFactory.getLogger(PortletUser.class);

	private final User liferayUser;
	private final Set<Role> roles;

	/**
	 * Constructor.
	 * 
	 * @param liferayUser
	 *            The <i>real</i> liferay user.
	 */
	public PortletUser(final User liferayUser) {
		this.liferayUser = liferayUser;
		this.roles = new TreeSet<Role>();
		fillRoles();
	}

	private void fillRoles() {
		// guest users don't have any roles
		if (isAuthenticated()) {
			try {
				for (final com.liferay.portal.model.Role role : liferayUser.getRoles()) {
					// convert between liferay role name and our enums
					switch (role.getName()) {
					case "Administrator":
						roles.add(Role.Admin);
						break;
					case "Power User":
						roles.add(Role.PowerUser);
						break;
					case "User":
						roles.add(Role.User);
						break;
					case "End User":
						roles.add(Role.EndUser);
						break;
					case "Guest":
						roles.add(Role.Guest);
						break;
					case "kittyrole":
						roles.add(Role.KittyRole);
						break;
					case "RobotPermissionOwner":
						roles.add(Role.RobotPermissionOwner);
						break;
					default:
						if (LOG.isInfoEnabled()) {
							LOG.info("Ignoring unrecognized role: " + role.getName());
						}
					}
				}
			} catch (SystemException e) {
				throw new RuntimeException("Could not retrieve user roles.", e);
			}
		}
	}

	/**
	 * Only authenticated users have a <i>real</i> liferay user.
	 * 
	 * @return If the liferay user is not {@code null}.
	 */
	public boolean isAuthenticated() {
		return liferayUser != null;
	}

	/**
	 * Whether the user has write privileges.
	 * 
	 * @return {@code true} if the user has write privileges, {@code false} otherwise.
	 */
	public boolean canEdit() {
		return roles.contains(Role.Admin) || roles.contains(Role.KittyRole);
	}

	/**
	 * Whether the user has read privileges.
	 * 
	 * @return {@code true} if the user has read privileges, {@code false} otherwise.
	 */
	public boolean canRead() {
		// so, anyone that is logged-in has read access

		// Interviewer: Gary Oldman, who has read access?
		// Gary Oldman: https://www.youtube.com/watch?v=74BzSTQCl_c (except Guest users, that is)
		return roles.contains(Role.User) || roles.contains(Role.PowerUser) || roles.contains(Role.Admin)
				|| roles.contains(Role.EndUser) || roles.contains(Role.KittyRole);
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
	 * Simple enum mapping role names to handy java objects.
	 * 
	 * @author delagarza
	 */
	public enum Role {
		Admin, PowerUser, User, EndUser, Guest, KittyRole, RobotPermissionOwner;
	}

}
