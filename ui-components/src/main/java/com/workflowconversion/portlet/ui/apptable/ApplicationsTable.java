package com.workflowconversion.portlet.ui.apptable;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.Validate;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.workflowconversion.portlet.core.app.Application;
import com.workflowconversion.portlet.core.app.ApplicationProvider;
import com.workflowconversion.portlet.core.middleware.MiddlewareProvider;
import com.workflowconversion.portlet.ui.HorizontalSeparator;

/**
 * Component containing the applications to be displayed in a table, plus controls to add/save application if set on
 * editable mode.
 * 
 * Clients are responsible of validating that instances of this class based on a read-only {@link ApplicationProvider}
 * are not editable.
 */
public class ApplicationsTable extends VerticalLayout implements ApplicationCommittedListener {

	private static final long serialVersionUID = -5169354278787921392L;

	static final String COLUMN_ID = "Id";
	static final String COLUMN_NAME = "Name";
	static final String COLUMN_VERSION = "Version";
	static final String COLUMN_RESOURCE_TYPE = "Resource Type";
	static final String COLUMN_RESOURCE = "Resource";
	static final String COLUMN_DESCRIPTION = "Description";
	static final String COLUMN_PATH = "Path";

	private final Table table;
	private final ApplicationTableContainer containerDataSource;
	private final MiddlewareProvider middlewareProvider;

	/**
	 * Builds a new instance.
	 * 
	 * @param applicationProvider
	 *            the application provider to interact with the local storage.
	 * @param middlewareProvider
	 *            the middleware provider.
	 * @param withEditControls
	 *            whether the table with applications will be editable, regardless whether the passed application
	 *            provider is editable or not.
	 */
	ApplicationsTable(final ApplicationProvider applicationProvider, final MiddlewareProvider middlewareProvider,
			final boolean withEditControls) {
		Validate.notNull(applicationProvider, "applicationProvider cannot be null");
		Validate.notNull(middlewareProvider, "middlewareProvider cannot be null");
		// be defensive
		if (withEditControls && !applicationProvider.isEditable()) {
			throw new UnsupportedOperationException(
					"Invalid Parameters: edit controls should not be available in tables displaying applications from a read-only ApplicationProvider.");
		}
		this.middlewareProvider = middlewareProvider;
		this.containerDataSource = new ApplicationTableContainer(applicationProvider, middlewareProvider,
				withEditControls);
		this.table = new Table();
		if (withEditControls) {
			setUpEditControls();
		}
		setUpBasicUi();
	}

	private void setUpBasicUi() {
		setUpTable();
		addComponent(table);
	}

	private void setUpTable() {
		table.setContainerDataSource(containerDataSource);
		// make sure the ID column is hidden
		table.setVisibleColumns(new String[] { COLUMN_NAME, COLUMN_VERSION, COLUMN_DESCRIPTION, COLUMN_RESOURCE,
				COLUMN_RESOURCE_TYPE, COLUMN_PATH });
		table.setWidth(100, UNITS_PERCENTAGE);
		table.setHeight(450, UNITS_PIXELS);
		table.setSelectable(true);
		table.setMultiSelect(false);
		table.setWriteThrough(true);
		table.setReadThrough(true);
		table.setEditable(false);
		table.setSortDisabled(true);
		table.setImmediate(true);
		containerDataSource.setEditable(false);
	}

	private void setUpEditControls() {
		final Button saveButton = createButton("Save", "Save changes");
		saveButton.addListener(new ClickListener() {
			private static final long serialVersionUID = 600552795794561068L;

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					saveButtonClicked();
				} finally {
					saveButton.setEnabled(true);
				}
			}
		});

		final Button deleteButton = createButton("Delete", "Delete selected application");
		deleteButton.addListener(new ClickListener() {
			private static final long serialVersionUID = -2027451983542233810L;

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					deleteButtonClicked();
				} finally {
					deleteButton.setEnabled(true);
				}
			}
		});

		final Button addButton = createButton("Add...", "Add application");
		addButton.addListener(new ClickListener() {
			private static final long serialVersionUID = 9030768358959635995L;

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					addButtonClicked();
				} finally {
					addButton.setEnabled(true);
				}
			}
		});

		final Button bulkUploadButton = createButton("Bulk upload...", "Click to upload a CSV file with applications");
		bulkUploadButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 47556748874175883L;

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					bulkUploadButtonClicked();
				} finally {
					bulkUploadButton.setEnabled(true);
				}
			}
		});

		final Layout buttonLayout = new HorizontalLayout();
		buttonLayout.addComponent(saveButton);
		buttonLayout.addComponent(deleteButton);
		buttonLayout.addComponent(addButton);
		buttonLayout.addComponent(bulkUploadButton);
		buttonLayout.setVisible(false);

		final CheckBox editableCheckBox = new CheckBox("Editable", false);
		editableCheckBox.setWidth(10, UNITS_EM);
		editableCheckBox.setDescription("Enable edition");
		editableCheckBox.setImmediate(true);
		editableCheckBox.addListener(new ClickListener() {
			private static final long serialVersionUID = 6802078670856773823L;

			@Override
			public void buttonClick(final ClickEvent event) {
				final boolean enabled = event.getButton().booleanValue();
				// allow multi selection when editing
				table.setMultiSelect(enabled);
				table.setEditable(enabled);
				containerDataSource.setEditable(enabled);
				saveButton.setEnabled(enabled);
				deleteButton.setEnabled(enabled);
				addButton.setEnabled(enabled);
				bulkUploadButton.setEnabled(enabled);
				buttonLayout.setVisible(enabled);
			}
		});

		// make sure the delete button is enabled only if there is something selected AND
		// the control is in edit mode
		table.addListener(new Table.ValueChangeListener() {
			private static final long serialVersionUID = -139252210775992808L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				deleteButton.setEnabled(editableCheckBox.booleanValue() && (event.getProperty().getValue() != null));
			}
		});

		final Layout controlsLayout = new HorizontalLayout();
		controlsLayout.setHeight(45, UNITS_PIXELS);
		controlsLayout.addComponent(editableCheckBox);
		controlsLayout.addComponent(buttonLayout);

		final Layout layout = new VerticalLayout();
		layout.addComponent(controlsLayout);
		layout.addComponent(new HorizontalSeparator());

		// append to existing content
		super.addComponent(layout);
	}

	private void bulkUploadButtonClicked() {
		final BulkUploadApplicationsDialog bulkUploadDialog = new BulkUploadApplicationsDialog(middlewareProvider,
				this);
		if (bulkUploadDialog.getParent() != null) {
			getWindow().showNotification("The 'Bulk Upload Dialog' is already open.",
					Notification.TYPE_WARNING_MESSAGE);
		} else {
			getWindow().addWindow(bulkUploadDialog);
		}
	}

	private Button createButton(final String caption, final String description) {
		final Button button = new Button(caption);
		button.setDescription(description);
		button.setEnabled(false);
		button.setDisableOnClick(true);
		button.setImmediate(true);
		return button;
	}

	private void addButtonClicked() {
		// display a window with the application fields
		final AddApplicationDialog addApplicationDialog = new AddApplicationDialog(middlewareProvider, this);
		if (addApplicationDialog.getParent() != null) {
			getWindow().showNotification(
					"The 'Add Application Dialog' is already open. Please complete the form and then click on the 'Add' button to add a new application.",
					Notification.TYPE_WARNING_MESSAGE);
		} else {
			getWindow().addWindow(addApplicationDialog);
		}
	}

	@Override
	public synchronized void applicationCommitted(final Application application) {
		try {
			// add the valid application in the data source
			containerDataSource.addApplication(application);
		} catch (Exception e) {
			getWindow().showNotification("Could not add application", e.getMessage(), Notification.TYPE_ERROR_MESSAGE);
		}
	}

	// deletes the selected application
	private synchronized void deleteButtonClicked() {
		// since multiselect is enabled, we get a set of the selected values
		final Set<?> selectedRowIds = (Set<?>) table.getValue();
		if (CollectionUtils.isNotEmpty(selectedRowIds)) {
			// the app will be deleted from the container via callbacks
			try {
				for (final Object selectedRowId : selectedRowIds) {
					table.removeItem(selectedRowId.toString());
				}
			} catch (Exception e) {
				getWindow().showNotification("Could not delete applications", e.getMessage(),
						Notification.TYPE_ERROR_MESSAGE);
			}
		} else {
			getWindow().showNotification("Please select at least one application to delete",
					Notification.TYPE_WARNING_MESSAGE);
		}
	}

	// saves all changes
	private void saveButtonClicked() {
		final Collection<String> validationErrors;
		synchronized (this) {
			validationErrors = containerDataSource.getValidationErrors();
			if (validationErrors.isEmpty()) {
				try {
					// the container will figure out which rows are dirty
					containerDataSource.saveDirtyItems();
				} catch (Exception e) {
					getWindow().showNotification("Could not save changes", e.getMessage(),
							Notification.TYPE_ERROR_MESSAGE);
				}
			}
		}
		// check if we need to display something
		if (!validationErrors.isEmpty()) {
			displayValidationErrors(validationErrors);
		}
	}

	private void displayValidationErrors(final Collection<String> validationErrors) {
		final StringBuilder errorDisplay = new StringBuilder();
		errorDisplay.append("The following errors occured while validating your changes:<br><ul>");
		for (final String validationError : validationErrors) {
			errorDisplay.append("<li>").append(validationError);
		}
		errorDisplay.append("</ul>");
		getWindow().showNotification("Could not save changes", errorDisplay.toString(), Notification.TYPE_ERROR_MESSAGE,
				true);
	}

}
