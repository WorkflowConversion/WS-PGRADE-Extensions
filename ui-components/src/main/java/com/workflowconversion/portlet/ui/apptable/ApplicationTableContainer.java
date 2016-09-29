package com.workflowconversion.portlet.ui.apptable;

import static com.workflowconversion.portlet.ui.apptable.ApplicationsTable.COLUMN_DESCRIPTION;
import static com.workflowconversion.portlet.ui.apptable.ApplicationsTable.COLUMN_ID;
import static com.workflowconversion.portlet.ui.apptable.ApplicationsTable.COLUMN_NAME;
import static com.workflowconversion.portlet.ui.apptable.ApplicationsTable.COLUMN_PATH;
import static com.workflowconversion.portlet.ui.apptable.ApplicationsTable.COLUMN_RESOURCE;
import static com.workflowconversion.portlet.ui.apptable.ApplicationsTable.COLUMN_RESOURCE_TYPE;
import static com.workflowconversion.portlet.ui.apptable.ApplicationsTable.COLUMN_VERSION;

import java.util.Collection;
import java.util.LinkedList;
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
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.workflowconversion.portlet.core.app.Application;
import com.workflowconversion.portlet.core.app.ApplicationProvider;
import com.workflowconversion.portlet.core.exception.ApplicationException;
import com.workflowconversion.portlet.core.exception.InvalidApplicationException;
import com.workflowconversion.portlet.core.middleware.MiddlewareProvider;

import dci.data.Middleware;

/**
 * Data source for the application table. Interacts with an application provider.
 * 
 * @author delagarza
 */
class ApplicationTableContainer extends IndexedContainer {

	private static final long serialVersionUID = -9169222187689135218L;

	private final ApplicationProvider applicationProvider;
	private final MiddlewareProvider middlewareProvider;
	private final Set<String> dirtyItemIds;
	// whether it is possible to edit fields
	private boolean editable;
	// whether right now fields are to be edited
	private boolean editMode;

	/**
	 * Constructor.
	 * 
	 * @param applicationProvider
	 *            the application provider.
	 * @param middlewareProvider
	 *            the middleware provider.
	 * @param editable
	 *            whether the fields will be editable at some point.
	 */
	ApplicationTableContainer(final ApplicationProvider applicationProvider,
			final MiddlewareProvider middlewareProvider, final boolean editable) {
		Validate.notNull(applicationProvider, "applicationProvider cannot be null");
		Validate.notNull(middlewareProvider, "middlewareProvider cannot be null");
		this.applicationProvider = applicationProvider;
		this.middlewareProvider = middlewareProvider;
		this.dirtyItemIds = new TreeSet<String>();
		this.editable = editable;
		this.editMode = false;

		setUpProperties();
		setInitialApplications(applicationProvider.getApplications());
	}

	private void setUpProperties() {
		if (editable) {
			setUpPropertiesWithFields();
		} else {
			setUpPropertiesWithStrings();
		}
	}

	private void setUpPropertiesWithFields() {
		addContainerProperty(COLUMN_ID, Label.class, null);
		addContainerProperty(COLUMN_NAME, TextField.class, null);
		addContainerProperty(COLUMN_VERSION, TextField.class, null);
		addContainerProperty(COLUMN_RESOURCE_TYPE, ComboBox.class, null);
		addContainerProperty(COLUMN_RESOURCE, TextField.class, null);
		addContainerProperty(COLUMN_DESCRIPTION, TextArea.class, null);
		addContainerProperty(COLUMN_PATH, TextField.class, null);
	}

	private void setUpPropertiesWithStrings() {
		addContainerProperty(COLUMN_ID, String.class, null);
		addContainerProperty(COLUMN_NAME, String.class, null);
		addContainerProperty(COLUMN_VERSION, String.class, null);
		addContainerProperty(COLUMN_RESOURCE_TYPE, String.class, null);
		addContainerProperty(COLUMN_RESOURCE, String.class, null);
		addContainerProperty(COLUMN_DESCRIPTION, String.class, null);
		addContainerProperty(COLUMN_PATH, String.class, null);
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
	void addApplication(final Application application) {
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
		fillNewItemProperties(application, newItem);
	}

	@Override
	public boolean removeItem(final Object itemId) {
		// remove from the application provider
		applicationProvider.removeApplication(toApplication(itemId.toString()));
		// just in case, remove from the dirty items
		dirtyItemIds.remove(itemId.toString());
		// remove from this container
		return super.removeItem(itemId);
	}

	/**
	 * Saves dirty items on the provider. It is assumed that client code checked for validation using the
	 * {@link #getValidationErrors()} method.
	 */
	void saveDirtyItems() {
		// save items
		for (final String id : dirtyItemIds) {
			final Application dirtyApplication = toApplication(id);
			applicationProvider.saveApplication(dirtyApplication);
		}
		// clear dirty items
		dirtyItemIds.clear();
	}

	/**
	 * Returns validation errors that can be displayed in a UI.
	 * 
	 * @return the validation errors.
	 */
	Collection<String> getValidationErrors() {
		final Collection<String> validationErrors = new LinkedList<String>();

		return validationErrors;
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

	private void fillNewItemProperties(final Application application, final Item item) {
		if (editable) {
			item.getItemProperty(COLUMN_ID).setValue(newLabelWithValue(StringUtils.trimToEmpty(application.getId())));
			item.getItemProperty(COLUMN_NAME)
					.setValue(newTextFieldWithValue(StringUtils.trimToEmpty(application.getName())));
			item.getItemProperty(COLUMN_VERSION)
					.setValue(newTextFieldWithValue(StringUtils.trimToEmpty(application.getVersion())));
			item.getItemProperty(COLUMN_RESOURCE_TYPE)
					.setValue(newResourceComboBox(StringUtils.trimToEmpty(application.getResourceType())));
			item.getItemProperty(COLUMN_RESOURCE)
					.setValue(newTextFieldWithValue(StringUtils.trimToEmpty(application.getResource())));
			item.getItemProperty(COLUMN_DESCRIPTION)
					.setValue(newTextAreaWithValue(StringUtils.trimToEmpty(application.getDescription())));
			item.getItemProperty(COLUMN_PATH)
					.setValue(newTextFieldWithValue(StringUtils.trimToEmpty(application.getPath())));
			updateEditMode(item);
			setValueChangeListener(item);
		} else {
			item.getItemProperty(COLUMN_ID).setValue(StringUtils.trimToEmpty(application.getId()));
			item.getItemProperty(COLUMN_NAME).setValue(StringUtils.trimToEmpty(application.getName()));
			item.getItemProperty(COLUMN_VERSION).setValue(StringUtils.trimToEmpty(application.getVersion()));
			item.getItemProperty(COLUMN_RESOURCE_TYPE).setValue(StringUtils.trimToEmpty(application.getResourceType()));
			item.getItemProperty(COLUMN_RESOURCE).setValue(StringUtils.trimToEmpty(application.getResource()));
			item.getItemProperty(COLUMN_DESCRIPTION).setValue(StringUtils.trimToEmpty(application.getDescription()));
			item.getItemProperty(COLUMN_PATH).setValue(StringUtils.trimToEmpty(application.getPath()));
		}
	}

	private TextField newTextFieldWithValue(final String value) {
		final TextField textField = new TextField();
		textField.setValue(value);
		textField.setImmediate(true);
		textField.setWriteThrough(true);
		return textField;
	}

	private TextArea newTextAreaWithValue(final String value) {
		final TextArea textArea = new TextArea();
		textArea.setValue(value);
		textArea.setImmediate(true);
		textArea.setWriteThrough(true);
		return textArea;
	}

	private Label newLabelWithValue(final String value) {
		final Label label = new Label();
		label.setValue(value);
		return label;
	}

	private ComboBox newResourceComboBox(final String resourceType) {
		final ComboBox comboBox = new ComboBox();
		for (final Middleware middleware : middlewareProvider.getAllMiddlewares()) {
			comboBox.addItem(middleware.getType());
		}
		comboBox.setNullSelectionAllowed(false);
		comboBox.setImmediate(true);
		comboBox.setWriteThrough(true);
		comboBox.select(resourceType);
		return comboBox;
	}

	/**
	 * Controls whether the controls in this data source are set in edit mode.
	 * 
	 * If this control has been set as non-editable, the outcome of this method depends on the passed parameter:
	 * 
	 * <ul>
	 * <li>if {@code true} is passed as a parameter, then this method will throw an exception indicating that this is a
	 * read-only component.
	 * <li>otherwise the invocation will take no effect.
	 * </ul>
	 * 
	 * @param editable
	 *            whether the controls should be set on edit mode.
	 */
	void setEditable(final boolean editable) {
		if (!this.editable) {
			// throw an exception if we're read-only and client code wants to set this control as editable
			// otherwise, just ignore the request
			if (editable) {
				throw new ApplicationException(
						"This is a read-only component. This is quite likely a problem in the code and should be reported.");
			}
		} else {
			this.editMode = editable;
			for (final Object itemId : getItemIds()) {
				final Item item = getItem(itemId);
				updateEditMode(item);
			}
		}
	}

	private void updateEditMode(final Item item) {
		for (final Object propertyId : item.getItemPropertyIds()) {
			final Object propertyValue = item.getItemProperty(propertyId).getValue();
			if (propertyValue instanceof Component) {
				((Component) propertyValue).setReadOnly(!editMode);
				((Component) propertyValue).setEnabled(editMode);
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
