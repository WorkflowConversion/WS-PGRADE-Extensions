package com.workflowconversion.portlet.core.search;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workflowconversion.portlet.core.exception.InvalidFieldException;
import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.core.resource.FormField;
import com.workflowconversion.portlet.core.resource.Queue;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.core.resource.ResourceProvider;

/**
 * Utility class to find applications, queues.
 * 
 * @author delagarza
 *
 */
public class AssetFinder {

	private final static Logger LOG = LoggerFactory.getLogger(AssetFinder.class);

	private final Map<FormField, String> resourceFields;
	private final Map<FormField, String> applicationFields;
	private final Map<FormField, String> queueFields;

	private final Collection<FormFieldHandler> handlers;
	private final Collection<ResourceProvider> resourceProviders;

	/**
	 * Constructor.
	 */
	public AssetFinder() {
		resourceFields = new TreeMap<FormField, String>();
		applicationFields = new TreeMap<FormField, String>();
		queueFields = new TreeMap<FormField, String>();
		resourceProviders = new LinkedList<ResourceProvider>();

		handlers = new LinkedList<FormFieldHandler>();
		handlers.add(new ApplicationFieldHandler());
		handlers.add(new ResourceFieldHandler());
		handlers.add(new QueueFieldHandler());
	}

	/**
	 * 
	 * @param resourceProviders
	 */
	public void init(final Collection<ResourceProvider> resourceProviders) {
		Validate.notEmpty(resourceProviders, "resourceProviders cannot be null or empty.");
		this.resourceProviders.addAll(resourceProviders);
		clearAllFields();
	}

	/**
	 * Clears all fields.
	 */
	public void clearAllFields() {
		LOG.debug("Clearing fields");
		resourceFields.clear();
		applicationFields.clear();
		queueFields.clear();
	}

	public Resource findResource() {
		final String name = StringUtils.trimToNull(resourceFields.get(Resource.Field.Name));
		final String type = StringUtils.trimToNull(resourceFields.get(Resource.Field.Type));
		if (LOG.isDebugEnabled()) {
			LOG.debug("Searching resource with name=" + name + " and type=" + type);
		}
		if (name == null || type == null) {
			return null;
		}
		for (final ResourceProvider resourceProvider : resourceProviders) {
			final Resource resource = resourceProvider.getResource(name, type);
			if (resource != null) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Found resource with name=" + name + ", type=" + type + " on provider "
							+ resourceProvider.getName());
				}
				return resource;
			}
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Could not find a resource with name=" + name + ", type=" + type);
		}
		return null;
	}

	/**
	 * Finds an application based on the fields that were previously set.
	 * 
	 * @param resource
	 *            the resource on which the application resides.
	 * 
	 * @return an application that matches or {@code null} if no application was found.
	 */
	public Application findApplication(final Resource resource) {
		final String name = StringUtils.trimToNull(applicationFields.get(Application.Field.Name));
		final String version = StringUtils.trimToNull(applicationFields.get(Application.Field.Version));
		final String path = StringUtils.trimToNull(applicationFields.get(Application.Field.Path));
		if (LOG.isDebugEnabled()) {
			LOG.debug("Searching application with name=" + name + ", version=" + version + ", path=" + path
					+ " in resource " + (resource == null ? "null" : resource.getName()));
		}
		if (resource == null || name == null || version == null || path == null) {
			return null;
		}
		final Application application = resource.getApplication(name, version, path);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Found application " + application);
		}
		return application;
	}

	/**
	 * Finds a queue based on the fields that were previously set.
	 * 
	 * @param resource
	 *            the resource on which the application resides.
	 * 
	 * @return a queue that matches or {@code null} if no queue was found.
	 */
	public Queue findQueue(final Resource resource) {
		final String name = StringUtils.trimToNull(queueFields.get(Queue.Field.Name));
		if (LOG.isDebugEnabled()) {
			LOG.debug("Searching queue with name=" + name + " in resource " + resource);
		}
		if (resource == null || name == null) {
			return null;
		}
		final Queue queue = resource.getQueue(name);
		if (LOG.isDebugEnabled()) {
			LOG.debug("Found queue " + queue);
		}
		return queue;
	}

	/**
	 * Adds a search criterion.
	 * 
	 * @param field
	 *            the field.
	 * @param value
	 *            the value.
	 */
	public void addSearchCriterion(final FormField field, final String value) {
		boolean handled = false;
		for (final FormFieldHandler handler : handlers) {
			if (handler.canHandle(field)) {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Adding search criterion. field=" + field + ", value=" + value);
				}
				handler.handle(field, value);
				handled = true;
			}
		}
		if (!handled) {
			throw new InvalidFieldException(field);
		}
	}

	private class ApplicationFieldHandler extends AbstractFormFieldHandler {
		ApplicationFieldHandler() {
			super(Application.Field.class, applicationFields);
		}
	}

	private class ResourceFieldHandler extends AbstractFormFieldHandler {
		ResourceFieldHandler() {
			super(Resource.Field.class, resourceFields);
		}
	}

	private class QueueFieldHandler extends AbstractFormFieldHandler {
		QueueFieldHandler() {
			super(Queue.Field.class, queueFields);
		}
	}
}
