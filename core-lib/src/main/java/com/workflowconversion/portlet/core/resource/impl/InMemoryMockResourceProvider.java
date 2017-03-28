package com.workflowconversion.portlet.core.resource.impl;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.apache.http.annotation.NotThreadSafe;
import org.jsoup.helper.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workflowconversion.portlet.core.exception.DuplicateResourceException;
import com.workflowconversion.portlet.core.exception.ProviderNotEditableException;
import com.workflowconversion.portlet.core.exception.ResourceNotFoundException;
import com.workflowconversion.portlet.core.middleware.MiddlewareProvider;
import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.core.resource.Queue;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.core.resource.ResourceProvider;

import dci.data.Middleware;

/**
 * Mock resource provider (not thread safe).
 * 
 * @author delagarza
 *
 */
@NotThreadSafe
public class InMemoryMockResourceProvider implements ResourceProvider {

	private static final long serialVersionUID = 9085026519196444948L;

	private final static Logger LOG = LoggerFactory.getLogger(InMemoryMockResourceProvider.class);

	private final static int N_INITIAL_RESOURCES = 8;
	private final static int N_INITIAL_APPS = 9;
	private final static int N_INITIAL_QUEUES = 4;

	private final boolean editable;
	private final String name;
	private final MiddlewareProvider middlewareProvider;
	private final Map<String, Resource> resources;

	public InMemoryMockResourceProvider(final String name, final MiddlewareProvider middlewareProvider,
			final boolean editable) {
		this.name = name;
		this.middlewareProvider = middlewareProvider;
		this.editable = editable;
		this.resources = new TreeMap<String, Resource>();
		fillInitialResources();
	}

	private void fillInitialResources() {
		final Middleware[] middlewares = middlewareProvider.getAllMiddlewares().toArray(new Middleware[] {});
		for (int i = 0; i < N_INITIAL_RESOURCES; i++) {
			final Resource resource = new Resource();
			resource.setType(getRandomResourceType(middlewares));
			resource.setName("Fake resource " + i);
			for (int j = 0; j < N_INITIAL_QUEUES; j++) {
				final Queue queue = new Queue();
				queue.setName("queue_" + i + '_' + j);
				resource.addQueue(queue);
			}
			for (int j = 0; j < N_INITIAL_APPS; j++) {
				final Application application = new Application();
				application.setName("Fake application " + i + ", " + j);
				application.setDescription("Description of fake application " + j);
				application.setPath("/path/of/fake/app_" + j);
				application.setVersion("1.1." + j);
				resource.addApplication(application);
			}
			addResource_internal(resource);
		}
	}

	private String getRandomResourceType(final Middleware[] middlewares) {
		return middlewares[(int) ((middlewares.length - 1) * Math.random())].getType();
	}

	@Override
	public boolean isEditable() {
		return editable;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void init() {
		// nop
	}

	@Override
	public Collection<Resource> getResources() {
		return resources.values();
	}

	@Override
	public void addResource(final Resource resource) {
		LOG.info("ADDING " + resource);
		validateEditableBeforeEdition();
		addResource_internal(resource);
	}

	/**
	 * @param resource
	 */
	private void addResource_internal(final Resource resource) {
		Validate.notNull(resource);
		final String key = resource.generateKey();
		if (!resources.containsKey(key)) {
			resources.put(key, resource);
		} else {
			throw new DuplicateResourceException(resource);
		}
	}

	@Override
	public void saveResource(final Resource resource) {
		LOG.info("SAVING " + resource);
		validateEditableBeforeEdition();
		Validate.notNull(resource);
		final String key = resource.generateKey();
		if (resources.containsKey(key)) {
			resources.put(key, resource);
		} else {
			throw new ResourceNotFoundException(resource);
		}
	}

	@Override
	public void removeResource(final Resource resource) {
		LOG.info("REMOVING " + resource);
		validateEditableBeforeEdition();
		Validate.notNull(resource);
		if (resources.remove(resource.generateKey()) == null) {
			throw new ResourceNotFoundException(resource);
		}
	}

	@Override
	public void removeAllResources() throws ProviderNotEditableException {
		LOG.info("REMOVING ALL RESOURCES");
		validateEditableBeforeEdition();
		resources.clear();
	}

	@Override
	public boolean containsResource(final Resource resource) {
		Validate.notNull(resource);
		return resources.containsKey(resource.generateKey());
	}

	@Override
	public void commitChanges() {
		LOG.info("COMMITTING CHANGES");
		validateEditableBeforeEdition();
		// does nothing, since we're actually not storing anything
	}

	private void validateEditableBeforeEdition() {
		if (!editable) {
			throw new ProviderNotEditableException("This mock application provider is read only.");
		}
	}
}
