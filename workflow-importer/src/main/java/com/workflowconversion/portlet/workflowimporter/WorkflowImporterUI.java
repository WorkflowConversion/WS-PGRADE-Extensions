package com.workflowconversion.portlet.workflowimporter;

import com.vaadin.data.Item;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;
import com.workflowconversion.portlet.core.settings.Settings;
import com.workflowconversion.portlet.ui.HorizontalSeparator;
import com.workflowconversion.portlet.ui.WorkflowConversionUI;

/**
 * Entry point for this portlet.
 * 
 * @author delagarza
 *
 */
public class WorkflowImporterUI extends WorkflowConversionUI {

	private static final String PROPERTY_NAME_CAPTION = "workflow.name";
	private static final long serialVersionUID = 712483663690909775L;

	/**
	 * Constructor.
	 */
	public WorkflowImporterUI() {
		super(Settings.getInstance().getPortletSanityCheck(), Settings.getInstance().getApplicationProviders());

	}

	@Override
	protected Layout prepareContent() {

		final ComboBox workflowComboBox = getWorkflowComboBox();
		final Button importButton = createButton("Import...", "Import a workflow");

		final HorizontalLayout comboBoxLayout = new HorizontalLayout();
		comboBoxLayout.setSpacing(true);
		comboBoxLayout.setMargin(false);
		comboBoxLayout.addComponent(workflowComboBox);
		comboBoxLayout.addComponent(importButton);
		comboBoxLayout.setComponentAlignment(workflowComboBox, Alignment.BOTTOM_LEFT);
		comboBoxLayout.setComponentAlignment(importButton, Alignment.BOTTOM_RIGHT);

		final VerticalLayout workflowDetailsLayout = new VerticalLayout();
		// ******
		final Label titleLabel = new Label("The name of the workflow");
		workflowDetailsLayout.addComponent(titleLabel);

		// add some fake jobs
		for (int i = 0; i < 6; i++) {

			final Label jobNameLabel = new Label("Job_" + i);

			final ComboBox binaryComboBox = new ComboBox();
			binaryComboBox.setCaptionAsHtml(true);
			binaryComboBox.setCaption("<h3>Binary</h3>");
			binaryComboBox.setNullSelectionAllowed(false);
			binaryComboBox.setImmediate(true);
			binaryComboBox.setDescription("Select a binary for this job");
			binaryComboBox.setInputPrompt("Select a binary for this job");
			binaryComboBox.setWidth(350, Unit.PIXELS);
			binaryComboBox.addContainerProperty("binary.name", String.class, null);
			binaryComboBox.setItemCaptionPropertyId("binary.name");
			final Item item = binaryComboBox.addItem(i);
			item.getItemProperty("binary.name").setValue("Some program running somewhere");

			final ComboBox queueComboBox = new ComboBox();
			queueComboBox.setCaptionAsHtml(true);
			queueComboBox.setCaption("<h3>Queue</h3>");
			queueComboBox.setNullSelectionAllowed(false);
			queueComboBox.setImmediate(true);
			queueComboBox.setDescription("Select a queue for this job");
			queueComboBox.setInputPrompt("Select a queue for this job");
			queueComboBox.setWidth(150, Unit.PIXELS);
			queueComboBox.addContainerProperty("queue.name", String.class, null);
			queueComboBox.setItemCaptionPropertyId("queue.name");
			final Item queueItem = queueComboBox.addItem(i);
			queueItem.getItemProperty("queue.name").setValue("fast");

			final HorizontalLayout jobDetailsLayout = new HorizontalLayout();
			jobDetailsLayout.setMargin(false);
			jobDetailsLayout.setSpacing(true);
			jobDetailsLayout.addComponent(jobNameLabel);
			jobDetailsLayout.addComponent(binaryComboBox);
			jobDetailsLayout.addComponent(queueComboBox);
			jobDetailsLayout.setComponentAlignment(jobNameLabel, Alignment.MIDDLE_LEFT);
			jobDetailsLayout.setComponentAlignment(binaryComboBox, Alignment.BOTTOM_CENTER);
			jobDetailsLayout.setComponentAlignment(queueComboBox, Alignment.BOTTOM_RIGHT);

			workflowDetailsLayout.addComponent(jobDetailsLayout);
		}
		// ******

		final Button saveButton = createButton("Save", "Save changes");
		final Button saveAsButton = createButton("Save as...", "Save current workflow under a different name");
		final Button copyButton = createButton("Export...", "Export current workflow");
		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.setMargin(false);
		buttonLayout.addComponent(saveButton);
		buttonLayout.addComponent(saveAsButton);
		buttonLayout.addComponent(copyButton);

		final Layout mainLayout = new VerticalLayout();
		mainLayout.addComponent(comboBoxLayout);
		mainLayout.addComponent(new HorizontalSeparator());
		mainLayout.addComponent(workflowDetailsLayout);
		mainLayout.addComponent(new HorizontalSeparator());
		mainLayout.addComponent(buttonLayout);

		return mainLayout;
	}

	private ComboBox getWorkflowComboBox() {
		final ComboBox workflowComboBox = new ComboBox();
		workflowComboBox.setCaptionAsHtml(true);
		workflowComboBox.setCaption("<h3>Workflow</h3>");
		workflowComboBox.setNullSelectionAllowed(false);
		workflowComboBox.setImmediate(true);
		workflowComboBox.setDescription("Select a workflow to edit");
		workflowComboBox.setInputPrompt("Select a workflow to edit");
		workflowComboBox.setWidth(650, Unit.PIXELS);

		workflowComboBox.addContainerProperty(PROPERTY_NAME_CAPTION, String.class, null);
		workflowComboBox.setItemCaptionPropertyId(PROPERTY_NAME_CAPTION);

		return workflowComboBox;
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

}
