package com.workflowconversion.portlet.core.search;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.workflowconversion.portlet.core.exception.DuplicateResourceException;
import com.workflowconversion.portlet.core.exception.InvalidFieldException;
import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.core.resource.FormField;
import com.workflowconversion.portlet.core.resource.Queue;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.core.resource.ResourceProvider;
import com.workflowconversion.portlet.core.utils.KeyUtils;

/**
 * Utility class to find applications, queues.
 * 
 * @author delagarza
 *
 */
public class AssetFinder {

	private final Map<String, Resource> resourceMap;

	private final Map<FormField, String> resourceFields;
	private final Map<FormField, String> applicationFields;
	private final Map<FormField, String> queueFields;

	private final Collection<FormFieldHandler> handlers;

	/**
	 * Constructor.
	 */
	public AssetFinder() {
		resourceMap = new TreeMap<String, Resource>();

		resourceFields = new TreeMap<FormField, String>();
		applicationFields = new TreeMap<FormField, String>();
		queueFields = new TreeMap<FormField, String>();

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
		fillResourcesMap(resourceProviders);
		clearAllFields();
	}

	private void fillResourcesMap(final Collection<ResourceProvider> resourceProviders) {
		resourceMap.clear();

		for (final ResourceProvider resourceProvider : resourceProviders) {
			for (final Resource resource : resourceProvider.getResources()) {
				if (resourceMap.put(KeyUtils.generate(resource), resource) != null) {
					throw new DuplicateResourceException(resource);
				}
			}
		}
	}

	/**
	 * Clears all fields.
	 */
	public void clearAllFields() {
		resourceFields.clear();
		applicationFields.clear();
		queueFields.clear();
	}

	public Resource findResource() {
		final String name = StringUtils.trimToNull(resourceFields.get(Resource.Field.Name));
		final String type = StringUtils.trimToNull(resourceFields.get(Resource.Field.Type));
		if (name == null || type == null) {
			return null;
		}
		return resourceMap.get(KeyUtils.generateResourceKey(name, type));
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
		if (resource == null) {
			return null;
		}
		final String name = StringUtils.trimToNull(applicationFields.get(Application.Field.Name));
		final String path = StringUtils.trimToNull(applicationFields.get(Application.Field.Path));
		final String version = StringUtils.trimToNull(applicationFields.get(Application.Field.Version));
		if (name == null || version == null || path == null) {
			return null;
		}
		return resource.getApplication(name, version, path);
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
		if (resource == null) {
			return null;
		}
		final String name = StringUtils.trimToNull(queueFields.get(Queue.Field.Name));
		if (name == null) {
			return null;
		}
		return resource.getQueue(name);
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
