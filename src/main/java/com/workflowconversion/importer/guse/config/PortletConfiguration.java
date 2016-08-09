package com.workflowconversion.importer.guse.config;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.sztaki.lpds.information.local.PropertyLoader;

/**
 * Simple class that reads properties from gUSE's {@link PropertyLoader}.
 * 
 * Users are responsible to make sure that the configuration is valid. A convenience method (i.e., {@link #isValid()})
 * is provided to make sure that all needed configuration values have been properly set.
 * 
 * @author delagarza
 *
 */
public class PortletConfiguration {

	private static final Logger LOG = LoggerFactory.getLogger(PortletConfiguration.class);

	private static final String GUSE_KEY_DATABASE_DRIVER = "guse.system.database.driver";
	private static final String GUSE_KEY_DATABASE_URL = "guse.system.database.url";
	private static final String GUSE_KEY_DATABASE_USER = "guse.system.database.user";
	private static final String GUSE_KEY_DATABASE_PASSWORD = "guse.system.database.password";

	// we don't need these two properties, but if they are missing, it means
	// that gUSE did not initialize the portlet
	private static final String GUSE_KEY_INFORMATION_SERVICE_URL = "is.url";
	private static final String GUSE_KEY_INFORMATION_SERVICE_ID = "is.id";

	// we only need one configuration per portlet
	private static final PortletConfiguration INSTANCE = new PortletConfiguration();

	// enforce singleton pattern
	private PortletConfiguration() {

	}

	/**
	 * Returns the only instance of the {@link PortletConfiguration}.
	 * 
	 * @return The only instance of this class.
	 */
	public static PortletConfiguration getInstance() {
		return INSTANCE;
	}

	/**
	 * The database driver (e.g., {@code com.mysql.jdbc.Driver}).
	 * 
	 * @return The database driver.
	 */
	public String getDatabaseDriver() {
		return getGUSEProperty(GUSE_KEY_DATABASE_DRIVER);
	}

	/**
	 * The database JDBC url (e.g., {@code jdbc:mysql://localhost:3306/user})
	 * 
	 * @return The databse url.
	 */
	public String getDatabaseURL() {
		return getGUSEProperty(GUSE_KEY_DATABASE_URL);
	}

	/**
	 * The username to access the database.
	 * 
	 * @return The database username.
	 */
	public String getDatabaseUsername() {
		return getGUSEProperty(GUSE_KEY_DATABASE_USER);
	}

	/**
	 * The password to access the database.
	 * 
	 * @return The database password.
	 */
	public String getDatabasePassword() {
		return getGUSEProperty(GUSE_KEY_DATABASE_PASSWORD);
	}

	private String getGUSEProperty(final String key) {
		return PropertyLoader.getInstance().getProperty(key);
	}

	/**
	 * Returns {@code true} if all of the needed configuration values could be retrieved from gUSE.
	 * 
	 * @return {@code true} if all of the needed configuration values are present.
	 */
	public boolean isValid() {
		boolean valid = true;
		// check all of the required properties
		for (final String key : new String[] { GUSE_KEY_DATABASE_DRIVER, GUSE_KEY_DATABASE_URL, GUSE_KEY_DATABASE_USER,
				GUSE_KEY_DATABASE_PASSWORD, GUSE_KEY_INFORMATION_SERVICE_URL, GUSE_KEY_INFORMATION_SERVICE_ID }) {
			final String value = PropertyLoader.getInstance().getProperty(key);
			if (StringUtils.isBlank(value)) {
				LOG.error("The required gUSE property [" + key
						+ "] has not been defined. It is highly probable that the portlet needs to be initialized by restarting gUSE.");
				// let it fail if at least one property is missing
				valid = false;
			}
		}
		return valid;
	}
}
