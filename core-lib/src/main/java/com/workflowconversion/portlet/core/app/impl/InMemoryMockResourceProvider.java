package com.workflowconversion.portlet.core.app.impl;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.workflowconversion.portlet.core.app.Application;
import com.workflowconversion.portlet.core.app.Resource;
import com.workflowconversion.portlet.core.app.ResourceProvider;
import com.workflowconversion.portlet.core.exception.ProviderNotEditableException;
import com.workflowconversion.portlet.core.middleware.MiddlewareProvider;

import dci.data.Middleware;

public class InMemoryMockResourceProvider implements ResourceProvider {

	private static final long serialVersionUID = 9085026519196444948L;

	private final static int N_INITIAL_RESOURCES = 3;
	private final static int N_INITIAL_APPS = 8;
	private final static int N_INITIAL_QUEUES = 2;

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
				resource.addQueue("queue_" + j);
			}
			for (int j = 0; j < N_INITIAL_APPS; j++) {
				final Application application = new Application();
				application.setName("Fake application " + j);
				application.setDescription("Description of fake application " + j);
				application.setPath("/path/of/fake/app_" + j);
				application.setVersion("1.1." + j);
				resource.addApplication(application);
			}
			addResource(resource);
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
	public boolean needsInit() {
		return false;
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
	public void addResource(final Resource resource) throws ProviderNotEditableException {
		validateEditableBeforeEdition();
		resources.put(resource.generateKey(), resource);
	}

	@Override
	public void saveResource(final Resource resource) throws ProviderNotEditableException {
		addResource(resource);
	}

	@Override
	public void removeResource(final Resource resource) throws ProviderNotEditableException {
		validateEditableBeforeEdition();
		resources.remove(resource.generateKey());
	}

	@Override
	public void commitChanges() throws ProviderNotEditableException {
		validateEditableBeforeEdition();
		// does nothing, since we're actually not storing anything
	}

	private void validateEditableBeforeEdition() {
		if (!editable) {
			throw new ProviderNotEditableException("This mock application provider is read only.");
		}
	}
}
