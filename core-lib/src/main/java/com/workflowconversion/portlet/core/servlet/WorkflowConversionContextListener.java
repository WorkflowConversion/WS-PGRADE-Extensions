package com.workflowconversion.portlet.core.servlet;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.jdbc.AbandonedConnectionCleanupThread;
import com.workflowconversion.portlet.core.execution.JobExecutionPropertiesHandler;
import com.workflowconversion.portlet.core.execution.impl.DefaultJobExecutionPropertiesHandler;
import com.workflowconversion.portlet.core.middleware.MiddlewareProvider;
import com.workflowconversion.portlet.core.middleware.impl.InMemoryMockMiddlewareProvider;
import com.workflowconversion.portlet.core.middleware.impl.WSPGRADEMiddlewareProvider;
import com.workflowconversion.portlet.core.resource.ResourceProvider;
import com.workflowconversion.portlet.core.resource.impl.InMemoryMockResourceProvider;
import com.workflowconversion.portlet.core.resource.impl.JAXBResourceDatabase;
import com.workflowconversion.portlet.core.resource.impl.UnicoreResourceProvider;
import com.workflowconversion.portlet.core.settings.Settings;
import com.workflowconversion.portlet.core.validation.PortletSanityCheck;
import com.workflowconversion.portlet.core.validation.impl.GUSEPortletSanityCheck;
import com.workflowconversion.portlet.core.validation.impl.MockPortletSanityCheck;
import com.workflowconversion.portlet.core.workflow.WorkflowExporterFactory;
import com.workflowconversion.portlet.core.workflow.WorkflowManagerFactory;
import com.workflowconversion.portlet.core.workflow.impl.DefaultWorkflowExporterFactory;
import com.workflowconversion.portlet.core.workflow.impl.DefaultWorkflowManagerFactory;
import com.workflowconversion.portlet.core.workflow.impl.MockWorkflowExporterFactory;
import com.workflowconversion.portlet.core.workflow.impl.MockWorkflowManagerFactory;

/**
 * Class that deals with cleaning/init up of webapps by reading configuration values from the servlet descriptor
 * ({@code web.xml}).
 * 
 * @author delagarza
 *
 */
public class WorkflowConversionContextListener implements ServletContextListener {

	private static final Logger LOG = LoggerFactory.getLogger(WorkflowConversionContextListener.class);

	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {
		LOG.info("Performing initialization tasks for a com.workflowconversion portlet");
		// warn about using mocks
		if (useMocks(servletContextEvent)) {
			LOG.warn("#############################===WARNING===#############################");
			LOG.warn(
					"############################# THIS PORTLET HAS BEEN SET IN DEVELOPMENT MODE! #############################");
			LOG.warn(
					"############################# MAKE SURE THIS IS NOT A PRODUCTION INSTANCE! #############################");
			LOG.warn(
					"############################# UPDATE THE 'ui.development.mode' PARAMETER IN THE SERVLET DESCRIPTOR (web.xml) TO CHANGE THIS PORTLET'S BEHAVIOR #############################");
			LOG.warn(
					"############################# A RESTART OF THE WS-PGRADE INSTANCE IS REQUIRED FOR THE CHANGE TO TAKE EFFECT #############################");
			LOG.warn("#############################################################");
		}

		final MiddlewareProvider middlewareProvider = extractMiddlewareProvider(servletContextEvent);
		final Collection<ResourceProvider> applicationProviders = extractResourceProviders(servletContextEvent,
				middlewareProvider);
		final PortletSanityCheck portletSanityCheck = extractPortletSanityCheck(servletContextEvent);
		final Class<? extends WorkflowManagerFactory> workflowManagerFactoryClass = extractWorkflowManagerFactoryClass(
				servletContextEvent);
		final Class<? extends WorkflowExporterFactory> workflowExporterFactoryClass = extractWorkflowExporterFactoryClass(
				servletContextEvent);
		final String workflowStagingAreaPath = extractInitParam("workflow.stagingArea.path", servletContextEvent);
		// make sure that the staging area exists
		if (StringUtils.isNotBlank(workflowStagingAreaPath)) {
			LOG.info("Creating workflow staging area folder");
			new File(workflowStagingAreaPath).mkdirs();
		}
		// use the same instance, the default implementation is thread-safe
		final JobExecutionPropertiesHandler jobExecutionPropertiesHandler = new DefaultJobExecutionPropertiesHandler();

		final Settings.Builder settingsBuilder = new Settings.Builder();

		settingsBuilder.withApplicationProviders(applicationProviders).withMiddlewareProvider(middlewareProvider)
				.withPortletSanityCheck(portletSanityCheck).withWorkflowStagingAreaPath(workflowStagingAreaPath)
				.withWorkflowManagerFactoryClass(workflowManagerFactoryClass)
				.withWorkflowExporterFactoryClass(workflowExporterFactoryClass)
				.withJobExecutionPropertiesHandler(jobExecutionPropertiesHandler);

		Settings.setInstance(settingsBuilder.newSettings());
	}

	private Class<? extends WorkflowExporterFactory> extractWorkflowExporterFactoryClass(
			final ServletContextEvent servletContextEvent) {
		if (useMocks(servletContextEvent)) {
			return MockWorkflowExporterFactory.class;
		}
		return DefaultWorkflowExporterFactory.class;
	}

	private Class<? extends WorkflowManagerFactory> extractWorkflowManagerFactoryClass(
			final ServletContextEvent servletContextEvent) {
		if (useMocks(servletContextEvent)) {
			return MockWorkflowManagerFactory.class;
		}
		return DefaultWorkflowManagerFactory.class;
	}

	private String extractInitParam(final String paramName, final ServletContextEvent servletContextEvent) {
		return servletContextEvent.getServletContext().getInitParameter(paramName);
	}

	private Collection<ResourceProvider> extractResourceProviders(final ServletContextEvent servletContextEvent,
			final MiddlewareProvider middlewareProvider) {
		// find out if we are using mocks
		final Collection<ResourceProvider> applicationProviders = new LinkedList<ResourceProvider>();
		if (useMocks(servletContextEvent)) {
			applicationProviders
					.add(new InMemoryMockResourceProvider("Editable mock app provider", middlewareProvider, true));
			applicationProviders
					.add(new InMemoryMockResourceProvider("Read-only mock app provider", middlewareProvider, false));
		} else {
			applicationProviders
					.add(new JAXBResourceDatabase(extractInitParam("resource.xmlFile.location", servletContextEvent)));
			applicationProviders.add(new UnicoreResourceProvider(middlewareProvider));
		}
		return Collections.unmodifiableCollection(applicationProviders);
	}

	private PortletSanityCheck extractPortletSanityCheck(final ServletContextEvent servletContextEvent) {
		final PortletSanityCheck portletSanityCheck;
		if (useMocks(servletContextEvent)) {
			portletSanityCheck = new MockPortletSanityCheck();
		} else {
			portletSanityCheck = new GUSEPortletSanityCheck();
		}
		return portletSanityCheck;
	}

	private boolean useMocks(final ServletContextEvent servletContextEvent) {
		return Boolean.parseBoolean(extractInitParam("ui.development.mode", servletContextEvent));
	}

	private MiddlewareProvider extractMiddlewareProvider(final ServletContextEvent servletContextEvent) {
		if (useMocks(servletContextEvent)) {
			return new InMemoryMockMiddlewareProvider();
		} else {
			return new WSPGRADEMiddlewareProvider();
		}
	}

	@Override
	public void contextDestroyed(final ServletContextEvent servletContextEvent) {
		LOG.info("Performing cleanup tasks for a com.workflowconversion portlet");
		Settings.clearInstance();
		// shutdown mysql cleanup thread
		try {
			AbandonedConnectionCleanupThread.shutdown();
		} catch (final InterruptedException e) {
			LOG.error("could not shutdown MySQL's Cleanup Thread: " + e.getMessage());
			// but there's not much we can do anyway...
		}
	}
}
