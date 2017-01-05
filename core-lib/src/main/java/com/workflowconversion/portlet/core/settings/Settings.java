package com.workflowconversion.portlet.core.settings;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.Validate;

import com.workflowconversion.portlet.core.exception.ApplicationException;
import com.workflowconversion.portlet.core.middleware.MiddlewareProvider;
import com.workflowconversion.portlet.core.resource.ResourceProvider;
import com.workflowconversion.portlet.core.text.StringSimilaritySettings;
import com.workflowconversion.portlet.core.validation.PortletSanityCheck;
import com.workflowconversion.portlet.core.workflow.WorkflowExporterFactory;
import com.workflowconversion.portlet.core.workflow.WorkflowProviderFactory;

/**
 * Simple class that provides application settings that are configured when the application is started up.
 * 
 * @author delagarza
 *
 */
public class Settings implements Serializable {

	private static final long serialVersionUID = -1344935177312215690L;
	private final PortletSanityCheck portletSanityCheck;
	private final Collection<ResourceProvider> applicationProviders;
	private final StringSimilaritySettings stringSimilaritySettings;
	private final MiddlewareProvider middlewareProvider;
	private final Class<? extends WorkflowProviderFactory> workflowProviderFactoryClass;
	private final Class<? extends WorkflowExporterFactory> workflowExporterFactoryClass;
	private final String workflowStagingAreaPath;
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
	 * Obtains the application providers.
	 * 
	 * @return The application providers.
	 */
	public Collection<ResourceProvider> getApplicationProviders() {
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

	/**
	 * @return the workflow exporter factory.
	 */
	public WorkflowExporterFactory getWorkflowExporterFactory() {
		try {
			return workflowExporterFactoryClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new ApplicationException("Could not instantiate a new WorkflowExporterFactory", e);
		}
	}

	/**
	 * @return the workflow provider factory.
	 */
	public WorkflowProviderFactory getWorkflowProviderFactory() {
		try {
			return workflowProviderFactoryClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new ApplicationException("Could not instantiate a new WorkflowProviderFactory", e);
		}
	}

	/**
	 * @return the workflow staging area path.
	 */
	public String getWorkflowStagingAreaPath() {
		return workflowStagingAreaPath;
	}

	private Settings(final PortletSanityCheck portletSanityCheck,
			final Collection<ResourceProvider> applicationProviders,
			final StringSimilaritySettings stringSimilaritySettings, final MiddlewareProvider middlewareProvider,
			final Class<? extends WorkflowExporterFactory> workflowExporterFactoryClass,
			final Class<? extends WorkflowProviderFactory> workflowProviderFactoryClass,
			final String workflowStagingAreaPath) {
		Validate.notNull(portletSanityCheck,
				"portletSanityCheck cannot be null, please use the Builder.withPortletSanityCheck() method to set a non-null value");
		Validate.notEmpty(applicationProviders,
				"applicationProviders cannot be null or empty, please use the Builder.withApplicationProviders() method to set a proper value");
		Validate.notNull(stringSimilaritySettings,
				"stringSimilaritySettings cannot be null, please use the Builder.withStringSimilaritySettings() method to set a non-null value");
		Validate.notNull(middlewareProvider,
				"middlewareProvider cannot be null, please use the Builder.withMiddlewareProvider() method to set a non-null value");
		Validate.notNull(workflowProviderFactoryClass,
				"workflowProviderFactoryClass cannot be null, please use the Builder.withWorkflowProviderFactoryClass() method to set a non-null value");
		Validate.notNull(workflowExporterFactoryClass,
				"workflowExporterFactoryClass cannot be null, please use the Builder.withWorkflowExporterFactoryClass() method to set a non-null value");
		this.applicationProviders = Collections.unmodifiableCollection(applicationProviders);
		this.portletSanityCheck = portletSanityCheck;
		this.stringSimilaritySettings = stringSimilaritySettings;
		this.middlewareProvider = middlewareProvider;
		this.workflowExporterFactoryClass = workflowExporterFactoryClass;
		this.workflowProviderFactoryClass = workflowProviderFactoryClass;
		// this could very well be empty, so no need to validate
		this.workflowStagingAreaPath = workflowStagingAreaPath;
	}

	/**
	 * Builder class for {@link Settings}.
	 * 
	 * @author delagarza
	 *
	 */
	public static class Builder {
		private Collection<ResourceProvider> applicationProviders;
		private PortletSanityCheck portletSanityCheck;
		private StringSimilaritySettings stringSimilaritySettings;
		private MiddlewareProvider middlewareProvider;
		private Class<? extends WorkflowProviderFactory> workflowProviderFactoryClass;
		private Class<? extends WorkflowExporterFactory> workflowExporterFactoryClass;
		private String workflowStagingAreaPath;

		/**
		 * Sets the application providers.
		 * 
		 * @param applicationProviders
		 *            The collection of application providers.
		 * @return the instance of {@code this} {@link Builder}.
		 */
		public Builder withApplicationProviders(final Collection<ResourceProvider> applicationProviders) {
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
		public Builder withStringSimilaritySettings(final StringSimilaritySettings stringSimilaritySettings) {
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
		public Builder withMiddlewareProvider(final MiddlewareProvider middlewareProvider) {
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
		public Builder withPortletSanityCheck(final PortletSanityCheck portletSanityCheck) {
			this.portletSanityCheck = portletSanityCheck;
			return this;
		}

		/**
		 * Sets the workflow exporter factory.
		 * 
		 * @param workflowExporterFactoryClass
		 *            the factory.
		 * @return the instance of {@code this} {@link Builder}.
		 */
		public Builder withWorkflowExporterFactoryClass(
				final Class<? extends WorkflowExporterFactory> workflowExporterFactoryClass) {
			this.workflowExporterFactoryClass = workflowExporterFactoryClass;
			return this;
		}

		/**
		 * Sets the workflow provider factory.
		 * 
		 * @param workflowProviderFactoryClass
		 *            the workflow provider factory.
		 * @return the instance of {@code this} {@link Builder}.
		 */
		public Builder withWorkflowProviderFactoryClass(
				final Class<? extends WorkflowProviderFactory> workflowProviderFactoryClass) {
			this.workflowProviderFactoryClass = workflowProviderFactoryClass;
			return this;
		}

		/**
		 * Sets the path of the workflow staging area to use.
		 * 
		 * @param workflowStagingAreaPath
		 *            the path to use.
		 * @return the instance of {@code this} {@link Builder}.
		 */
		public Builder withWorkflowStagingAreaPath(final String workflowStagingAreaPath) {
			this.workflowStagingAreaPath = workflowStagingAreaPath;
			return this;
		}

		/**
		 * Builds a new {@link Settings}.
		 * 
		 * @return a new instance of an {@link Settings}.
		 */
		public Settings newSettings() {
			return new Settings(portletSanityCheck, applicationProviders, stringSimilaritySettings, middlewareProvider,
					workflowExporterFactoryClass, workflowProviderFactoryClass, workflowStagingAreaPath);
		}
	}
}
