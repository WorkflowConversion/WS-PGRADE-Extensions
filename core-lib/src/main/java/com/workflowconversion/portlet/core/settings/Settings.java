package com.workflowconversion.portlet.core.settings;

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.workflowconversion.portlet.core.app.ApplicationProvider;
import com.workflowconversion.portlet.core.dbconfig.DatabaseConfigurationProvider;
import com.workflowconversion.portlet.core.middleware.MiddlewareProvider;
import com.workflowconversion.portlet.core.text.StringSimilaritySettings;
import com.workflowconversion.portlet.core.validation.PortletSanityCheck;

/**
 * Simple class that provides application settings that are configured when the application is started up.
 * 
 * @author delagarza
 *
 */
public class Settings {

	private final String vaadinTheme;
	private final DatabaseConfigurationProvider databaseConfigurationProvider;
	private final PortletSanityCheck portletSanityCheck;
	private final Collection<ApplicationProvider> applicationProviders;
	private final StringSimilaritySettings stringSimilaritySettings;
	private final MiddlewareProvider middlewareProvider;
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
	 * Obtains the database configuration provider.
	 * 
	 * @return The database configuration provider.
	 */
	public DatabaseConfigurationProvider getDatabaseConfigurationProvider() {
		return databaseConfigurationProvider;
	}

	/**
	 * Obtains the application providers.
	 * 
	 * @return The application providers.
	 */
	public Collection<ApplicationProvider> getApplicationProviders() {
		return this.applicationProviders;
	}

	/**
	 * @return the string similarity settings.
	 */
	public StringSimilaritySettings getStringSimilaritySettings() {
		return stringSimilaritySettings;
	}

	/**
	 * @return the middleware provider.
	 */
	public MiddlewareProvider getMiddlewareProvider() {
		return middlewareProvider;
	}

	/**
	 * @return the portletSanityCheck
	 */
	public PortletSanityCheck getPortletSanityCheck() {
		return portletSanityCheck;
	}

	private Settings(final String vaadinTheme, final DatabaseConfigurationProvider databaseConfigurationProvider,
			final PortletSanityCheck portletSanityCheck, final Collection<ApplicationProvider> applicationProviders,
			final StringSimilaritySettings stringSimilaritySettings, final MiddlewareProvider middlewareProvider) {
		Validate.isTrue(StringUtils.isNotBlank(vaadinTheme),
				"vaadinTheme cannot be null or empty, please use the Builder.setVaadinTheme() method to set a non-blank value");
		Validate.notNull(databaseConfigurationProvider,
				"databaseConfigurationProvider cannot be null, please use the Builder.setDatabaseConfigurationProvider() method to set a non-null value");
		Validate.notNull(portletSanityCheck,
				"portletSanityCheck cannot be null, please use the Builder.setPortletSanityCheck() method to set a non-null value");
		Validate.notEmpty(applicationProviders,
				"applicationProviders cannot be null or empty, please use the Builder.setApplicationProviders() method to set a proper value");
		Validate.notNull(stringSimilaritySettings,
				"stringSimilaritySettings cannot be null, please use the Builder.setStringSimilaritySettings() method to set a non-null value");
		Validate.notNull(middlewareProvider,
				"middlewareProvider cannot be null, please use the Builder.setMiddlewareProvider() method to set a non-null value");
		this.vaadinTheme = vaadinTheme;
		this.databaseConfigurationProvider = databaseConfigurationProvider;
		this.applicationProviders = Collections.unmodifiableCollection(applicationProviders);
		this.portletSanityCheck = portletSanityCheck;
		this.stringSimilaritySettings = stringSimilaritySettings;
		this.middlewareProvider = middlewareProvider;
	}

	/**
	 * Builder class for {@link Settings}.
	 * 
	 * @author delagarza
	 *
	 */
	public static class Builder {
		private String vaadinTheme;
		private DatabaseConfigurationProvider databaseConfigurationProvider;
		private Collection<ApplicationProvider> applicationProviders;
		private PortletSanityCheck portletSanityCheck;
		private StringSimilaritySettings stringSimilaritySettings;
		private MiddlewareProvider middlewareProvider;

		/**
		 * Sets the vaadin theme.
		 * 
		 * @param vaadinTheme
		 *            The vaadin theme.
		 * @return the instance of {@code this} {@link Builder}.
		 */
		public Builder setVaadinTheme(final String vaadinTheme) {
			this.vaadinTheme = vaadinTheme;
			return this;
		}

		/**
		 * Sets the database configuration provider.
		 * 
		 * @param databaseConfigurationProvider
		 *            The database configuration provider.
		 * @return the instance of {@code this} {@link Builder}.
		 */
		public Builder setDatabaseConfigurationProvider(
				final DatabaseConfigurationProvider databaseConfigurationProvider) {
			this.databaseConfigurationProvider = databaseConfigurationProvider;
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
			this.applicationProviders = applicationProviders;
			return this;
		}

		/**
		 * Sets the string similarity settings.
		 * 
		 * @param stringSimilaritySettings
		 *            The string similarity settings.
		 * @return the instance of {@code this} {@link Builder}.
		 */
		public Builder setStringSimilaritySettings(final StringSimilaritySettings stringSimilaritySettings) {
			this.stringSimilaritySettings = stringSimilaritySettings;
			return this;
		}

		/**
		 * Sets the middleware provider.
		 * 
		 * @param middlewareProvider
		 *            the middleware provider.
		 * @return the instance of {@code this} {@link Builder}.
		 */
		public Builder setMiddlewareProvider(final MiddlewareProvider middlewareProvider) {
			this.middlewareProvider = middlewareProvider;
			return this;
		}

		/**
		 * Sets the portlet sanity check.
		 * 
		 * @param portletSanityCheck
		 *            the portlet sanity check.
		 * @return the instance of {@code this} {@link Builder}.
		 */
		public Builder setPortletSanityCheck(final PortletSanityCheck portletSanityCheck) {
			this.portletSanityCheck = portletSanityCheck;
			return this;
		}

		/**
		 * Builds a new {@link Settings}.
		 * 
		 * @return a new instance of an {@link Settings}.
		 */
		public Settings newSettings() {
			return new Settings(vaadinTheme, databaseConfigurationProvider, portletSanityCheck, applicationProviders,
					stringSimilaritySettings, middlewareProvider);
		}
	}
}
