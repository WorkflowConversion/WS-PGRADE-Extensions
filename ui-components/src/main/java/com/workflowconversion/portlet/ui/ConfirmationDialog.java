package com.workflowconversion.portlet.ui;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Simple confirmation dialog.
 * 
 * @author delagarza
 *
 */
public class ConfirmationDialog extends Window {
	private static final long serialVersionUID = -4134747049491888621L;

	private final String caption;
	private final String message;
	private final ConfirmationDialogCloseListener listener;
	private volatile boolean response;

	/**
	 * Constructor.
	 * 
	 * @param caption
	 *            dialogue caption.
	 * @param message
	 *            message to display.
	 * @param listener
	 *            a listener that will receive a notification when this dialog is closed.
	 */
	public ConfirmationDialog(final String caption, final String message,
			final ConfirmationDialogCloseListener listener) {
		Validate.isTrue(StringUtils.isNotBlank(caption),
				"caption cannot be null, empty or contain only whitespace characters.");
		Validate.isTrue(StringUtils.isNotBlank(message),
				"message cannot be null, empty or contain only whitespace characters.");
		Validate.notNull(listener, "listener cannot be null.");
		this.caption = caption;
		this.message = message;
		this.listener = listener;
		setModal(true);
		setClosable(false);
		setUpLayout();
	}

	/**
	 * Displays this dialog.
	 */
	public void display() {
		UI.getCurrent().addWindow(this);
		bringToFront();
	}

	/**
	 * Constructs a confirmation dialog using "Confirm" as its caption.
	 * 
	 * @param message
	 *            message to display.
	 * @param listener
	 *            a listener that will receive a notification once this dialog is closed.
	 */
	public ConfirmationDialog(final String message, final ConfirmationDialogCloseListener listener) {
		this("Confirm", message, listener);
	}

	private void setUpLayout() {
		super.setCaption(this.caption);
		final Label messageLabel = new Label(this.message);
		final Button yesButton = new Button("Yes");
		yesButton.setImmediate(true);
		yesButton.setDisableOnClick(true);
		final Button noButton = new Button("No");
		noButton.setImmediate(true);
		noButton.setDisableOnClick(true);

		final HorizontalLayout buttonLayout = new HorizontalLayout();
		buttonLayout.setSpacing(true);
		buttonLayout.addComponent(noButton);
		buttonLayout.addComponent(yesButton);

		noButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 8474921427874675413L;

			@Override
			public void buttonClick(final ClickEvent event) {
				response = false;
				close();
			}
		});

		yesButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 4110837439955065365L;

			@Override
			public void buttonClick(final ClickEvent event) {
				response = true;
				close();
			}
		});

		final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSpacing(true);
		mainLayout.setMargin(true);
		mainLayout.addComponent(messageLabel);
		mainLayout.addComponent(buttonLayout);
		mainLayout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_RIGHT);
		setContent(mainLayout);
	}

	@Override
	public void close() {
		super.close();
		listener.confirmationDialogClose(response);
	}
}
