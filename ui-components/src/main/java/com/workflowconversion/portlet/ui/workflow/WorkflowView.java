package com.workflowconversion.portlet.ui.workflow;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.util.IndexedContainer;
import com.vaadin.terminal.Resource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window.Notification;
import com.workflowconversion.portlet.core.settings.Settings;
import com.workflowconversion.portlet.core.user.PortletUser;
import com.workflowconversion.portlet.core.workflow.Workflow;
import com.workflowconversion.portlet.core.workflow.WorkflowProvider;
import com.workflowconversion.portlet.ui.workflow.export.WorkflowExportDialog;
import com.workflowconversion.portlet.ui.workflow.upload.WorkflowUploadDialog;
import com.workflowconversion.portlet.ui.workflow.upload.WorkflowUploadedListener;

/**
 * Class containing all of the ui components needed to upload, save, delete, export workflows.
 * 
 * @author delagarza
 *
 */
public class WorkflowView extends VerticalLayout implements WorkflowUploadedListener {

	private static final String NOTIFICATION_TITLE = "Workflow Staging Area";

	private static final long serialVersionUID = 3843347539780676302L;

	private final static Logger LOG = LoggerFactory.getLogger(WorkflowView.class);

	private static final String WF_PROPERTY_ID = "wfid";
	private static final String WF_PROPERTY_NAME = "wfname";
	private static final String WF_PROPERTY_STATUS = "wfstatus";

	private final Table workflowTable;
	private final PortletUser portletUser;
	private final VerticalLayout selectedWorkflowDisplayLayout;
	private final Map<String, Workflow> currentlyDisplayedWorkflows;
	private final Set<String> dirtyWorkflowsId;
	private final WorkflowProvider workflowProvider;

	/**
	 * Constructor.
	 * 
	 * @param portletUser
	 *            the user requesting this view. the workflow provider for the user requesting this view.
	 * @param workflowExporter
	 *            the workflow exporter for the user requesting this view.
	 */
	public WorkflowView(final PortletUser portletUser) {
		Validate.notNull(portletUser, "portletUser cannot be null");
		this.portletUser = portletUser;

		this.dirtyWorkflowsId = new TreeSet<String>();
		this.workflowProvider = Settings.getInstance().getWorkflowProviderFactory().withPortletUser(portletUser)
				.newWorkflowProvider();
		this.currentlyDisplayedWorkflows = new TreeMap<String, Workflow>();
		this.workflowTable = new Table("Your workflows");
		// this.workflowTableContainer = new IndexedContainer();
		this.selectedWorkflowDisplayLayout = new VerticalLayout();

		getInitialWorkflows();
		initUI();
	}

	private void getInitialWorkflows() {
		for (final Workflow stagedWorkflow : workflowProvider.getStagedWorkflows()) {
			currentlyDisplayedWorkflows.put(stagedWorkflow.getId(), stagedWorkflow);
		}
	}

	private void initUI() {
		final Button uploadButton = createButton("Upload...", "Click to upload a workflow");
		final Button saveButton = createButton("Save", "Save changes");
		final Button deleteButton = createButton("Delete", "Delete selected workflows");
		final Button exportButton = createButton("Export...", "Export selected workflow");

		uploadButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 3920929249905705698L;

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					uploadButtonClick();
				} finally {
					uploadButton.setEnabled(true);
				}
			}
		});
		saveButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 6892804972706972688L;

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					saveButtonClick();
				} finally {
					saveButton.setEnabled(true);
				}
			}
		});
		deleteButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = 7562987128061971035L;

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					deleteButtonClick();
				} finally {
					deleteButton.setEnabled(true);
				}
			}
		});
		exportButton.addListener(new Button.ClickListener() {
			private static final long serialVersionUID = -5910585413586746243L;

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					exportButtonClick();
				} finally {
					exportButton.setEnabled(true);
				}
			}
		});

		final IndexedContainer workflowTableContainer = new IndexedContainer();
		workflowTableContainer.addContainerProperty(WF_PROPERTY_ID, String.class, null);
		workflowTableContainer.addContainerProperty(WF_PROPERTY_STATUS, Resource.class, null);
		workflowTableContainer.addContainerProperty(WF_PROPERTY_NAME, String.class, null);
		fillContainerWithWorkflows(workflowTableContainer);
		workflowTable.setContainerDataSource(workflowTableContainer);
		workflowTable.addStyleName("stagingAreaWorkflowTable");
		workflowTable.setReadOnly(true);
		workflowTable.setImmediate(true);
		workflowTable.setMultiSelect(true);

		final HorizontalSplitPanel splitPanel = new HorizontalSplitPanel();
		splitPanel.setFirstComponent(workflowTable);
		splitPanel.setSecondComponent(selectedWorkflowDisplayLayout);

		final Layout buttonLayout = new HorizontalLayout();
		buttonLayout.addComponent(uploadButton);
		buttonLayout.addComponent(saveButton);
		buttonLayout.addComponent(deleteButton);
		buttonLayout.addComponent(exportButton);

		super.addComponent(buttonLayout);
	}

	private void fillContainerWithWorkflows(final IndexedContainer workflowTableContainer) {

	}

	protected void exportButtonClick() {
		final Set<?> selectedItems = (Set<?>) workflowTable.getValue();
		if (selectedItems.size() == 1) {
			final Workflow workflowToExport = currentlyDisplayedWorkflows.get(selectedItems.iterator().next());
			final WorkflowExportDialog workflowExportDialog = new WorkflowExportDialog(workflowToExport, portletUser);
			getWindow().addWindow(workflowExportDialog);

		} else {
			getWindow().showNotification(NOTIFICATION_TITLE, "Please select a single workflow to export.",
					Notification.TYPE_WARNING_MESSAGE);
		}
	}

	protected void deleteButtonClick() {
		final Set<?> selectedItems = (Set<?>) workflowTable.getValue();
		if (selectedItems.isEmpty()) {
			getWindow().showNotification(NOTIFICATION_TITLE, "Please select at least one workflow to delete.",
					Notification.TYPE_WARNING_MESSAGE);
		} else {
			final Collection<String> errors = new LinkedList<String>();
			for (final Object workflowId : selectedItems) {
				try {
					workflowProvider.deleteWorkflow(currentlyDisplayedWorkflows.get((String) workflowId));
					workflowTable.removeItem(workflowId);
				} catch (Exception e) {
					LOG.error("Could not delete workflow with id: " + workflowId, e);
					errors.add("Workflow with id " + workflowId + " could not be removed, reason: " + e.getMessage());
				}
			}
			displayErrorsAsUnorderedList("The following errors occurred while removing workflows:", errors);
		}
	}

	protected void saveButtonClick() {
		final Set<String> savedWorkflowsId = new TreeSet<String>();
		final Collection<String> errors = new LinkedList<String>();
		for (final String workflowId : dirtyWorkflowsId) {
			try {
				savedWorkflowsId.add(workflowId);
				workflowProvider.deleteWorkflow(currentlyDisplayedWorkflows.get(workflowId));
			} catch (Exception e) {
				LOG.error("Could not save workflow with id " + workflowId, e);
				errors.add("Workflow with id " + workflowId + " could not be saved, reason: " + e.getMessage());
			}
		}
		dirtyWorkflowsId.removeAll(savedWorkflowsId);
		displayErrorsAsUnorderedList("The following errors occurred while saving changes:", errors);
	}

	private void displayErrorsAsUnorderedList(final String caption, final Collection<String> errors) {
		if (!errors.isEmpty()) {
			final StringBuilder errorMessage = new StringBuilder(caption + "<ul>");
			for (final String error : errors) {
				errorMessage.append("<li>");
				errorMessage.append(error);
			}
			errorMessage.append("</ul>");
			getWindow().showNotification(NOTIFICATION_TITLE, errorMessage.toString(), Notification.TYPE_ERROR_MESSAGE,
					true);
		}
	}

	protected void uploadButtonClick() {
		final WorkflowUploadDialog workflowUploadDialog = new WorkflowUploadDialog(this);
		if (workflowUploadDialog.getParent() != null) {
			getWindow().showNotification(NOTIFICATION_TITLE, "The 'Workflow Upload Dialog' is already open.",
					Notification.TYPE_WARNING_MESSAGE);
		} else {
			getWindow().addWindow(workflowUploadDialog);
		}
	}

	private Button createButton(final String caption, final String description) {
		final Button button = new Button(caption);
		button.setDescription(description);
		button.setDisableOnClick(true);
		button.setImmediate(true);
		return button;
	}

	@Override
	public void workflowUploaded(final File location) {
		try {
			final Workflow newWorkflow = workflowProvider.importToStagingArea(location);
			// TODO: 1. validate the location contains a valid workflow file
			// TODO: 2. extract a Workflow object from the file
			// TODO: 3. move the file to the user's staging area
			// TODO: 3.1 set an icon based on the workflow status
			// TODO: 4. add the workflow using the workflowprovider
		} catch (IOException e) {

		}
	}

}
