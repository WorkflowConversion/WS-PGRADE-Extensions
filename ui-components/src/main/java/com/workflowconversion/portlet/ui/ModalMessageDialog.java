package com.workflowconversion.portlet.ui;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * Simple modal message dialog.
 * 
 * @author delagarza
 *
 */
public class ModalMessageDialog extends Window {

	private static final long serialVersionUID = -1847983350864789711L;

	private final Type type;
	private final String title;
	private final String message;

	/**
	 * @param title
	 *            the title to display on the bar.
	 * @param message
	 *            the message.
	 * @param type
	 *            the message type.
	 */
	public ModalMessageDialog(final String title, final String message, final Type type) {
		this.title = title;
		this.message = message;
		this.type = type;
		setModal(true);
		setResizable(false);
	}

	/**
	 * Displays this dialog.
	 */
	public void display() {
		initUI();
		UI.getCurrent().addWindow(this);
		bringToFront();
	}

	private void initUI() {
		setCaption(title);
		final VerticalLayout content = new VerticalLayout();

		final SimpleContent.Builder builder = new SimpleContent.Builder();
		switch (type) {
		case ERROR_MESSAGE:
			builder.withIconLocation("../runo/icons/64/cancel.png");
			builder.withStyle(UIConstants.ERROR_THEME);
			content.addStyleName(UIConstants.ERROR_THEME);
			break;
		case WARNING_MESSAGE:
			builder.withIconLocation("../runo/icons/64/attention.png");
			builder.withStyle(UIConstants.WARNING_THEME);
			content.addStyleName(UIConstants.WARNING_THEME);
			break;
		default:
			builder.withIconLocation("../runo/icons/64/note.png");

		}
		builder.withMessage(message);

		final Button closeButton = new Button("Close");
		closeButton.setImmediate(true);
		closeButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = -4591820014121111057L;

			@Override
			public void buttonClick(final ClickEvent event) {
				ModalMessageDialog.this.close();
			}
		});

		final HorizontalLayout footerLayout = new HorizontalLayout();
		footerLayout.setWidth(100, Unit.PERCENTAGE);
		footerLayout.addComponent(closeButton);
		footerLayout.setComponentAlignment(closeButton, Alignment.BOTTOM_RIGHT);

		content.addComponent(builder.newContent());
		content.addComponent(footerLayout);
		content.setWidth(800, Unit.PIXELS);
		content.setMargin(true);

		setContent(content);
	}

}
