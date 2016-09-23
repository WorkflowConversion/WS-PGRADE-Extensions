package com.workflowconversion.portlet.core.app.impl;

import java.util.Collection;

import com.workflowconversion.portlet.core.app.Application;
import com.workflowconversion.portlet.core.app.ApplicationProvider;
import com.workflowconversion.portlet.core.exception.NotEditableApplicationProviderException;

public class MockApplicationProvider implements ApplicationProvider {

	private final boolean editable;
	private final String name;

	public MockApplicationProvider(final String name, final boolean editable) {
		this.name = name;
		this.editable = editable;
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

	}

	@Override
	public Collection<Application> getApplications() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addApplication(Application app) throws NotEditableApplicationProviderException {

	}

	@Override
	public void saveApplication(Application app) throws NotEditableApplicationProviderException {

	}

	@Override
	public void removeApplication(Application app) throws NotEditableApplicationProviderException {

	}
}
