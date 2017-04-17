package com.workflowconversion.portlet.ui.table.resource;

import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.core.resource.Queue;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.ui.ConfirmationDialog;
import com.workflowconversion.portlet.ui.ConfirmationDialogCloseListener;
import com.workflowconversion.portlet.ui.HorizontalSeparator;
import com.workflowconversion.portlet.ui.table.AbstractGenericElementDetailDialog;
import com.workflowconversion.portlet.ui.table.GenericElementDetailsCommittedListener;
import com.workflowconversion.portlet.ui.table.Size;
import com.workflowconversion.portlet.ui.table.TableWithControls;
import com.workflowconversion.portlet.ui.table.application.ApplicationsTable.ApplicationsTableFactory;
import com.workflowconversion.portlet.ui.table.queue.QueueTable.QueueTableFactory;

/**
 * Dialog to see/edit Applications and Queues of a resource.
 * 
 * @author delagarza
 *
 */
public class ResourceDetaislDialog extends AbstractGenericElementDetailDialog<Resource> {
	private static final long serialVersionUID = 3316803946008323297L;

	/**
	 * Constructor.
	 * 
	 * @param itemId
	 *            the id of the item whose details will be displayed.
	 * @param resource
	 *            the resource which details will be shown.
	 * @param listener
	 *            a listener to invoke when details are saved.
	 * @param allowEdition
	 *            whether the tables can be edited.
	 */
	public ResourceDetaislDialog(final Object itemId, final Resource resource,
			final GenericElementDetailsCommittedListener<Resource> listener, final boolean allowEdition) {
		super(itemId, resource, listener, allowEdition);
		setUp();
	}

	private void setUp() {
		setUpDialog();
		setUpLayout();
	}

	private void setUpDialog() {
		setCaption("Details of resource [" + super.element.getName() + "], type [" + super.element.getType() + ']');
	}

	private void setUpLayout() {
		final CheckBox enableEditionCheckBox = new CheckBox("Enable edition", false);
		enableEditionCheckBox.setImmediate(true);
		enableEditionCheckBox.setEnabled(allowEdition);

		final Button closeButton = new Button("Close");
		closeButton.setImmediate(true);
		final Button saveButton = new Button("Save");
		saveButton.setImmediate(true);
		saveButton.setEnabled(false);
		saveButton.setDisableOnClick(true);

		final ApplicationsTableFactory applicationsTableFactory = new ApplicationsTableFactory();
		applicationsTableFactory.withOwningResource(super.element).allowEdition(super.element.canModifyApplications())
				.withTitle("Applications");
		final TableWithControls<Application> applicationsTable = applicationsTableFactory.newInstance();
		applicationsTable.init(super.element.getApplications());

		final QueueTableFactory queueTableFactory = new QueueTableFactory();
		queueTableFactory.withTitle("Queues");
		final TableWithControls<Queue> queueTable = queueTableFactory.newInstance();
		queueTable.init(super.element.getQueues());

		if (allowEdition) {
			enableEditionCheckBox.addValueChangeListener(new Property.ValueChangeListener() {
				private static final long serialVersionUID = -7084690551560062117L;

				@Override
				public void valueChange(final ValueChangeEvent event) {
					final boolean enableEdition = Boolean.parseBoolean(event.getProperty().getValue().toString());
					applicationsTable.setReadOnly(!enableEdition);
					queueTable.setReadOnly(!enableEdition);
					saveButton.setEnabled(enableEdition);
				}
			});
		}

		closeButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 9168543231461290544L;

			@Override
			public void buttonClick(final ClickEvent event) {
				closeButtonClicked(applicationsTable, queueTable);
			}
		});

		saveButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 4969931319627846349L;

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					applicationsTable.clearSelection();
					queueTable.clearSelection();
					saveChanges(applicationsTable, queueTable);
				} finally {
					saveButton.setEnabled(true);
				}
			}
		});

		final HorizontalLayout footerLayout = new HorizontalLayout();
		footerLayout.setWidth(100, Unit.PERCENTAGE);

		footerLayout.addComponent(enableEditionCheckBox);
		footerLayout.setComponentAlignment(enableEditionCheckBox, Alignment.BOTTOM_LEFT);

		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.addComponent(saveButton);
		buttonLayout.addComponent(closeButton);

		footerLayout.addComponent(buttonLayout);
		footerLayout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_RIGHT);

		final Size applicationsTableSize = applicationsTable.getSize();
		final TabSheet tabSheet = new TabSheet();
		// we know the app table is bigger than the queue table
		tabSheet.setWidth(applicationsTableSize.width, applicationsTableSize.widthUnit);
		tabSheet.addTab(new VerticalLayout(applicationsTable), "Applications");
		tabSheet.addTab(new VerticalLayout(queueTable), "Queues");
		final VerticalLayout tabSheetLayout = new VerticalLayout();
		tabSheetLayout.addComponent(tabSheet);

		final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setMargin(true);
		mainLayout.addComponent(tabSheetLayout);
		mainLayout.addComponent(new HorizontalSeparator());
		mainLayout.addComponent(footerLayout);
		setContent(mainLayout);
	}

	private void closeButtonClicked(final TableWithControls<Application> applicationsTable,
			final TableWithControls<Queue> queueTable) {
		if (applicationsTable.isDirty() || queueTable.isDirty()) {
			final ConfirmationDialogCloseListener listener = new ConfirmationDialogCloseListener() {
				@Override
				public void confirmationDialogClose(final boolean response) {
					if (response) {
						ResourceDetaislDialog.this.close();
					}
				}
			};

			final ConfirmationDialog confirmationDialog = new ConfirmationDialog(
					"There are unsaved changes. Do you want to proceed without saving?", listener);
			confirmationDialog.display();
		} else {
			ResourceDetaislDialog.this.close();
		}
	}

	private void saveChanges(final TableWithControls<Application> applicationsTable,
			final TableWithControls<Queue> queueTable) {
		if (applicationsTable.isDirty() || queueTable.isDirty()) {
			applicationsTable.saveAllChanges();
			queueTable.saveAllChanges();
			listener.elementDetailsCommitted(super.itemId, super.element);
		}
	}
}
