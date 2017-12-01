package com.workflowconversion.portlet.core;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

import org.apache.commons.lang.Validate;

import com.workflowconversion.portlet.core.middleware.MiddlewareProvider;
import com.workflowconversion.portlet.core.resource.ResourceProvider;
import com.workflowconversion.portlet.core.validation.PortletSanityCheck;

/**
 * Simple class that provides application settings that are configured when the application is started up.
 * 
 * @author delagarza
 *
 */
public class Settings implements Serializable {

	private static final long serialVersionUID = -1344935177312215690L;
	private final PortletSanityCheck portletSanityCheck;
	private final Collection<ResourceProvider> resourceProviders;
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
	 * Obtains the application providers.
	 * 
	 * @return The application providers.
	 */
	public Collection<ResourceProvider> getResourceProviders() {
		return this.resourceProviders;
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

	private Settings(final PortletSanityCheck portletSanityCheck, final Collection<ResourceProvider> resourceProviders,
			final MiddlewareProvider middlewareProvider) {
		Validate.notNull(portletSanityCheck,
				"portletSanityCheck cannot be null, please use the Builder.withPortletSanityCheck() method to set a non-null value");
		Validate.notEmpty(resourceProviders,
				"resourceProviders cannot be null or empty, please use the Builder.withApplicationProviders() method to set a proper value");
		Validate.notNull(middlewareProvider,
				"middlewareProvider cannot be null, please use the Builder.withMiddlewareProvider() method to set a non-null value");
		this.resourceProviders = Collections.unmodifiableCollection(resourceProviders);
		this.portletSanityCheck = portletSanityCheck;
		this.middlewareProvider = middlewareProvider;
	}

	/**
	 * Builder class for {@link Settings}.
	 * 
	 * @author delagarza
	 *
	 */
	public static class Builder {
		private Collection<ResourceProvider> resourceProviders;
		private PortletSanityCheck portletSanityCheck;
		private MiddlewareProvider middlewareProvider;

		/**
		 * Sets the application providers.
		 * 
		 * @param resourceProviders
		 *            The collection of resource providers.
		 * @return the instance of {@code this} {@link Builder}.
		 */
		public Builder withResourceProviders(final Collection<ResourceProvider> resourceProviders) {
			this.resourceProviders = resourceProviders;
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
		 * Builds a new {@link Settings}.
		 * 
		 * @return a new instance of an {@link Settings}.
		 */
		public Settings newSettings() {
			return new Settings(portletSanityCheck, resourceProviders, middlewareProvider);
		}
	}
}
