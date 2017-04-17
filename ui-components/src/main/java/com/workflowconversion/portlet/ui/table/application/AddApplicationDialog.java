package com.workflowconversion.portlet.ui.table.application;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.ui.table.AbstractAddGenericElementDialog;
import com.workflowconversion.portlet.ui.table.GenericElementCommittedListener;

/**
 * Modal dialog to add new applications.
 * 
 * @author delagarza
 *
 */
class AddApplicationDialog extends AbstractAddGenericElementDialog<Application> {

	private static final long serialVersionUID = 2514225841272484511L;

	/**
	 * @param listener
	 *            the listener to be notified when an application is to be added.
	 */
	AddApplicationDialog(final GenericElementCommittedListener<Application> listener) {
		super("Add application", listener);
	}

	@Override
	protected ApplicationWrapper createDefaultElementWrapper() {
		return new ApplicationWrapper();
	}

	@Override
	protected void addAndBindComponents(final FormLayout formLayout, final FieldGroup fieldGroup) {
		// create the input controls
		final TextField applicationName = createRequiredTextField("Application name:",
				"Please enter a name for the application", Application.Field.Name.getMaxLength());
		final TextField version = createRequiredTextField("Version:", "Please enter a valid version",
				Application.Field.Version.getMaxLength());
		final TextField executablePath = createRequiredTextField("Executable path:",
				"Please enter a valid application path", Application.Field.Path.getMaxLength());
		final TextArea description = createOptionalTextArea("Description:",
				Application.Field.Description.getMaxLength());

		formLayout.addComponent(applicationName);
		formLayout.addComponent(version);
		formLayout.addComponent(executablePath);
		formLayout.addComponent(description);

		fieldGroup.bind(applicationName, Application.Field.Name.getMemberName());
		fieldGroup.bind(version, Application.Field.Version.getMemberName());
		fieldGroup.bind(executablePath, Application.Field.Path.getMemberName());
		fieldGroup.bind(description, Application.Field.Description.getMemberName());

	}

}
