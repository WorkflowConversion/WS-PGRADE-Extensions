package com.workflowconversion.portlet.workflowimporter;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;
import com.workflowconversion.portlet.core.settings.Settings;
import com.workflowconversion.portlet.ui.WorkflowConversionUI;

/**
 * Entry point for this portlet.
 * 
 * @author delagarza
 *
 */
public class WorkflowImporterUI extends WorkflowConversionUI {

	private static final long serialVersionUID = 712483663690909775L;

	public WorkflowImporterUI() {
		super(Settings.getInstance().getPortletSanityCheck(), Settings.getInstance().getApplicationProviders());

	}

	@Override
	protected Layout prepareContent() {
		// TODO: actually implement this
		final Layout layout = new VerticalLayout();

		final CheckBox editableCheckBox = new CheckBox("Editable", false);
		editableCheckBox.setWidth(15, Unit.EM);
		editableCheckBox.setDescription("Enable edition");
		editableCheckBox.setImmediate(true);
		layout.addComponent(editableCheckBox);

		return layout;
	}

}
