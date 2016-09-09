package com.workflowconversion.importer.guse.vaadin.ui;

import org.apache.commons.lang.Validate;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.workflowconversion.importer.guse.appdb.Application;
import com.workflowconversion.importer.guse.appdb.ApplicationProvider;
import com.workflowconversion.importer.guse.middleware.MiddlewareProvider;
import com.workflowconversion.importer.guse.permission.PermissionManager;
import com.workflowconversion.importer.guse.user.PortletUser;

/**
 * Panel containing the applications to be displayed in a table, plus controls to add/save applications.
 */
class ApplicationsViewPanel extends Panel {

	private static final long serialVersionUID = -5169354278787921392L;

	private Window parentWindow;
	private final Table table;
	private final ApplicationTableContainer containerDataSource;

	/**
	 * Constructor.
	 * 
	 * @param parentWindow
	 *            the parent UI window that contains this control.
	 * @param user
	 *            the user accessing this view.
	 * @param permissionManager
	 *            the permission manager.
	 * @param middlewareProvider
	 *            the middleware provider.
	 * @param applicationProvider
	 *            the application provider to interact with the local storage.
	 */
	ApplicationsViewPanel(final Window parentWindow, final PortletUser user, final PermissionManager permissionManager,
			final MiddlewareProvider middlewareProvider, final ApplicationProvider applicationProvider) {
		Validate.notNull(parentWindow, "parentWindow cannot be null");
		Validate.notNull(user, "user cannot be null");
		Validate.notNull(permissionManager, "permissionManager cannot be null");
		Validate.notNull(middlewareProvider, "middlewareProvider cannot be null");
		Validate.notNull(applicationProvider, "applicationProvider cannot be null");
		this.parentWindow = parentWindow;
		this.containerDataSource = new ApplicationTableContainer(middlewareProvider, applicationProvider);
		this.table = new Table(applicationProvider.getName(), containerDataSource);
		setUpUiElements(permissionManager.hasWriteAccess(user) && applicationProvider.isEditable());
	}

	private void setUpUiElements(final boolean withEditControls) {
		setUpTable();
		final Layout layout = new VerticalLayout();
		// add the table
		layout.addComponent(table);
		// add the controls based on permissions and the application provider
		if (withEditControls) {
			setUpEditControls(layout);
		}
		this.setContent(layout);
	}

	private void setUpEditControls(final Layout parentLayout) {
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

		final Button addButton = createButton("Add", "Add application");
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

		final CheckBox editableCheckBox = new CheckBox("Editable", false);
		editableCheckBox.setDescription("Enable edition");
		editableCheckBox.addListener(new ClickListener() {
			private static final long serialVersionUID = 6802078670856773823L;

			@Override
			public void buttonClick(final ClickEvent event) {
				final boolean enabled = event.getButton().booleanValue();
				table.setEditable(enabled);
				saveButton.setEnabled(enabled);
				deleteButton.setEnabled(enabled);
			}
		});

		final Layout layout = new HorizontalLayout();
		layout.addComponent(editableCheckBox);
		layout.addComponent(saveButton);
		layout.addComponent(addButton);
		layout.addComponent(deleteButton);

		parentLayout.addComponent(layout);
	}

	protected Button createButton(final String caption, final String description) {
		final Button button = new Button(caption);
		button.setDescription(description);
		button.setEnabled(false);
		button.setDisableOnClick(true);
		return button;
	}

	protected void addButtonClicked() {
		// display a window with the application fields
		final Window addApplicationDialog = new Window("Add application");
		addApplicationDialog.setModal(true);

		parentWindow.addWindow(addApplicationDialog);

		this.addComponent(addApplicationDialog);
	}

	protected void deleteButtonClicked() {

	}

	protected void saveButtonClicked() {

	}

	private void setUpTable() {
		table.setSelectable(true);
		table.setMultiSelect(false);
		table.setEditable(false);
		table.setSortDisabled(false);
	}

	/**
	 * Inserts the given application at the end of the table.
	 * 
	 * @param application
	 *            the application to insert.
	 */
	void insertApplication(final Application application) {

	}

	/**
	 * Edits the application on the given index.
	 * 
	 * @param application
	 *            the application that will remain in the table.
	 * @param index
	 *            the index on which the application is located.
	 */
	void editApplication(final Application application, final int index) {

	}

	/**
	 * Removes the application at the given index.
	 * 
	 * @param index
	 *            the index of the application to delete.
	 */
	void removeApplication(final int index) {

	}
}
