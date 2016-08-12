package com.workflowconversion.importer.guse;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.workflowconversion.importer.guse.appdb.ApplicationProvider;
import com.workflowconversion.importer.guse.config.DatabaseConfiguration;
import com.workflowconversion.importer.guse.permission.PermissionManager;

/**
 * Simple class that provides application settings that are configured when the application is started up.
 * 
 * @author delagarza
 *
 */
public class Settings {

	private final String vaadinTheme;
	private final DatabaseConfiguration databaseConfiguration;
	private final PermissionManager permissionManager;
	private final Collection<ApplicationProvider> applicationProviders;
	private static Settings INSTANCE;

	/**
	 * Sets the current instance.
	 * 
	 * @param instance
	 *            The current instance.
	 */
	public synchronized static void setInstance(final Settings instance) {
		if (INSTANCE != null) {
			throw new IllegalStateException(
					"instance is not null, please use the clearInstance() method before setting a new instance.");
		}
		INSTANCE = instance;
	}

	/**
	 * Clears the current instance.
	 */
	public synchronized static void clearInstance() {
		INSTANCE = null;
	}

	/**
	 * Obtains the current instance.
	 * 
	 * @return The current instance.
	 */
	public synchronized static Settings getInstance() {
		if (INSTANCE == null) {
			throw new IllegalStateException(
					"intance is null, please use the setInstance() method before using invoking getInstance().");
		}
		return INSTANCE;
	}

	/**
	 * Obtains the Vaadin theme.
	 * 
	 * @return The Vaadin theme.
	 */
	public String getVaadinTheme() {
		return vaadinTheme;
	}

	/**
	 * Obtains the database configuration.
	 * 
	 * @return The database configuration.
	 */
	public DatabaseConfiguration getDatabaseConfiguration() {
		return databaseConfiguration;
	}

	/**
	 * Obtains the permission manager.
	 * 
	 * @return The permission manager.
	 */
	public PermissionManager getPermissionManager() {
		return permissionManager;
	}

	/**
	 * Obtains the application providers.
	 * 
	 * @return The application providers.
	 */
	public Collection<ApplicationProvider> getApplicationProviders() {
		return this.applicationProviders;
	}

	private Settings(final String vaadinTheme, final DatabaseConfiguration databaseConfiguration,
			final PermissionManager permissionManager, final Collection<ApplicationProvider> applicationProviders) {
		Validate.isTrue(StringUtils.isNotBlank(vaadinTheme),
				"vaadinTheme cannot be null or empty, please use the Builder.setVaadinTheme() method to set a non-blank value");
		Validate.notNull(databaseConfiguration,
				"databaseConfiguration cannot be null, please use the Builder.setDatabaseConfiguration() method to set a non-null value");
		Validate.notNull(permissionManager,
				"permissionManager cannot be null, please use the Builder.setPermissionManager() method to set a non-null value");
		Validate.notEmpty(applicationProviders,
				"applicationProviders cannot be null or empty, please use the Builder.setApplicationProviders() method to set a proper value");
		this.vaadinTheme = vaadinTheme;
		this.databaseConfiguration = databaseConfiguration;
		this.permissionManager = permissionManager;
		this.applicationProviders = Collections.unmodifiableCollection(applicationProviders);
	}

	/**
	 * Builder class for {@link Settings}.
	 * 
	 * @author delagarza
	 *
	 */
	public static class Builder {
		private String vaadinTheme;
		private DatabaseConfiguration databaseConfiguration;
		private PermissionManager permissionManager;
		private Collection<ApplicationProvider> applicationProviders;

		/**
		 * Sets the vaadin theme.
		 * 
		 * @param vaadinTheme
		 *            The vaadin theme.
		 * @return the instance of {@code this} {@link Builder}.
		 */
		public Builder setVaadinTheme(final String vaadinTheme) {
			Validate.isTrue(StringUtils.isNotBlank(vaadinTheme), "vaadinTheme cannot be blank, null or empty.");
			this.vaadinTheme = vaadinTheme;
			return this;
		}

		/**
		 * Sets the database configuration.
		 * 
		 * @param databaseConfiguration
		 *            The database configuration.
		 * @return the instance of {@code this} {@link Builder}.
		 */
		public Builder setDatabaseConfiguration(final DatabaseConfiguration databaseConfiguration) {
			Validate.notNull(databaseConfiguration, "databaseConfiguration cannot be null.");
			this.databaseConfiguration = databaseConfiguration;
			return this;
		}

		/**
		 * Sets the permission manager.
		 * 
		 * @param permissionManager
		 *            The permission manager.
		 * @return the instance of {@code this} {@link Builder}.
		 */
		public Builder setPermissionManager(final PermissionManager permissionManager) {
			Validate.notNull(permissionManager, "permissionManager cannot be null.");
			this.permissionManager = permissionManager;
			return this;
		}

		/**
		 * Sets the application providers.
		 * 
		 * @param applicationProviders
		 *            The collection of application providers.
		 * @return the instance of {@code this} {@link Builder}.
		 */
		public Builder setApplicationProviders(final Collection<ApplicationProvider> applicationProviders) {
			Validate.notEmpty(applicationProviders, "applicationProviders cannot be null or empty.");
			this.applicationProviders = applicationProviders;
			return this;
		}

		/**
		 * Builds a new {@link Settings}.
		 * 
		 * @return a new instance of an {@link Settings}.
		 */
		public Settings newApplicationSettings() {
			return new Settings(vaadinTheme, databaseConfiguration, permissionManager, applicationProviders);
		}
	}

}
