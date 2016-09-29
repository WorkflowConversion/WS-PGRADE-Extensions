package com.workflowconversion.portlet.core.dbconfig.impl;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.workflowconversion.portlet.core.dbconfig.DatabaseConfigurationProvider;
import com.workflowconversion.portlet.core.exception.InvalidPropertyValueException;

import hu.sztaki.lpds.information.local.PropertyLoader;

/**
 * Simple class that reads properties from gUSE's {@link PropertyLoader}.
 * 
 * Users are responsible to make sure that the configuration is valid.
 * 
 * @author delagarza
 *
 */
public class GUSEDatabaseConfigurationProvider implements DatabaseConfigurationProvider {

	private static final long serialVersionUID = -7720187686401812914L;
	private static final String GUSE_KEY_DATABASE_DRIVER = "guse.system.database.driver";
	private static final String GUSE_KEY_DATABASE_URL = "guse.system.database.url";
	private static final String GUSE_KEY_DATABASE_USER = "guse.system.database.user";
	private static final String GUSE_KEY_DATABASE_PASSWORD = "guse.system.database.password";

	private final PoolProperties initialSettings;

	/**
	 * Constructor.
	 * 
	 * @param initialSettings
	 *            initial settings of the connection pool.
	 */
	public GUSEDatabaseConfigurationProvider(final PoolProperties initialSettings) {
		Validate.notNull(initialSettings, "initialSettings cannot be null");
		this.initialSettings = initialSettings;
	}

	// since calling the webservices every time is time consuming, store the information in a cache
	private final LoadingCache<String, String> properties = CacheBuilder.newBuilder()
			.expireAfterAccess(5, TimeUnit.MINUTES).build(new CacheLoader<String, String>() {
				public String load(String key) {
					final String value = PropertyLoader.getInstance().getProperty(key);
					// CacheLoader does not allow null values, so tweak it a little bit
					return value == null ? "" : value;
				}
			});

	private String getDriver() {
		return getAndValidateProperty(GUSE_KEY_DATABASE_DRIVER);
	}

	private String getURL() {
		return getAndValidateProperty(GUSE_KEY_DATABASE_URL);
	}

	private String getUsername() {
		return getAndValidateProperty(GUSE_KEY_DATABASE_USER);
	}

	private String getPassword() {
		return getAndValidateProperty(GUSE_KEY_DATABASE_PASSWORD);
	}

	private String getAndValidateProperty(final String property) {
		final String value = properties.getUnchecked(property);
		if (StringUtils.isBlank(value)) {
			throw new InvalidPropertyValueException("The property " + property
					+ " has an invalid blank value. It is highly probable that the portlet needs to be initialized by restarting WS-PGRADE.");
		}
		return value;
	}

	@Override
	public PoolProperties getPoolProperties() {
		initialSettings.setUrl(getURL());
		initialSettings.setDriverClassName(getDriver());
		initialSettings.setUsername(getUsername());
		initialSettings.setPassword(getPassword());

		return initialSettings;
	}
}
