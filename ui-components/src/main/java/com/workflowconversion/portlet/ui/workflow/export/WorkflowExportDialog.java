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

/**
 * Dialog to export workflows from the staging area to either gUSE or to a zip file to be downloaded to the user's
 * computer.
 * 
 * @author delagarza
 *
 */
public class WorkflowExportDialog extends Window {
	private static final long serialVersionUID = 2247174725490181004L;
	private static final String EXPORT_WSPGRADE = "WS-PGRADE local repository";
	private static final String CAPTION_EXPORT_DOWNLOAD = "Download to your computer";
	private static final String PROPERTY_ID = "id";
	private static final String PROPERTY_CAPTION = "caption";

	private final Workflow workflowToExport;
	private final PortletUser portletUser;

	/**
	 * Constructor.
	 * 
	 * @param workflowToExport
	 *            the workflow that will be exported.
	 * @param portletUser
	 *            the portlet user requesting this dialog.
	 */
	public WorkflowExportDialog(final Workflow workflowToExport, final PortletUser portletUser) {
		Validate.notNull(workflowToExport, "workflowToExport cannot be null");
		Validate.notNull(portletUser, "portletUser cannot be null");
		this.workflowToExport = workflowToExport;
		this.portletUser = portletUser;

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
		exportDestinationOptionGroup.select(EXPORT_WSPGRADE);

		final Button exportButton = new Button("Export");
		exportButton.setDescription("Export your workflow to the selected destination");
		exportButton.setImmediate(true);
		exportButton.setDisableOnClick(true);
		exportButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -6734041359258651966L;

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					exportButtonClicked((WorkflowExportDestination) exportDestinationOptionGroup.getValue());
				} finally {
					exportButton.setEnabled(true);
				}
			}
		});

		final VerticalLayout layout = (VerticalLayout) getContent();
		layout.addComponent(exportDestinationOptionGroup);
		layout.addComponent(new HorizontalSeparator());
		layout.addComponent(exportButton);
		layout.setSpacing(true);
		layout.setMargin(true);
	}

	private IndexedContainer getIndexedContainer() {
		final IndexedContainer container = new IndexedContainer();
		container.addContainerProperty(PROPERTY_ID, WorkflowExportDestination.class, null);
		container.addContainerProperty(PROPERTY_CAPTION, String.class, null);

		for (final WorkflowExportDestination destination : WorkflowExportDestination.values()) {
			final String caption;
			switch (destination) {
			case Archive:
				caption = CAPTION_EXPORT_DOWNLOAD;
				break;
			case LocalRepository:
				caption = EXPORT_WSPGRADE;
				break;
			default:
				throw new InvalidExportDestinationException(destination);
			}
			final Item newItem = container.addItem(destination);
			newItem.getItemProperty(PROPERTY_CAPTION).setValue(caption);
			newItem.getItemProperty(PROPERTY_ID).setValue(destination);
		}

		return container;
	}

	protected void exportButtonClicked(final WorkflowExportDestination destination) {
		final WorkflowExporterFactory workflowExporterFactory = Settings.getInstance().getWorkflowExporterFactory();
		switch (destination) {
		case Archive:
		case LocalRepository:
			workflowExporterFactory.withDestination(destination);
			break;
		default:
			throw new InvalidExportDestinationException(destination);
		}
		final WorkflowExporter workflowExporter = workflowExporterFactory.withPortletUser(portletUser)
				.newInstance();
		try {
			workflowExporter.export(workflowToExport);
		} catch (Exception e) {

		}
	}

}
