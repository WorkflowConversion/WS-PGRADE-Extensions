package com.workflowconversion.importer.guse.vaadin.ui;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.AbstractValidator;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DefaultFieldFactory;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Window;
import com.workflowconversion.importer.guse.appdb.Application;
import com.workflowconversion.importer.guse.appdb.ApplicationField;
import com.workflowconversion.importer.guse.exception.InvalidApplicationFieldException;
import com.workflowconversion.importer.guse.middleware.MiddlewareProvider;

import dci.data.Middleware;

/**
 * Modal dialog displayed when the user wants
 * 
 * @author delagarza
 *
 */
class AddApplicationWindow extends Window {

	private static final long serialVersionUID = 2514225841272484511L;

	private final Window parent;
	private final Form applicationForm;
	private final Application application;
	private final MiddlewareProvider middlewareProvider;

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            the parent window.
	 */
	AddApplicationWindow(final Window parent, final MiddlewareProvider middlewareProvider) {
		Validate.notNull(parent, "parent cannot be null");
		Validate.notNull(middlewareProvider, "middlewareProvider cannot be null");
		this.parent = parent;
		this.middlewareProvider = middlewareProvider;
		super.setVisible(false);
		super.setCaption("Add application");
		super.setModal(true);

		this.application = new Application();
		this.applicationForm = new Form();

		setUpForm();
	}

	void setUpForm() {
		applicationForm.setCaption("New application");
		applicationForm.setWriteThrough(false);
		applicationForm.setInvalidCommitted(false);
		applicationForm.setFormFieldFactory(new ApplicationFieldFactory());
		applicationForm.setItemDataSource(new BeanItem<Application>(application));
		applicationForm.setVisibleItemProperties(
				new ApplicationField[] { ApplicationField.Name, ApplicationField.Version, ApplicationField.Path,
						ApplicationField.Description, ApplicationField.ResourceType, ApplicationField.Resource });
		final Button addApplicationButton = new Button("Add", new Button.ClickListener() {
			private static final long serialVersionUID = -2999251588313488770L;

			@Override
			public void buttonClick(ClickEvent event) {
				applicationForm.commit();
			}
		});
		applicationForm.getFooter().addComponent(addApplicationButton);
		applicationForm.getFooter().setMargin(true, false, true, true);

		addComponent(applicationForm);
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			parent.addWindow(this);
		}
	}

	private class ApplicationFieldFactory extends DefaultFieldFactory {

		private static final long serialVersionUID = 1952414851060513902L;
		private static final String COMMON_FIELD_WIDTH = "12em";

		@Override
		public Field createField(final Item item, final Object propertyId, final Component uiContext) {
			final ApplicationField applicationField = (ApplicationField) propertyId;
			switch (applicationField) {
			case Name:
				return createRequiredTextField("Application name:", "Please enter a name for the application",
						applicationField.getMaxLength());
			case Version:
				return createRequiredTextField("Version:", "Please enter a valid version",
						applicationField.getMaxLength());
			case Path:
				return createRequiredTextField("Executable path:", "Please enter a valid application path",
						applicationField.getMaxLength());
			case Description:
				return createOptionalTextArea("Description:", applicationField.getMaxLength());
			case Resource:
				return createRequiredTextField("Resource:",
						"Please enter a valid resource (i.e., IP address, DNS name of resource)",
						applicationField.getMaxLength());
			case ResourceType:
				return createResourceTypeComboBox();
			default:
				throw new InvalidApplicationFieldException(applicationField);

			}
		}

		private Field createResourceTypeComboBox() {
			final ComboBox resourceTypeComboBox = new ComboBox("Resource type:");
			resourceTypeComboBox.setInputPrompt("Please select a resource type");
			resourceTypeComboBox.setWidth(COMMON_FIELD_WIDTH);
			setRequired(resourceTypeComboBox, "Plese select a resource type");
			// add all of the possible resource types
			for (final Middleware middleware : middlewareProvider.getAllMiddlewares()) {
				resourceTypeComboBox.addItem(middleware.getType());
			}
			return resourceTypeComboBox;
		}

		private Field createRequiredTextField(final String caption, final String requiredError, final int fieldSize) {
			final Field textField = new TextField(caption);
			textField.addValidator(new NonBlankMaxLengthStringValidator(fieldSize));
			setRequired(textField, requiredError);
			textField.setWidth(COMMON_FIELD_WIDTH);
			return textField;
		}

		private void setRequired(final Field field, final String requiredError) {
			field.setRequired(true);
			field.setRequiredError(requiredError);
		}

		private Field createOptionalTextArea(final String caption, final int maxLength) {
			final Field textArea = new TextArea(caption);
			textArea.addValidator(new StringLengthValidator(
					"Please enter a description no longer than " + maxLength + " characters.", -1, maxLength, true));
			textArea.setRequired(false);
			textArea.setWidth("20em");
			return textArea;
		}
	}

	private static class NonBlankMaxLengthStringValidator extends AbstractValidator {

		private static final long serialVersionUID = 8476005512295626122L;
		private final int maxLength;

		public NonBlankMaxLengthStringValidator(final int maxLength) {
			super("The field cannot be empty, blank, contain only whitespaces or contain more than " + maxLength
					+ " characters.");
			this.maxLength = maxLength;
		}

		@Override
		public boolean isValid(final Object value) {
			if (value == null) {
				return false;
			}
			return StringUtils.isNotBlank(value.toString()) && value.toString().length() <= maxLength;
		}
	}

}
