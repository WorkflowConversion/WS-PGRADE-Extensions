package com.workflowconversion.importer.app.impl;

import java.util.Collection;

import com.workflowconversion.importer.app.Application;
import com.workflowconversion.importer.app.ApplicationProvider;
import com.workflowconversion.importer.exception.NotEditableApplicationProviderException;

public class MockApplicationProvider implements ApplicationProvider {

	public MockApplicationProvider() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isEditable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean needsInit() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public Collection<Application> getApplications() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addApplication(Application app) throws NotEditableApplicationProviderException {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveApplication(Application app) throws NotEditableApplicationProviderException {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeApplication(Application app) throws NotEditableApplicationProviderException {
		// TODO Auto-generated method stub

	}

}
