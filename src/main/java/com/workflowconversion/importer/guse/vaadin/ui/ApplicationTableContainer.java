package com.workflowconversion.importer.guse.vaadin.ui;

import static com.workflowconversion.importer.guse.vaadin.ui.ApplicationsViewPanel.COLUMN_DESCRIPTION;
import static com.workflowconversion.importer.guse.vaadin.ui.ApplicationsViewPanel.COLUMN_ID;
import static com.workflowconversion.importer.guse.vaadin.ui.ApplicationsViewPanel.COLUMN_NAME;
import static com.workflowconversion.importer.guse.vaadin.ui.ApplicationsViewPanel.COLUMN_PATH;
import static com.workflowconversion.importer.guse.vaadin.ui.ApplicationsViewPanel.COLUMN_RESOURCE;
import static com.workflowconversion.importer.guse.vaadin.ui.ApplicationsViewPanel.COLUMN_RESOURCE_TYPE;
import static com.workflowconversion.importer.guse.vaadin.ui.ApplicationsViewPanel.COLUMN_VERSION;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.workflowconversion.importer.guse.appdb.Application;
import com.workflowconversion.importer.guse.appdb.ApplicationProvider;
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

	private final MiddlewareProvider middlewareProvider;
	private final ApplicationProvider applicationProvider;
	private final Set<String> dirtyItemIds;
	private volatile boolean editable;

	/**
	 * Constructor.
	 * 
	 * @param middlewareProvider
	 *            the middleware provider.
	 */
	ApplicationTableContainer(final MiddlewareProvider middlewareProvider,
			final ApplicationProvider applicationProvider) {
		Validate.notNull(middlewareProvider,
				"middlewareProvider cannot be null, this is quite likely a bug and should be reported");
		Validate.notNull(applicationProvider,
				"applicationProvider cannot be null, this is quite likely a bug and should be reported");
		this.middlewareProvider = middlewareProvider;
		this.applicationProvider = applicationProvider;
		this.dirtyItemIds = new TreeSet<String>();
		this.editable = false;

		setUpProperties();
		setInitialApplications(applicationProvider.getApplications());
	}

	private void setUpProperties() {
		addContainerProperty(COLUMN_ID, String.class, null);
		addContainerProperty(COLUMN_NAME, TextField.class, null);
		addContainerProperty(COLUMN_VERSION, TextField.class, null);
		addContainerProperty(COLUMN_RESOURCE_TYPE, ComboBox.class, null);
		addContainerProperty(COLUMN_RESOURCE, TextField.class, null);
		addContainerProperty(COLUMN_DESCRIPTION, TextArea.class, null);
		addContainerProperty(COLUMN_PATH, TextField.class, null);
	}

	private void setInitialApplications(final Collection<Application> initialApplications) {
		if (initialApplications != null) {
			for (final Application application : initialApplications) {
				addApplicationInContainer(application);
			}
		}
	}

	/**
	 * Adds an application in this container and in the application provider.
	 * 
	 * @param application
	 *            the application to add, it must contain a valid ID.
	 * @returns the added item.
	 */
	synchronized void addApplication(final Application application) {
		validateApplicationBeforeUpdate(application);
		// add first in the container, for it will update the id field
		applicationProvider.addApplication(application);
		addApplicationInContainer(application);
	}

	private void validateApplicationBeforeUpdate(final Application application) {
		Validate.notNull(application, "application cannot be null, this is quite likely a bug and should be reported");
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
			throw new InvalidApplicationException("Unrecognized middleware type", application);
		}
		Validate.isTrue(StringUtils.isNotBlank(application.getName()),
				"application name cannot be empty, null or contain only whitespace elements");
		Validate.isTrue(StringUtils.isNotBlank(application.getVersion()),
				"version cannot be empty, null or contain only whitespace elements");
		Validate.isTrue(StringUtils.isNotBlank(application.getPath()),
				"application path cannot be empty, null or contain only whitespace elements");
		Validate.isTrue(StringUtils.isNotBlank(application.getResource()),
				"resource cannot be empty, null or contain only whitespace elements");
	}

	private void addApplicationInContainer(final Application application) {
		final Item newItem = super.addItem(application.getId());
		fillNewItemFields(application, newItem);
	}

	@Override
	public synchronized boolean removeItem(final Object itemId) {
		// remove from the application provider
		applicationProvider.removeApplication(toApplication(itemId.toString()));
		// just in case, remove from the dirty items
		dirtyItemIds.remove(itemId.toString());
		// remove from this container
		return super.removeItem(itemId);
	}

	/**
	 * Saves dirty items on the provider.
	 */
	synchronized void saveDirtyItems() {
		// save items
		for (final String id : dirtyItemIds) {
			final Application dirtyApplication = toApplication(id);
			applicationProvider.saveApplication(dirtyApplication);
		}
		// clear dirty items
		dirtyItemIds.clear();
	}

	private Application toApplication(final Item item) {
		final Application application = new Application();
		application.setId(item.getItemProperty(COLUMN_ID).toString());
		application.setName(item.getItemProperty(COLUMN_NAME).toString());
		application.setVersion(item.getItemProperty(COLUMN_VERSION).toString());
		application.setResourceType(item.getItemProperty(COLUMN_RESOURCE_TYPE).toString());
		application.setResource(item.getItemProperty(COLUMN_RESOURCE).toString());
		application.setDescription(item.getItemProperty(COLUMN_DESCRIPTION).toString());
		application.setPath(item.getItemProperty(COLUMN_PATH).toString());
		return application;
	}

	private Application toApplication(final String itemId) {
		final Item item = getItem(itemId);
		return toApplication(item);
	}

	private void fillNewItemFields(final Application application, final Item item) {
		item.getItemProperty(COLUMN_ID).setValue(application.getId().trim());
		item.getItemProperty(COLUMN_NAME).setValue(newTextFieldWithValue(application.getName().trim()));
		item.getItemProperty(COLUMN_VERSION).setValue(newTextFieldWithValue(application.getVersion().trim()));
		item.getItemProperty(COLUMN_RESOURCE_TYPE).setValue(newResourceComboBox(application.getResourceType()));
		item.getItemProperty(COLUMN_RESOURCE).setValue(newTextFieldWithValue(application.getResource().trim()));
		item.getItemProperty(COLUMN_DESCRIPTION).setValue(newTextAreaWithValue(application.getDescription().trim()));
		item.getItemProperty(COLUMN_PATH).setValue(newTextFieldWithValue(application.getPath().trim()));
		updateEditable(item);
		setValueChangeListener(item);
	}

	private TextField newTextFieldWithValue(final String value) {
		final TextField textField = new TextField();
		textField.setValue(value);
		return textField;
	}

	private TextArea newTextAreaWithValue(final String value) {
		final TextArea textArea = new TextArea();
		textArea.setValue(value);
		return textArea;
	}

	private ComboBox newResourceComboBox(final String resourceType) {
		final ComboBox comboBox = new ComboBox();
		for (final Middleware middleware : middlewareProvider.getAllMiddlewares()) {
			comboBox.addItem(middleware.getType());
		}
		comboBox.select(resourceType);
		return comboBox;
	}

	/**
	 * Controls whether the controls in this data source are editable.
	 * 
	 * @param editable
	 *            whether the controls should be editable.
	 */
	void setEditable(final boolean editable) {
		this.editable = editable;
		for (final Object itemId : getItemIds()) {
			final Item item = getItem(itemId);
			updateEditable(item);
		}
	}

	private void updateEditable(final Item item) {
		for (final Object propertyId : item.getItemPropertyIds()) {
			final Object propertyValue = item.getItemProperty(propertyId);
			if (propertyValue instanceof Component) {
				((Component) propertyValue).setReadOnly(!editable);
			}
		}
	}

	private void setValueChangeListener(final Item item) {
		for (final Object propertyId : item.getItemPropertyIds()) {
			final Object propertyValue = item.getItemProperty(propertyId).getValue();
			if (propertyValue instanceof Field) {
				((Field) propertyValue).addListener(new Property.ValueChangeListener() {
					private static final long serialVersionUID = 2055195168270807750L;

					@Override
					public void valueChange(ValueChangeEvent event) {
						// this item is now dirty
						synchronized (ApplicationTableContainer.this) {
							dirtyItemIds.add(item.getItemProperty(COLUMN_ID).getValue().toString());
						}
					}
				});
				;
			}
		}
	}
}
