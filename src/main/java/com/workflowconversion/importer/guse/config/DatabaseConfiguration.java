package com.workflowconversion.importer.guse.config;

/**
 * Interface to obtain database configuration values.
 * 
 * @author delagarza
 *
 */
public interface DatabaseConfiguration {

	/**
	 * Retrieves the database driver (e.g., {@code com.mysql.jdbc.Driver}).
	 * 
	 * @return The database driver.
	 */
	public String getDriver();

	/**
	 * Retrieves the database JDBC url (e.g., {@code jdbc:mysql://localhost:3306/user})
	 * 
	 * @return The databse url.
	 */
	public String getURL();

	/**
	 * Retrieves the username to access the database.
	 * 
	 * @return The database username.
	 */
	public String getUsername();

	/**
	 * Retrieves the password to access the database.
	 * 
	 * @return The database password.
	 */
	public String getPassword();

	/**
	 * Returns {@code true} if all of the needed configuration values could be retrieved and are valid. Certain
	 * implementations might choose to provide configuration values via a webservice or an XML file.
	 * 
	 * @return {@code true} if all of the needed configuration values are present.
	 */
	public boolean isValid();
}
