package com.workflowconversion.portlet.ui.table;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.jsoup.helper.Validate;

import com.vaadin.data.Buffered.SourceException;
import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.validator.StringLengthValidator;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.workflowconversion.portlet.ui.validation.NonBlankMaxLengthStringValidator;
import com.workflowconversion.portlet.ui.validation.NonBlankStringValidator;

/**
 * Modal dialog to be displayed when a new element is to be added directly by the user.
 * 
 * @author delagarza
 *
 */
public abstract class AbstractAddGenericElementDialog<T> extends Window {
	private static final long serialVersionUID = 318425957596472520L;

	protected static final String LARGE_FIELD_WIDTH = "15em";
	protected static final String COMMON_FIELD_WIDTH = "10em";

	protected final FieldGroup fieldGroup;
	protected final GenericElementCommittedListener<T> listener;
	protected Wrapper<T> elementWrapper;

	protected AbstractAddGenericElementDialog(final String caption, final GenericElementCommittedListener<T> listener) {
		Validate.isTrue(StringUtils.isNotBlank(caption),
				"caption cannot be null, empty or contain only whitespace characters.");
		Validate.notNull(listener, "listener cannot be null.");

		this.fieldGroup = new FieldGroup();
		this.listener = listener;

		setCaption(caption);
		setModal(true);
		setResizable(false);
	}

	/**
	 * Sets the default element and the layout.
	 */
	public void init() {
		this.elementWrapper = createDefaultElementWrapper();
		setUpLayout();
	}

	/**
	 * Creates a new element to be displayed when the dialog opens.
	 * 
	 * @return the "default" element to display when the dialog opens.
	 */
	protected abstract Wrapper<T> createDefaultElementWrapper();

	protected void setUpLayout() {
		final FormLayout formLayout = new FormLayout();

		fieldGroup.setBuffered(true);
		fieldGroup.setItemDataSource(new BeanItem<Wrapper<T>>(elementWrapper));

		// add and bind components
		addAndBindComponents(formLayout, fieldGroup);

		final Button addGenericElementButton = new Button("Add", new Button.ClickListener() {
			private static final long serialVersionUID = -2999251588313488770L;

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					fieldGroup.commit();
					try {
						// notify listeners that there's a committed application
						listener.elementCommitted(elementWrapper.get());
					} finally {
						// always close the dialog
						removeFromParentWindow();
					}
				} catch (CommitException | SourceException | InvalidValueException e) {
					// ignore
				}
			}
		});
		addGenericElementButton.setDescription("Add new item");
		final HorizontalLayout footerLayout = new HorizontalLayout();
		footerLayout.setWidth(100, Unit.PERCENTAGE);
		footerLayout.addComponent(addGenericElementButton);
		footerLayout.setComponentAlignment(addGenericElementButton, Alignment.BOTTOM_RIGHT);

		formLayout.addComponent(footerLayout);

		final Label error = new Label("", ContentMode.HTML);
		error.setVisible(false);
		formLayout.addComponent(error);

		formLayout.setWidth(410, Unit.PIXELS);
		formLayout.setMargin(true);

		setContent(formLayout);
	}

	/**
	 * This is where implementations add components for each of the fields to be filled out and bind them to the field
	 * group.
	 * 
	 * @param formLayout
	 *            the form in which components are to be added.
	 * @param fieldGroup
	 *            the field group to which components are bound to.
	 */
	protected abstract void addAndBindComponents(final FormLayout formLayout, final FieldGroup fieldGroup);

	protected final void removeFromParentWindow() {
		UI.getCurrent().removeWindow(this);
	}

	protected final TextField createRequiredTextField(final String caption, final String requiredError,
			final int fieldSize) {
		final TextField textField = new TextField(caption);
		textField.addValidator(new NonBlankMaxLengthStringValidator(fieldSize));
		setRequired(textField, requiredError);
		textField.setWidth(COMMON_FIELD_WIDTH);
		return textField;
	}

	protected final void setRequired(final Field<?> field, final String requiredError) {
		field.setRequired(true);
		field.setRequiredError(requiredError);
	}

	protected final TextArea createOptionalTextArea(final String caption, final int maxLength) {
		final TextArea textArea = new TextArea(caption);
		textArea.addValidator(new StringLengthValidator(
				"Please enter a value no longer than " + maxLength + " characters.", -1, maxLength, true));
		textArea.setRequired(false);
		textArea.setWidth(LARGE_FIELD_WIDTH);
		return textArea;
	}

	protected final ComboBox createComboBox(final String caption, final String requiredError,
			final Collection<String> options) {
		final ComboBox comboBox = new ComboBox(caption);
		comboBox.setInputPrompt(requiredError);
		comboBox.setWidth(COMMON_FIELD_WIDTH);
		comboBox.setNullSelectionAllowed(false);
		setRequired(comboBox, requiredError);
		comboBox.addValidator(new NonBlankStringValidator(requiredError));
		// add all of the possible resource types
		for (final String option : options) {
			comboBox.addItem(option);
		}
		return comboBox;
	}

}
