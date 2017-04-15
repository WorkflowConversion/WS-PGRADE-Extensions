package com.workflowconversion.portlet.ui.workflow.export;

import org.apache.commons.lang.Validate;

import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.workflowconversion.portlet.core.exception.InvalidExportDestinationException;
import com.workflowconversion.portlet.core.settings.Settings;
import com.workflowconversion.portlet.core.user.PortletUser;
import com.workflowconversion.portlet.core.workflow.Workflow;
import com.workflowconversion.portlet.core.workflow.WorkflowExportDestination;
import com.workflowconversion.portlet.core.workflow.WorkflowExporter;
import com.workflowconversion.portlet.core.workflow.WorkflowExporterFactory;
import com.workflowconversion.portlet.ui.HorizontalSeparator;
import com.workflowconversion.portlet.ui.NotificationUtils;

/**
 * Dialog to export workflows from the staging area to either gUSE or to a zip file to be downloaded to the user's
 * computer. This dialog is, by now, unused.
 * 
 * @author delagarza
 *
 */
public class WorkflowExportDialog extends Window {
	private static final long serialVersionUID = 2247174725490181004L;
	private static final String PROPERTY_ID = "id";
	private static final String PROPERTY_CAPTION = "caption";

	private final Workflow workflowToExport;
	private final PortletUser portletUser;
	private final WorkflowExportListener listener;

	/**
	 * Constructor.
	 * 
	 * @param workflowToExport
	 *            the workflow that will be exported.
	 * @param portletUser
	 *            the portlet user requesting this dialog.
	 * @param listener
	 *            the listener interested in events.
	 */
	public WorkflowExportDialog(final Workflow workflowToExport, final PortletUser portletUser,
			final WorkflowExportListener listener) {
		Validate.notNull(workflowToExport,
				"workflowToExport cannot be null. This is a coding problem and should be reported.");
		Validate.notNull(portletUser, "portletUser cannot be null. This is a coding problem and should be reported.");
		Validate.notNull(listener, "listener cannot be null. This is a coding problem and should be reported.");
		this.workflowToExport = workflowToExport;
		this.portletUser = portletUser;
		this.listener = listener;
		setModal(true);
		setCaption("Workflow Export");
		initUI();
	}

	private void initUI() {
		final IndexedContainer exportDestinationContainer = getIndexedContainer();

		final OptionGroup exportDestinationOptionGroup = new OptionGroup("Export destination",
				exportDestinationContainer);
		exportDestinationOptionGroup.setItemCaptionPropertyId(PROPERTY_CAPTION);
		exportDestinationOptionGroup.setDescription("Select an export method");
		exportDestinationOptionGroup.setImmediate(true);
		exportDestinationOptionGroup.setNullSelectionAllowed(false);
		exportDestinationOptionGroup.setMultiSelect(false);
		exportDestinationOptionGroup.select(WorkflowExportDestination.Archive);

		final Button exportButton = new Button("Export");
		exportButton.setDescription("Export your workflow to the selected destination");
		exportButton.setImmediate(true);
		exportButton.setDisableOnClick(true);
		exportButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = -6734041359258651966L;

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					exportButtonClicked((WorkflowExportDestination) exportDestinationOptionGroup.getValue());
				} finally {
					exportButton.setEnabled(true);
				}
			}
		});

		final VerticalLayout layout = new VerticalLayout();
		layout.addComponent(exportDestinationOptionGroup);
		layout.addComponent(new HorizontalSeparator());
		layout.addComponent(exportButton);
		layout.setSpacing(true);
		layout.setMargin(true);
		setContent(layout);
	}

	@SuppressWarnings("unchecked")
	private IndexedContainer getIndexedContainer() {
		final IndexedContainer container = new IndexedContainer();
		container.addContainerProperty(PROPERTY_ID, WorkflowExportDestination.class, null);
		container.addContainerProperty(PROPERTY_CAPTION, String.class, null);

		for (final WorkflowExportDestination destination : WorkflowExportDestination.values()) {
			final Item newItem = container.addItem(destination);
			newItem.getItemProperty(PROPERTY_CAPTION).setValue(destination.getLongCaption());
			newItem.getItemProperty(PROPERTY_ID).setValue(destination);
		}

		return container;
	}

	protected void exportButtonClicked(final WorkflowExportDestination destination) {
		final WorkflowExporterFactory workflowExporterFactory = Settings.getInstance().getWorkflowExporterFactory();
		boolean handled = false;
		switch (destination) {
		case Archive:
			workflowExporterFactory.withDestination(destination);
			handled = true;
			break;
		case LocalRepository:
			NotificationUtils
					.displayWarning("Exporting to the local WS-PGRADE repository is not yet supported. Sorry.");
			// not supported, by now
			break;
		default:
			throw new InvalidExportDestinationException(destination);
		}
		if (handled) {
			final WorkflowExporter workflowExporter = workflowExporterFactory.withPortletUser(portletUser)
					.newInstance();
			try {
				workflowExporter.export(workflowToExport);
			} catch (final Exception e) {
				listener.exportFailed(e);
			}
		}
		// else NOP
	}
}
