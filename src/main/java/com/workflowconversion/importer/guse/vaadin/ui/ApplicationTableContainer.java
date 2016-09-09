package com.workflowconversion.importer.guse.vaadin.ui;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.TextField;
import com.workflowconversion.importer.guse.appdb.Application;
import com.workflowconversion.importer.guse.appdb.ApplicationProvider;
import com.workflowconversion.importer.guse.appdb.impl.UnicoreApplicationProvider;
import com.workflowconversion.importer.guse.exception.InvalidApplicationException;
import com.workflowconversion.importer.guse.middleware.MiddlewareProvider;

import dci.data.Middleware;

/**
 * Data source for the application table. Interacts with an application provider.
 * 
 * @author delagarza
 */
class ApplicationTableContainer extends IndexedContainer {

	private static final long serialVersionUID = -9169222187689135218L;

	private static final String COLUMN_NAME = "Name";
	private static final String COLUMN_VERSION = "Version";
	private static final String COLUMN_RESOURCE_TYPE = "Resource Type";
	private static final String COLUMN_RESOURCE = "Resource";
	private static final String COLUMN_DESCRIPTION = "Description";
	private static final String COLUMN_PATH = "Path";

	private final MiddlewareProvider middlewareProvider;
	private final ApplicationProvider applicationProvider;

	/**
	 * Constructor.
	 * 
	 * @param middlewareProvider
	 *            the middleware provider.
	 * @param applicationProvider
	 *            the application provider.
	 */
	ApplicationTableContainer(final MiddlewareProvider middlewareProvider,
			final ApplicationProvider applicationProvider) {
		Validate.notNull(middlewareProvider,
				"middlewareProvider cannot be null, this is quite likely a bug and should be reported");
		Validate.notNull(applicationProvider,
				"applicationProvider cannot be null, this is quite likely a bug and should be reported");
		this.middlewareProvider = middlewareProvider;
		this.applicationProvider = applicationProvider;

		setUpProperties();
		setInitialApplications();
	}

	private void setUpProperties() {
		addContainerProperty(COLUMN_NAME, String.class, null);
		addContainerProperty(COLUMN_VERSION, String.class, null);
		addContainerProperty(COLUMN_RESOURCE_TYPE, String.class, null);
		addContainerProperty(COLUMN_RESOURCE, String.class, null);
		addContainerProperty(COLUMN_DESCRIPTION, TextField.class, null);
		addContainerProperty(COLUMN_PATH, String.class, null);
	}

	void setInitialApplications() {
		final Collection<Application> initialApplications = applicationProvider.getApplications();
		// add them only in the container
		for (final Application application : initialApplications) {
			addApplicationInContainer(application);
		}
	}

	/**
	 * Adds an application.
	 * 
	 * @param application
	 *            the application to add.
	 */
	void addApplication(final Application application) {
		validateApplicationBeforeUpdate(application);
		// add it on storage
		addApplicationInStorage(application);
		// and add it on our container (this)
		addApplicationInContainer(application);
	}

	private void addApplicationInStorage(final Application application) {
		applicationProvider.addApplication(application);
	}

	private void addApplicationInContainer(final Application application) {
		final Item newItem = super.addItem(application.getId());
		fillItemProperties(application, newItem);
	}

	private void fillItemProperties(final Application application, final Item item) {
		item.getItemProperty(COLUMN_NAME).setValue(application.getName().trim());
		item.getItemProperty(COLUMN_VERSION).setValue(application.getVersion().trim());
		item.getItemProperty(COLUMN_RESOURCE_TYPE).setValue(application.getResourceType().trim());
		item.getItemProperty(COLUMN_RESOURCE).setValue(application.getResource().trim());
		item.getItemProperty(COLUMN_DESCRIPTION).setValue(application.getDescription().trim());
		item.getItemProperty(COLUMN_PATH).setValue(application.getPath().trim());
	}

	void validateApplicationBeforeUpdate(final Application application) {
		Validate.notNull(application, "application cannot be null, this is quite likely a bug and should be reported");
		// check that it is not UNICORE
		if (UnicoreApplicationProvider.UNICORE_RESOURCE_TYPE
				.equals(application.getResourceType().toLowerCase().trim())) {
			throw new InvalidApplicationException("Cannot add applications to UNICORE", application);
		}
		// check that the resource type is a known type
		Validate.isTrue(StringUtils.isNotBlank(application.getResourceType()),
				"the resource type of the application cannot be null, empty or contain only whitespace; this is probably a bug and should be reported");
		final String resourceType = application.getResourceType().toLowerCase().trim();
		boolean found = false;
		for (final Middleware middleware : middlewareProvider.getAllMiddlewares()) {
			if (resourceType.equals(middleware.getType())) {
				found = true;
				break;
			}
		}
		if (!found) {
			throw new InvalidApplicationException("Unrecognized resource type", application);
		}
	}

}
