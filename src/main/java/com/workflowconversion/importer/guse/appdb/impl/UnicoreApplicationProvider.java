package com.workflowconversion.importer.guse.appdb.impl;

import java.util.Collection;

import com.workflowconversion.importer.guse.appdb.Application;
import com.workflowconversion.importer.guse.appdb.ApplicationProvider;
import com.workflowconversion.importer.guse.exception.NotEditableApplicationProviderException;

/**
 * Application provider that interacts with UNICORE. This is not editable.
 * 
 * @author delagarza
 *
 */
public class UnicoreApplicationProvider implements ApplicationProvider {

	public UnicoreApplicationProvider() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isEditable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<Application> getApplications() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addApplication(Application app) throws NotEditableApplicationProviderException {
		throw new NotEditableApplicationProviderException(
				"The UNICORE ApplicationProvider is not editable! This is an invalid operation.");
	}

	@Override
	public void saveApplication(Application app) throws NotEditableApplicationProviderException {
		throw new NotEditableApplicationProviderException(
				"The UNICORE ApplicationProvider is not editable! This is an invalid operation.");
	}

	@Override
	public Collection<Application> searchApplicationsByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

}
