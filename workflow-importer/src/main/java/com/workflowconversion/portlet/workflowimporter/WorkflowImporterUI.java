package com.workflowconversion.portlet.workflowimporter;

import com.vaadin.data.Item;
import com.vaadin.ui.Accordion;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
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

		// final VerticalLayout workflowDetailsLayout = new VerticalLayout();
		final Accordion workflowDetailsAccordion = new Accordion();
		// ******
		// add some fake jobs
		for (int i = 0; i < 6; i++) {
			final ComboBox binaryComboBox = new ComboBox();
			binaryComboBox.setCaptionAsHtml(true);
			binaryComboBox.setCaption("<h3>Binary</h3>");
			binaryComboBox.setNullSelectionAllowed(false);
			binaryComboBox.setImmediate(true);
			binaryComboBox.setDescription("Select a binary");
			binaryComboBox.setInputPrompt("Select a binary");
			binaryComboBox.setWidth(450, Unit.PIXELS);
			binaryComboBox.addContainerProperty("binary.name", String.class, null);
			binaryComboBox.setItemCaptionPropertyId("binary.name");
			final Item item = binaryComboBox.addItem(i);
			item.getItemProperty("binary.name").setValue("Some program running somewhere");

			final ComboBox queueComboBox = new ComboBox();
			queueComboBox.setCaptionAsHtml(true);
			queueComboBox.setCaption("<h3>Queue</h3>");
			queueComboBox.setNullSelectionAllowed(false);
			queueComboBox.setImmediate(true);
			queueComboBox.setDescription("Select a queue");
			queueComboBox.setInputPrompt("Select a queue");
			queueComboBox.setWidth(200, Unit.PIXELS);
			queueComboBox.addContainerProperty("queue.name", String.class, null);
			queueComboBox.setItemCaptionPropertyId("queue.name");
			final Item queueItem = queueComboBox.addItem(i);
			queueItem.getItemProperty("queue.name").setValue("fast");

			final HorizontalLayout binarySelectionLayout = new HorizontalLayout();
			binarySelectionLayout.setMargin(false);
			binarySelectionLayout.setSpacing(true);
			binarySelectionLayout.addComponent(binaryComboBox);
			binarySelectionLayout.addComponent(queueComboBox);
			binarySelectionLayout.setComponentAlignment(binaryComboBox, Alignment.TOP_LEFT);
			binarySelectionLayout.setComponentAlignment(queueComboBox, Alignment.TOP_RIGHT);

			// final HorizontalLayout jobNameLayout = new HorizontalLayout();
			// jobNameLayout.setMargin(false);
			// jobNameLayout.setSpacing(true);
			// jobNameLayout.addComponent(jobNameLabel);
			// jobNameLayout.setComponentAlignment(jobNameLabel, Alignment.BOTTOM_LEFT);

			// final VerticalLayout jobDetailsLayout = new VerticalLayout();
			// jobDetailsLayout.setMargin(false);
			// jobDetailsLayout.setSpacing(true);
			// jobDetailsLayout.addComponent(jobNameLayout);
			// jobDetailsLayout.addComponent(binarySelectionLayout);

			workflowDetailsAccordion.addTab(binarySelectionLayout, "Job_" + i);
			// workflowDetailsLayout.addComponent(new HorizontalSeparator());
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
		mainLayout.addComponent(workflowDetailsAccordion);
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
