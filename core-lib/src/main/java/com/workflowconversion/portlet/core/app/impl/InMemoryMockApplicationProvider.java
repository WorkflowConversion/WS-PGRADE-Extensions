package com.workflowconversion.portlet.core.app.impl;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.workflowconversion.portlet.core.app.Application;
import com.workflowconversion.portlet.core.app.ApplicationProvider;
import com.workflowconversion.portlet.core.exception.NotEditableApplicationProviderException;
import com.workflowconversion.portlet.core.middleware.MiddlewareProvider;

import dci.data.Middleware;

public class InMemoryMockApplicationProvider implements ApplicationProvider, Serializable {

	private static final long serialVersionUID = 9085026519196444948L;

	private final static int N_INITIAL_APPS = 15;

	private final boolean editable;
	private final String name;
	private final MiddlewareProvider middlewareProvider;
	private final Map<Integer, Application> applicationMap;
	private int currentApplicationId;

	public InMemoryMockApplicationProvider(final String name, final MiddlewareProvider middlewareProvider,
			final boolean editable) {
		this.name = name;
		this.middlewareProvider = middlewareProvider;
		this.editable = editable;
		this.applicationMap = new TreeMap<Integer, Application>();
		this.currentApplicationId = 0;
		fillInitialApps();
	}

	private void fillInitialApps() {
		final Middleware[] middlewares = middlewareProvider.getAllMiddlewares().toArray(new Middleware[] {});
		for (currentApplicationId = 0; currentApplicationId < N_INITIAL_APPS; currentApplicationId++) {
			final Application application = new Application();
			application.setId(Integer.toString(currentApplicationId));
			application.setName("Fake application " + currentApplicationId);
			application.setDescription("Description of fake application " + currentApplicationId);
			application.setPath("/path/of/fake/app_" + currentApplicationId);
			application.setResource("resource.app-" + currentApplicationId + ".fake");
			application.setResourceType(getRandomResourceType(middlewares));
			application.setVersion("1.1." + currentApplicationId);
			applicationMap.put(currentApplicationId, application);
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
	public Collection<Application> getApplications() {
		return applicationMap.values();
	}

	@Override
	public String addApplication(final Application app) throws NotEditableApplicationProviderException {
		validateEditableBeforeEdition();
		// assign the id
		app.setId(Integer.toString(currentApplicationId));
		applicationMap.put(currentApplicationId, app);
		return Integer.toHexString(currentApplicationId++);
	}

	@Override
	public void saveApplication(final Application app) throws NotEditableApplicationProviderException {
		validateEditableBeforeEdition();
		applicationMap.put(Integer.valueOf(app.getId()), app);
	}

	@Override
	public void removeApplication(final Application app) throws NotEditableApplicationProviderException {
		validateEditableBeforeEdition();
		applicationMap.remove(Integer.valueOf(app.getId()));
	}

	private void validateEditableBeforeEdition() {
		if (!editable) {
			throw new NotEditableApplicationProviderException("This mock application provider is read only.");
		}
	}
}
