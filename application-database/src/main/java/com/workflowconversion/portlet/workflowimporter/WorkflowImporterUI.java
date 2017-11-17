package com.workflowconversion.portlet.workflowimporter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.workflowconversion.portlet.core.exception.ApplicationException;
import com.workflowconversion.portlet.core.resource.ResourceProvider;
import com.workflowconversion.portlet.core.settings.Settings;
import com.workflowconversion.portlet.core.workflow.Job;
import com.workflowconversion.portlet.core.workflow.Workflow;
import com.workflowconversion.portlet.core.workflow.WorkflowManager;
import com.workflowconversion.portlet.core.workflow.WorkflowManagerFactory;
import com.workflowconversion.portlet.ui.HorizontalSeparator;
import com.workflowconversion.portlet.ui.NotificationUtils;
import com.workflowconversion.portlet.ui.WorkflowConversionUI;
import com.workflowconversion.portlet.ui.workflow.WorkflowView;
import com.workflowconversion.portlet.ui.workflow.upload.WorkflowUploadDialog;
import com.workflowconversion.portlet.ui.workflow.upload.WorkflowUploadListener;

/**
 * Entry point for this portlet.
 * 
 * @author delagarza
 *
 */
public class WorkflowImporterUI extends WorkflowConversionUI {
	private static final long serialVersionUID = 712483663690909775L;

	private static final String PROPERTY_NAME_CAPTION = "WorkflowImporterUI_property_name";

	private static final Logger LOG = LoggerFactory.getLogger(WorkflowImporterUI.class);

	private final Map<Integer, WorkflowView> workflowViewMap;
	private WorkflowManager workflowManager;

	/**
	 * Constructor.
	 */
	public WorkflowImporterUI() {
		super(Settings.getInstance().getPortletSanityCheck(), Settings.getInstance().getResourceProviders());
		workflowViewMap = new TreeMap<Integer, WorkflowView>();

	}

	@Override
	protected Layout prepareContent() {
		final WorkflowManagerFactory workflowManagerFactory = Settings.getInstance().getWorkflowManagerFactory();
		workflowManagerFactory.withPortletUser(currentUser);
		workflowManagerFactory.withResourceProviders(Settings.getInstance().getResourceProviders());
		workflowManager = workflowManagerFactory.newInstance();
		workflowManager.init();

		final ComboBox workflowComboBox = getWorkflowComboBox();
		final Button importButton = createButton("Import...", "Import a workflow");

		// fill the combobox with workflows
		for (final Workflow workflow : workflowManager.getImportedWorkflows()) {
			addWorkflowToComboBox(workflow, workflowComboBox, Settings.getInstance().getResourceProviders());
		}

		final VerticalLayout workflowDetailsLayout = new VerticalLayout();

		workflowComboBox.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = 2950115407239159416L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				workflowDetailsLayout.removeAllComponents();
				// check for nulls in case an item was removed
				if (event != null && event.getProperty() != null && event.getProperty().getValue() != null) {
					final int workflowViewId = (int) event.getProperty().getValue();
					workflowDetailsLayout.addComponent(workflowViewMap.get(workflowViewId));
				}
				workflowDetailsLayout.markAsDirtyRecursive();
			}
		});

		final HorizontalLayout comboBoxLayout = new HorizontalLayout();
		comboBoxLayout.setSpacing(true);
		comboBoxLayout.setMargin(false);
		comboBoxLayout.addComponent(workflowComboBox);
		comboBoxLayout.addComponent(importButton);
		comboBoxLayout.setComponentAlignment(workflowComboBox, Alignment.BOTTOM_LEFT);
		comboBoxLayout.setComponentAlignment(importButton, Alignment.BOTTOM_RIGHT);

		final Button saveButton = createButton("Save All", "Save changes");
		final Button exportButton = createButton("Export...", "Export current workflow");
		final Button deleteButton = createButton("Delete", "Delete current workflow");
		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.setMargin(false);
		buttonLayout.addComponent(saveButton);
		buttonLayout.addComponent(exportButton);
		buttonLayout.addComponent(deleteButton);

		importButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 6816115357145595183L;

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					importButtonClicked(Settings.getInstance().getResourceProviders(), workflowComboBox);
				} finally {
					importButton.setEnabled(true);
				}
			}
		});

		saveButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 7508331027519882976L;

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					saveButtonClicked();
				} finally {
					saveButton.setEnabled(true);
				}
			}
		});

		exportButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = -4658323736597834818L;

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					if (workflowComboBox.getValue() == null) {
						NotificationUtils.displayMessage("Please select a workflow to export.");
					}
				} finally {
					exportButton.setEnabled(true);
				}
			}
		});
		final FileDownloader fileDownloader = new FileDownloader(createWorkflowStreamResource(workflowComboBox));
		fileDownloader.extend(exportButton);

		deleteButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = -3385835445931117051L;

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					deleteButtonClicked(workflowComboBox);
				} finally {
					deleteButton.setEnabled(true);
				}
			}
		});

		final Layout mainLayout = new VerticalLayout();
		mainLayout.addComponent(comboBoxLayout);
		mainLayout.addComponent(new HorizontalSeparator());
		mainLayout.addComponent(workflowDetailsLayout);
		mainLayout.addComponent(buttonLayout);

		return mainLayout;
	}

	private void importButtonClicked(final Collection<ResourceProvider> resourceProviders,
			final ComboBox workflowComboBox) {
		final Window importWorkflowDialog = new WorkflowUploadDialog(new WorkflowUploadListener() {
			@Override
			public void workflowUploaded(final File location) {
				final Workflow uploadedWorkflow = workflowManager.importWorkflow(location);
				addWorkflowToComboBox(uploadedWorkflow, workflowComboBox, resourceProviders);
				NotificationUtils.displayTrayMessage(
						"The workflow was uploaded successfully. Don't forget to commit your changes by clicking on the [Save All] button.");

			}

			@Override
			public void uploadFailed(final Exception reason) {
				NotificationUtils.displayError("There was an error adding the uploaded workflow to the staging area.",
						reason);
			}
		});
		UI.getCurrent().addWindow(importWorkflowDialog);
	}

	private ComboBox getWorkflowComboBox() {
		final ComboBox workflowComboBox = getComboBox("Workflow", "Select a workflow to edit");
		workflowComboBox.setWidth(650, Unit.PIXELS);

		workflowComboBox.addContainerProperty(PROPERTY_NAME_CAPTION, String.class, null);
		workflowComboBox.setItemCaptionPropertyId(PROPERTY_NAME_CAPTION);

		return workflowComboBox;
	}

	private ComboBox getComboBox(final String caption, final String description) {
		final ComboBox comboBox = new ComboBox();
		comboBox.setCaptionAsHtml(true);
		comboBox.setCaption("<h3>" + caption + "</h3>");
		comboBox.setNullSelectionAllowed(false);
		comboBox.setImmediate(true);
		comboBox.setDescription(description);
		comboBox.setInputPrompt(description);
		return comboBox;
	}

	@SuppressWarnings("unchecked")
	private void addWorkflowToComboBox(final Workflow workflow, final ComboBox workflowComboBox,
			final Collection<ResourceProvider> resourceProviders) {
		final int id = workflowViewMap.size();
		final Item item = workflowComboBox.addItem(id);
		item.getItemProperty(PROPERTY_NAME_CAPTION).setValue(workflow.getName());
		workflowViewMap.put(id, new WorkflowView(workflow, resourceProviders));
	}

	private void saveButtonClicked() {
		final StringBuilder error = new StringBuilder();
		for (final WorkflowView workflowView : workflowViewMap.values()) {
			final Workflow workflow = workflowView.getWorkflow();
			final Collection<Job> unsupportedJobs = workflowManager.getUnsupportedJobs(workflow);
			if (!unsupportedJobs.isEmpty()) {
				appendUnsupportedJobsErrorMessage(workflow, unsupportedJobs, error);
			}
			workflowManager.saveWorkflow(workflow);
		}
		workflowManager.commitChanges();
		if (error.length() > 0) {
			NotificationUtils.displayWarning(error.toString());
		}
	}

	private void appendUnsupportedJobsErrorMessage(final Workflow workflow, final Collection<Job> unsupportedJobs,
			final StringBuilder error) {
		Validate.notEmpty(unsupportedJobs,
				"Expected an empty collection. This is a coding problem and should be reported.");
		error.append("The workflow with name <br/>").append(workflow.getName())
				.append("<br/>contains the following unsupported resource types:<ul>");
		for (final Job unsupportedJob : unsupportedJobs) {
			error.append("<li>Job ").append(unsupportedJob.getName());
			final String resourceType = unsupportedJob.getResourceType();
			if (StringUtils.isBlank(resourceType)) {
				error.append(" contains no resource type.");
			} else {
				error.append(" of type ").append(resourceType);
			}
		}
		error.append("</ul><br>");
	}

	private Button createButton(final String caption, final String description) {
		final Button button = new Button();
		button.setCaption(caption);
		button.setDescription(description);
		button.setEnabled(true);
		button.setDisableOnClick(true);
		button.setImmediate(true);
		return button;
	}

	private void deleteButtonClicked(final ComboBox workflowComboBox) {
		if (workflowComboBox.getValue() != null) {
			final int workflowViewId = (Integer) workflowComboBox.getValue();
			final WorkflowView workflowView = workflowViewMap.get(workflowViewId);
			workflowManager.deleteWorkflow(workflowView.getWorkflow());
			workflowViewMap.remove(workflowViewId);
			workflowComboBox.removeItem(workflowViewId);
		} else {
			NotificationUtils.displayMessage("Please select a workflow to delete.");
		}
	}

	private Workflow getSelectedWorkflow(final ComboBox workflowComboBox) {
		final int workflowViewId = (int) workflowComboBox.getValue();
		final WorkflowView workflowView = workflowViewMap.get(workflowViewId);
		return workflowView.getWorkflow();
	}

	private StreamResource createWorkflowStreamResource(final ComboBox workflowComboBox) {
		return new StreamResource(new StreamSource() {
			private static final long serialVersionUID = -482825331518184714L;

			@Override
			public InputStream getStream() {
				try {
					if (LOG.isInfoEnabled()) {
						LOG.info("Creating StreamResource to download workflow");
					}
					if (workflowComboBox.getValue() != null) {
						final Workflow workflow = getSelectedWorkflow(workflowComboBox);
						return new BufferedInputStream(Files.newInputStream(workflow.getArchivePath()));
					}
				} catch (final IOException e) {
					throw new ApplicationException("Could not download workflow. Reason: " + e.getMessage(), e);
				}
				return null;

			}
		}, "workflow.zip");
	}
}
