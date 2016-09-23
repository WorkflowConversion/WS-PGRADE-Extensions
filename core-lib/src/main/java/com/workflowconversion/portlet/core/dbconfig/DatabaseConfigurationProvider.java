package com.workflowconversion.portlet.core.dbconfig;

import org.apache.tomcat.jdbc.pool.PoolProperties;

/**
 * Interface to obtain database configuration values (credentials, pool properties). Implementations can decide whether
 * to take these values from a properties file, hardcode them or get them via the gUSE webservices.
 * 
 * @author delagarza
 *
 */
public interface DatabaseConfigurationProvider {

	/**
	 * Obtains a fully configured pool properties.
	 * 
	 * @return a configured {@link PoolProperties} instance.
	 */
	public PoolProperties getPoolProperties();
}
