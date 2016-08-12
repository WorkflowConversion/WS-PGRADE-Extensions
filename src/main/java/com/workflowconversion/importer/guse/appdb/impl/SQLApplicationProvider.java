package com.workflowconversion.importer.guse.appdb.impl;

import java.util.Collection;

import com.workflowconversion.importer.guse.appdb.Application;
import com.workflowconversion.importer.guse.appdb.ApplicationProvider;
import com.workflowconversion.importer.guse.exception.NotEditableApplicationProviderException;

/**
 * Application provider using a SQL database as backup.
 * 
 * @author delagarza
 *
 */
public class SQLApplicationProvider implements ApplicationProvider {

	public SQLApplicationProvider() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isEditable() {
		return true;
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
	public Collection<Application> searchApplicationsByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

}
