package com.workflowconversion.portlet.workflowimporter;

import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.workflowconversion.portlet.core.settings.Settings;
import com.workflowconversion.portlet.ui.WorkflowConversionApplication;

/**
 * Entry point for this portlet.
 * 
 * @author delagarza
 *
 */
public class WorkflowImporterApplication extends WorkflowConversionApplication {

	private static final long serialVersionUID = 712483663690909775L;

	public WorkflowImporterApplication() {
		super(Settings.getInstance().getVaadinTheme(), Settings.getInstance().getPortletSanityCheck(),
				Settings.getInstance().getApplicationProviders());

	}

	@Override
	protected Window prepareMainWindow() {
		// TODO: actually implement this
		final Window window = new Window();
		final Layout layout = new VerticalLayout();
		final CheckBox editableCheckBox = new CheckBox("Editable", false);
		editableCheckBox.setWidth(15, Window.UNITS_EM);
		editableCheckBox.setDescription("Enable edition");
		editableCheckBox.setImmediate(true);
		layout.addComponent(editableCheckBox);
		window.setContent(layout);
		return window;
	}

}
