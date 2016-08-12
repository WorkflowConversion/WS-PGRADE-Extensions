package com.workflowconversion.importer.guse.permission.impl;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workflowconversion.importer.guse.exception.ApplicationException;
import com.workflowconversion.importer.guse.permission.PermissionManager;
import com.workflowconversion.importer.guse.user.PortletUser;

/**
 * Loads permission configuration from a properties file. If the file was changed after loading time, the contents are
 * reloaded.
 * 
 * @author delagarza
 *
 */
public class PropertiesFilePermissionManager implements PermissionManager {

	private final static String KEY_WRITE_ROLES = "write.roles";
	private final static String KEY_READ_ROLES = "read.roles";

	private static final Logger LOG = LoggerFactory.getLogger(PropertiesFilePermissionManager.class);

	private final FileConfiguration fileConfig;

	/**
	 * Constructor.
	 * 
	 * @param propertiesFileLocation
	 *            The location of the properties file containing the roles/permissions.
	 */
	public PropertiesFilePermissionManager(final String propertiesFileLocation) {
		try {
			this.fileConfig = new PropertiesConfiguration(propertiesFileLocation);
			this.fileConfig.setReloadingStrategy(new FileChangedReloadingStrategy() {
				@Override
				public void reloadingPerformed() {
					super.reloadingPerformed();
					if (LOG.isInfoEnabled()) {
						LOG.info("Refreshed permissions from " + fileConfig.getFile().getAbsolutePath());
					}
				}
			});
		} catch (ConfigurationException e) {
			// there is not much we can do...
			throw new ApplicationException("An error occurred while loading the permissions configuration.");
		}
	}

	@Override
	public boolean hasWriteAccess(final PortletUser user) {
		return hasAccess(KEY_WRITE_ROLES, user);
	}

	@Override
	public boolean hasReadAccess(final PortletUser user) {
		return hasAccess(KEY_READ_ROLES, user) || hasWriteAccess(user);
	}

	private boolean hasAccess(final String key, final PortletUser user) {
		final String[] roles = fileConfig.getStringArray(key);
		for (final String role : roles) {
			if (user.hasRole(role.trim())) {
				return true;
			}
		}
		return false;
	}

}
