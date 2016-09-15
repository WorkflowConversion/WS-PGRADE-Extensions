package com.workflowconversion.importer.guse.servlet;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.jdbc.AbandonedConnectionCleanupThread;
import com.workflowconversion.importer.guse.Settings;
import com.workflowconversion.importer.guse.appdb.ApplicationProvider;
import com.workflowconversion.importer.guse.appdb.config.DatabaseConfiguration;
import com.workflowconversion.importer.guse.exception.ApplicationException;
import com.workflowconversion.importer.guse.middleware.MiddlewareProvider;
import com.workflowconversion.importer.guse.permission.PermissionManager;
import com.workflowconversion.importer.guse.text.StringSimilarityAlgorithm;
import com.workflowconversion.importer.guse.text.StringSimilaritySettings;

/**
 * Class that deals with cleaning/init up of this webapp.
 * 
 * @author delagarza
 *
 */
public class WorkflowImporterContextListener implements ServletContextListener {

	private static final Logger LOG = LoggerFactory.getLogger(WorkflowImporterContextListener.class);

	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {
		LOG.info("Performing initialization tasks for WorkflowImporterPortlet");

		// perform the bindings with ApplicationSettings
		final DatabaseConfiguration dbConfig = newInstance(
				extractInitParam("databaseConfiguration.implementation", servletContextEvent),
				DatabaseConfiguration.class);
		final PermissionManager permissionManager = newInstance(
				extractInitParam("permissionManager.implementation", servletContextEvent), PermissionManager.class,
				extractInitParam("permissions.location", servletContextEvent));
		final Collection<ApplicationProvider> applicationProviders = extractApplicationProviders(servletContextEvent);
		final String vaadinTheme = extractInitParam("vaadinTheme", servletContextEvent);
		final PoolProperties poolProperties = extractPoolProperties(servletContextEvent.getServletContext());
		final StringSimilaritySettings stringSimilaritySettings = extractStringSimilaritySettings(
				servletContextEvent.getServletContext());
		final MiddlewareProvider middlewareProvider = newInstance(
				extractInitParam("middlewareProvider.implementation", servletContextEvent), MiddlewareProvider.class);

		final Settings.Builder settingsBuilder = new Settings.Builder();

		settingsBuilder.setVaadinTheme(vaadinTheme).setDatabaseConfiguration(dbConfig)
				.setPermissionManager(permissionManager).setApplicationProviders(applicationProviders)
				.setPoolProperties(poolProperties).setStringSimilaritySettings(stringSimilaritySettings)
				.setMiddlewareProvider(middlewareProvider);
		Settings.setInstance(settingsBuilder.newSettings());
	}

	private String extractInitParam(final String paramName, final ServletContextEvent servletContextEvent) {
		return servletContextEvent.getServletContext().getInitParameter(paramName);
	}

	private <T> T newInstance(final String className, final Class<T> interfaceClass,
			final Object... constructorParams) {
		try {
			// find the class
			@SuppressWarnings("unchecked")
			final Class<? extends T> implClass = (Class<T>) Class.forName(className.trim());
			// find the constructor
			final Constructor<? extends T> constructor;
			if (constructorParams.length == 0) {
				// default constructor
				constructor = implClass.getConstructor();
			} else {
				final Class<?>[] constructorParameterClasses = new Class<?>[constructorParams.length];
				for (int i = 0; i < constructorParams.length; i++) {
					constructorParameterClasses[i] = constructorParams[i].getClass();
				}
				constructor = implClass.getConstructor(constructorParameterClasses);
			}
			if (LOG.isInfoEnabled()) {
				LOG.info("Obtaining a new instance of " + implClass.getName() + " using the constructor "
						+ constructor.toGenericString() + " and the following parameters "
						+ Arrays.toString(constructorParams));
			}
			return constructor.newInstance(constructorParams);
		} catch (ClassNotFoundException | IllegalAccessException | InstantiationException | InvocationTargetException
				| NoSuchMethodException e) {
			// make it all fail, it's not safe to keep going
			throw new ApplicationException("Could not instantiate class with class name " + className, e);
		}
	}

	private Collection<ApplicationProvider> extractApplicationProviders(final ServletContextEvent servletContextEvent) {
		final Collection<ApplicationProvider> applicationProviders = new LinkedList<ApplicationProvider>();
		final String[] providerClassNames = extractInitParam("internal.applicationProviders.implementations",
				servletContextEvent).trim().split(",");
		for (final String providerClassName : providerClassNames) {
			applicationProviders.add(newInstance(providerClassName.trim(), ApplicationProvider.class));
		}
		return applicationProviders;
	}

	// extracts cut-off score and StringSimilarity algorithm to use
	private StringSimilaritySettings extractStringSimilaritySettings(ServletContext servletContext) {
		final StringSimilaritySettings.Builder builder = new StringSimilaritySettings.Builder();
		builder.setCutOffValue(Double.parseDouble(servletContext.getInitParameter("cutOff.filter.value")))
				.setAlgorithm(newInstance(servletContext.getInitParameter("stringSimilarityAlgorithm.implementation"),
						StringSimilarityAlgorithm.class));
		return builder.newStringSimilaritySettings();
	}

	// extracts pool properties from web.xml
	private PoolProperties extractPoolProperties(final ServletContext servletContext) {
		final PoolProperties poolProperties = new PoolProperties();
		poolProperties.setValidationInterval(Long.valueOf(servletContext.getInitParameter("pool.validationInterval")));
		poolProperties.setTimeBetweenEvictionRunsMillis(
				Integer.valueOf(servletContext.getInitParameter("pool.timeBetweenEviction")));
		poolProperties.setMaxActive(Integer.valueOf(servletContext.getInitParameter("pool.maxActive")));
		poolProperties.setMaxIdle(Integer.valueOf(servletContext.getInitParameter("pool.maxIdle")));
		poolProperties.setMinIdle(Integer.valueOf(servletContext.getInitParameter("pool.minIdle")));
		poolProperties.setInitialSize(Integer.valueOf(servletContext.getInitParameter("pool.initialSize")));
		poolProperties.setMaxWait(Integer.valueOf(servletContext.getInitParameter("pool.maxWait")));
		return poolProperties;
	}

	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
		LOG.info("Performing cleanup tasks for WorkflowImporterPortlet");
		Settings.clearInstance();
		// shutdown mysql cleanup thread
		try {
			AbandonedConnectionCleanupThread.shutdown();
		} catch (InterruptedException e) {
			LOG.error("could not shutdown MySQL's Cleanup Thread: " + e.getMessage());
			// but there's not much we can do anyway...
		}
	}
}
