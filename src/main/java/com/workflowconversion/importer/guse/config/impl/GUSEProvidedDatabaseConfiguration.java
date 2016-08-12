package com.workflowconversion.importer.guse.config.impl;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.workflowconversion.importer.guse.config.DatabaseConfiguration;

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
public class GUSEProvidedDatabaseConfiguration implements DatabaseConfiguration {

	private static final Logger LOG = LoggerFactory.getLogger(GUSEProvidedDatabaseConfiguration.class);

	private static final String GUSE_KEY_DATABASE_DRIVER = "guse.system.database.driver";
	private static final String GUSE_KEY_DATABASE_URL = "guse.system.database.url";
	private static final String GUSE_KEY_DATABASE_USER = "guse.system.database.user";
	private static final String GUSE_KEY_DATABASE_PASSWORD = "guse.system.database.password";

	// we don't need these two properties, but if they are missing, it means that gUSE did not initialize the portlet
	private static final String GUSE_KEY_INFORMATION_SERVICE_URL = "is.url";
	private static final String GUSE_KEY_INFORMATION_SERVICE_ID = "is.id";

	// since calling the webservices every time is time consuming, store the information in a cache
	private final LoadingCache<String, String> properties = CacheBuilder.newBuilder()
			.expireAfterAccess(5, TimeUnit.MINUTES).build(new CacheLoader<String, String>() {
				public String load(String key) {
					final String value = PropertyLoader.getInstance().getProperty(key);
					// CacheLoader does not allow null values, so tweak it a little bit
					return value == null ? "" : value;
				}
			});

	@Override
	public String getDriver() {
		return properties.getUnchecked(GUSE_KEY_DATABASE_DRIVER);
	}

	@Override
	public String getURL() {
		return properties.getUnchecked(GUSE_KEY_DATABASE_URL);
	}

	@Override
	public String getUsername() {
		return properties.getUnchecked(GUSE_KEY_DATABASE_USER);
	}

	@Override
	public String getPassword() {
		return properties.getUnchecked(GUSE_KEY_DATABASE_PASSWORD);
	}

	/**
	 * Returns {@code true} if all of the needed configuration values could be properly retrieved from gUSE.
	 * 
	 * @return {@code true} if all of the needed configuration values are present.
	 */
	@Override
	public boolean isValid() {
		boolean valid = true;
		// check all of the required properties
		for (final String key : new String[] { GUSE_KEY_DATABASE_DRIVER, GUSE_KEY_DATABASE_URL, GUSE_KEY_DATABASE_USER,
				GUSE_KEY_DATABASE_PASSWORD, GUSE_KEY_INFORMATION_SERVICE_URL, GUSE_KEY_INFORMATION_SERVICE_ID }) {
			final String value = properties.getUnchecked(key);
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
