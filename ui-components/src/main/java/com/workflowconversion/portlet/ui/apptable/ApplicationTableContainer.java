package com.workflowconversion.portlet.ui.apptable;

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
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.workflowconversion.portlet.core.app.Application;
import com.workflowconversion.portlet.core.app.Resource;
import com.workflowconversion.portlet.core.exception.ApplicationException;

/**
 * Data source for the application table. Interacts with an application provider.
 * 
 * @author delagarza
 */
class ApplicationTableContainer extends IndexedContainer {

	private static final long serialVersionUID = -9169222187689135218L;
	private static final String PROPERTY_ID = "Item_ID";

	private final Resource resource;
	private final Set<String> dirtyItemIds;
	// whether it is possible to edit fields
	private boolean editable;
	// whether right now fields are to be edited
	private boolean editMode;

	/**
	 * Constructor.
	 * 
	 * @param resource
	 *            the resource owning the applications to be displayed.
	 * @param editable
	 *            whether the fields will be editable at some point.
	 */
	ApplicationTableContainer(final Resource resource, final boolean editable) {
		Validate.notNull(resource, "resource cannot be null");

		this.resource = resource;
		this.editable = editable;
		this.dirtyItemIds = new TreeSet<String>();
		this.editMode = false;

		fillValidMiddlewareTypes();
		setUpProperties();
		setInitialApplications(resource.getApplications());
	}

	private void fillValidMiddlewareTypes() {

	}

	private void setUpProperties() {
		if (editable) {
			setUpPropertiesWithFields();
		} else {
			setUpPropertiesWithStrings();
		}
	}

	private void setUpPropertiesWithFields() {
		addContainerProperty(PROPERTY_ID, Label.class, null);
		addContainerProperty(Application.Field.Name, TextField.class, null);
		addContainerProperty(Application.Field.Version, TextField.class, null);
		addContainerProperty(Application.Field.Description, TextArea.class, null);
		addContainerProperty(Application.Field.Path, TextField.class, null);
	}

	private void setUpPropertiesWithStrings() {
		addContainerProperty(PROPERTY_ID, String.class, null);
		addContainerProperty(Application.Field.Name, String.class, null);
		addContainerProperty(Application.Field.Version, String.class, null);
		addContainerProperty(Application.Field.Description, String.class, null);
		addContainerProperty(Application.Field.Path, String.class, null);
	}

	private void setInitialApplications(final Collection<Application> initialApplications) {
		if (initialApplications != null) {
			for (final Application application : initialApplications) {
				addApplicationInContainer(application);
			}
		}
	}

	/**
	 * Adds an application in this container and to the owner resource.
	 * 
	 * @param application
	 *            the application to add, it must contain a valid ID.
	 * @returns the added item.
	 */
	void addApplication(final Application application) {
		validateApplicationBeforeUpdate(application);
		resource.addApplication(application);
		addApplicationInContainer(application);
	}

	private void validateApplicationBeforeUpdate(final Application application) {
		Validate.notNull(application, "application cannot be null, this is quite likely a bug and should be reported");
		Validate.isTrue(StringUtils.isNotBlank(application.getName()),
				"application name cannot be empty, null or contain only whitespace elements");
		Validate.isTrue(StringUtils.isNotBlank(application.getVersion()),
				"version cannot be empty, null or contain only whitespace elements");
		Validate.isTrue(StringUtils.isNotBlank(application.getPath()),
				"application path cannot be empty, null or contain only whitespace elements");
		Validate.notNull(application.getResource(), "resource cannot be null");
	}

	private void addApplicationInContainer(final Application application) {
		// check if the application already exists
		final Item newItem = super.addItem(application.generateKey());
		fillNewItemProperties(application, newItem);
	}

	@Override
	public boolean removeItem(final Object itemId) {
		// remove from the resource
		resource.removeApplication(toApplication(itemId.toString()));
		// just in case, remove from the dirty items
		dirtyItemIds.remove(itemId.toString());
		// remove from this container
		return super.removeItem(itemId);
	}

	/**
	 * Saves dirty items on the provider.
	 */
	void saveDirtyItems() {
		// save items
		for (final String id : dirtyItemIds) {
			final Application dirtyApplication = toApplication(id);
			resource.saveApplication(dirtyApplication);
		}
		// clear dirty items
		dirtyItemIds.clear();
	}

	private Application toApplication(final Item item) {
		final Application application = new Application();
		application.setName(item.getItemProperty(Application.Field.Name).getValue().toString());
		application.setVersion(item.getItemProperty(Application.Field.Version).getValue().toString());
		application.setDescription(item.getItemProperty(Application.Field.Description).getValue().toString());
		application.setPath(item.getItemProperty(Application.Field.Path).getValue().toString());
		return application;
	}

	private Application toApplication(final String itemId) {
		final Item item = getItem(itemId);
		return toApplication(item);
	}

	private void fillNewItemProperties(final Application application, final Item item) {
		if (editable) {
			final Label idLabel = new Label();
			idLabel.setValue(application.generateKey());
			item.getItemProperty(PROPERTY_ID).setValue(idLabel);
			item.getItemProperty(Application.Field.Name)
					.setValue(newTextFieldWithValue(StringUtils.trimToEmpty(application.getName())));
			item.getItemProperty(Application.Field.Version)
					.setValue(newTextFieldWithValue(StringUtils.trimToEmpty(application.getVersion())));
			item.getItemProperty(Application.Field.Description)
					.setValue(newTextAreaWithValue(StringUtils.trimToEmpty(application.getDescription())));
			item.getItemProperty(Application.Field.Path)
					.setValue(newTextFieldWithValue(StringUtils.trimToEmpty(application.getPath())));
			updateEditMode(item);
			setValueChangeListener(item);
		} else {
			item.getItemProperty(PROPERTY_ID).setValue(application.generateKey());
			item.getItemProperty(Application.Field.Name).setValue(StringUtils.trimToEmpty(application.getName()));
			item.getItemProperty(Application.Field.Version).setValue(StringUtils.trimToEmpty(application.getVersion()));
			item.getItemProperty(Application.Field.Description)
					.setValue(StringUtils.trimToEmpty(application.getDescription()));
			item.getItemProperty(Application.Field.Path).setValue(StringUtils.trimToEmpty(application.getPath()));
		}
	}

	private TextField newTextFieldWithValue(final String value) {
		final TextField textField = new TextField();
		textField.setValue(value);
		textField.setImmediate(true);
		textField.setBuffered(false);
		return textField;
	}

	private TextArea newTextAreaWithValue(final String value) {
		final TextArea textArea = new TextArea();
		textArea.setValue(value);
		textArea.setImmediate(true);
		textArea.setBuffered(false);
		return textArea;
	}

	// TODO: move this method to the ResourceTableContainer
	private ComboBox newResourceComboBox(final String resourceType) {
		final ComboBox comboBox = new ComboBox();
		// for (final String middlewareType : validMiddlewareTypes) {
		// comboBox.addItem(middlewareType);
		// }
		comboBox.setNullSelectionAllowed(false);
		comboBox.setImmediate(true);
		comboBox.setBuffered(false);
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
				((Field<?>) propertyValue).addValueChangeListener(new Property.ValueChangeListener() {
					private static final long serialVersionUID = 2055195168270807750L;

					@Override
					public void valueChange(final ValueChangeEvent event) {
						// this item is now dirty
						synchronized (ApplicationTableContainer.this) {
							dirtyItemIds.add(item.getItemProperty(PROPERTY_ID).getValue().toString());
						}
					}
				});
				;
			}
		}
	}
}
