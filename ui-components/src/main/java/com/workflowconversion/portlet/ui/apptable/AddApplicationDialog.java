package com.workflowconversion.portlet.ui.apptable;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.vaadin.data.Buffered.SourceException;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.workflowconversion.portlet.core.app.Application;
import com.workflowconversion.portlet.core.app.ApplicationField;
import com.workflowconversion.portlet.core.middleware.MiddlewareProvider;

import dci.data.Middleware;

/**
 * Modal dialog to add new applications.
 * 
 * @author delagarza
 *
 */
class AddApplicationDialog extends Window {

	private static final long serialVersionUID = 2514225841272484511L;

	private static final String LARGE_FIELD_WIDTH = "15em";
	private static final String COMMON_FIELD_WIDTH = "10em";

	private final FieldGroup applicationFieldGroup;
	private final Application application;
	private final MiddlewareProvider middlewareProvider;
	private final ApplicationCommittedListener listener;

	/**
	 * Constructor.
	 * 
	 * @param middlewareProvider
	 *            The middleware provider.
	 */
	AddApplicationDialog(final MiddlewareProvider middlewareProvider, final ApplicationCommittedListener listener) {
		Validate.notNull(middlewareProvider, "middlewareProvider cannot be null");
		Validate.notNull(listener, "listener cannot be null");
		super.setCaption("Add application");
		super.setModal(true);
		this.middlewareProvider = middlewareProvider;
		this.application = newEmptyApplication();
		this.applicationFieldGroup = new FieldGroup();
		this.listener = listener;
		setUpLayout();
	}

	private Application newEmptyApplication() {
		final Application app = new Application();
		app.setId("automatically generated");
		app.setName("");
		app.setDescription("");
		app.setPath("");
		app.setResource("");
		app.setResourceType("");
		app.setVersion("");
		return app;
	}

	void removeFromParentWindow() {
		UI.getCurrent().removeWindow(this);
	}

	void setUpLayout() {
		final FormLayout layout = new FormLayout();

		// create the input controls
		final TextField applicationName = createRequiredTextField("Application name:",
				"Please enter a name for the application", ApplicationField.Name.getMaxLength());
		final TextField version = createRequiredTextField("Version:", "Please enter a valid version",
				ApplicationField.Version.getMaxLength());
		final TextField executablePath = createRequiredTextField("Executable path:",
				"Please enter a valid application path", ApplicationField.Path.getMaxLength());
		final TextArea description = createOptionalTextArea("Description:",
				ApplicationField.Description.getMaxLength());
		final TextField resource = createRequiredTextField("Resource:",
				"Please enter a valid resource (i.e., IP address, DNS name of resource)",
				ApplicationField.Resource.getMaxLength());
		final ComboBox resourceType = createResourceTypeComboBox();

		layout.addComponent(applicationName);
		layout.addComponent(version);
		layout.addComponent(executablePath);
		layout.addComponent(description);
		layout.addComponent(resource);
		layout.addComponent(resourceType);

		applicationFieldGroup.setBuffered(true);
		applicationFieldGroup.setItemDataSource(new BeanItem<Application>(application));
		applicationFieldGroup.bind(applicationName, ApplicationField.Name.getMemberName());
		applicationFieldGroup.bind(version, ApplicationField.Version.getMemberName());
		applicationFieldGroup.bind(executablePath, ApplicationField.Path.getMemberName());
		applicationFieldGroup.bind(description, ApplicationField.Description.getMemberName());
		applicationFieldGroup.bind(resource, ApplicationField.Resource.getMemberName());
		applicationFieldGroup.bind(resourceType, ApplicationField.ResourceType.getMemberName());

		final Button addApplicationButton = new Button("Add", new Button.ClickListener() {
			private static final long serialVersionUID = -2999251588313488770L;

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					applicationFieldGroup.commit();
					try {
						// notify listeners that there's a committed application
						listener.applicationCommitted(application);
					} finally {
						// always close the dialog
						removeFromParentWindow();
					}
				} catch (CommitException | SourceException | InvalidValueException e) {
					// ignore
				}
			}
		});
		addApplicationButton.setDescription("Add application");
		layout.addComponent(addApplicationButton);

		final Label error = new Label("", ContentMode.HTML);
		error.setVisible(false);
		layout.addComponent(error);

		layout.setWidth(410, Unit.PIXELS);
		layout.setMargin(true);

		setContent(layout);
	}

	private ComboBox createResourceTypeComboBox() {
		final ComboBox resourceTypeComboBox = new ComboBox("Resource type:");
		resourceTypeComboBox.setInputPrompt("Please select a resource type");
		resourceTypeComboBox.setWidth(COMMON_FIELD_WIDTH);
		resourceTypeComboBox.setNullSelectionAllowed(false);
		setRequired(resourceTypeComboBox, "Plese select a resource type");
		resourceTypeComboBox.addValidator(new NonBlankStringValidator("Please select a resource type"));
		// add all of the possible resource types
		for (final Middleware middleware : middlewareProvider.getAllMiddlewares()) {
			resourceTypeComboBox.addItem(middleware.getType());
		}
		return resourceTypeComboBox;
	}

	private TextField createRequiredTextField(final String caption, final String requiredError, final int fieldSize) {
		final TextField textField = new TextField(caption);
		textField.addValidator(new NonBlankMaxLengthStringValidator(fieldSize));
		setRequired(textField, requiredError);
		textField.setWidth(COMMON_FIELD_WIDTH);
		return textField;
	}

	private void setRequired(final Field<?> field, final String requiredError) {
		field.setRequired(true);
		field.setRequiredError(requiredError);
	}

	private TextArea createOptionalTextArea(final String caption, final int maxLength) {
		final TextArea textArea = new TextArea(caption);
		textArea.addValidator(new StringLengthValidator(
				"Please enter a description no longer than " + maxLength + " characters.", -1, maxLength, true));
		textArea.setRequired(false);
		textArea.setWidth(LARGE_FIELD_WIDTH);
		return textArea;
	}

	private static class NonBlankMaxLengthStringValidator extends AbstractValidator<String> {

		private static final long serialVersionUID = 8476005512295626122L;
		private final int maxLength;

		private NonBlankMaxLengthStringValidator(final int maxLength) {
			this("The field cannot be empty, blank, contain only whitespaces or contain more than " + maxLength
					+ " characters.", maxLength);
		}

		private NonBlankMaxLengthStringValidator(final String errorMessage, final int maxLength) {
			super(errorMessage);
			this.maxLength = maxLength;
		}

		@Override
		protected boolean isValidValue(final String value) {
			if (value == null) {
				return false;
			}
			return StringUtils.isNotBlank(value.toString()) && value.toString().length() <= maxLength;
		}

		@Override
		public Class<String> getType() {
			return String.class;
		}
	}

	private static class NonBlankStringValidator extends NonBlankMaxLengthStringValidator {

		private static final long serialVersionUID = 1584625763507132085L;

		private NonBlankStringValidator() {
			super("The field cannot be empty, blank or contain only whitespaces.", Integer.MAX_VALUE);
		}

		private NonBlankStringValidator(final String message) {
			super(message, Integer.MAX_VALUE);
		}
	}

}
