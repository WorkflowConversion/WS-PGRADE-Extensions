package com.workflowconversion.portlet.core.search;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.workflowconversion.portlet.core.exception.DuplicateApplicationException;
import com.workflowconversion.portlet.core.exception.DuplicateQueueException;
import com.workflowconversion.portlet.core.exception.DuplicateResourceException;
import com.workflowconversion.portlet.core.exception.InvalidFieldException;
import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.core.resource.FormField;
import com.workflowconversion.portlet.core.resource.Queue;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.core.resource.ResourceProvider;
import com.workflowconversion.portlet.core.utils.SystemWideKeyUtils;

/**
 * Utility class to find applications, queues.
 * 
 * @author delagarza
 *
 */
public class AssetFinder {

	private final Map<String, Resource> resourceMap;
	private final Map<String, Application> applicationMap;
	private final Map<String, Queue> queueMap;

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

		applicationMap = new TreeMap<String, Application>();
		applicationFields = new TreeMap<FormField, String>();

		queueMap = new TreeMap<String, Queue>();
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
		fillMaps(resourceProviders);
		clearAllFields();
	}

	private void fillMaps(final Collection<ResourceProvider> resourceProviders) {
		clearAllMaps();

		for (final ResourceProvider resourceProvider : resourceProviders) {
			for (final Resource resource : resourceProvider.getResources()) {
				if (resourceMap.put(resource.generateKey(), resource) != null) {
					throw new DuplicateResourceException(resource);
				}
				for (final Queue queue : resource.getQueues()) {
					if (queueMap.put(SystemWideKeyUtils.generate(resource, queue), queue) != null) {
						throw new DuplicateQueueException(queue);
					}
				}
				for (final Application application : resource.getApplications()) {
					if (applicationMap.put(SystemWideKeyUtils.generate(resource, application),
							application) != null) {
						throw new DuplicateApplicationException(application);
					}
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

	private void clearAllMaps() {
		resourceMap.clear();
		applicationMap.clear();
		queueMap.clear();
	}

	public Resource findResource() {
		final Resource resource = new Resource();
		final String type = StringUtils.trimToNull(resourceFields.get(Resource.Field.Type));
		final String name = StringUtils.trimToNull(resourceFields.get(Resource.Field.Name));
		if (type == null || name == null) {
			return null;
		}
		resource.setType(type);
		resource.setName(name);
		return resourceMap.get(resource.generateKey());
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
		final Application application = new Application();
		final String name = StringUtils.trimToNull(applicationFields.get(Application.Field.Name));
		final String path = StringUtils.trimToNull(applicationFields.get(Application.Field.Path));
		final String version = StringUtils.trimToNull(applicationFields.get(Application.Field.Version));
		if (name == null || version == null) {
			return null;
		}
		if (path != null) {
			application.setPath(path);
		}
		application.setName(name);
		application.setVersion(version);
		return applicationMap.get(SystemWideKeyUtils.generate(resource, application));
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
		final Queue queue = new Queue();
		final String name = StringUtils.trimToNull(queueFields.get(Queue.Field.Name));
		if (name == null) {
			return null;
		}
		queue.setName(name);
		return queueMap.get(SystemWideKeyUtils.generate(resource, queue));
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
