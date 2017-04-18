package com.workflowconversion.portlet.ui;

import com.vaadin.server.Page;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;

/**
 * Simple class used to display HTML messages.
 * 
 * @author delagarza
 *
 */
public class NotificationUtils {

	private final static String NOTIFICATION_TITLE = "Workflow Conversion";

	/**
	 * Displays a warning dialog.
	 * 
	 * @param text
	 *            the message to display.
	 */
	public static void displayWarning(final String text) {
		displayHtmlNotification(NOTIFICATION_TITLE, text, Type.WARNING_MESSAGE);
	}

	/**
	 * Displays an error dialog.
	 * 
	 * @param text
	 *            the message to display.
	 */
	public static void displayError(final String text) {
		displayHtmlNotification(NOTIFICATION_TITLE, text, Type.ERROR_MESSAGE);
	}

	/**
	 * Displays an error dialog.
	 * 
	 * @param text
	 *            the message to display.
	 * @param e
	 *            the thrown exception.
	 */
	public static void displayError(final String text, final Exception e) {
		displayHtmlNotification(NOTIFICATION_TITLE, text + " -- Reason: " + e.getMessage(), Type.ERROR_MESSAGE);
	}

	/**
	 * Displays a message.
	 * 
	 * @param text
	 *            the message to display.
	 */
	public static void displayMessage(final String text) {
		displayHtmlNotification(NOTIFICATION_TITLE, text, Type.HUMANIZED_MESSAGE);
	}

	/**
	 * Displays a message on the "tray".
	 * 
	 * @param text
	 *            the message to display.
	 */
	public static void displayTrayMessage(final String text) {
		displayHtmlNotification(NOTIFICATION_TITLE, text, Type.TRAY_NOTIFICATION);
	}

	private static void displayHtmlNotification(final String caption, final String text, final Type type) {
		final Notification notification = new Notification(caption, text, type, true);
		notification.show(Page.getCurrent());
	}
}
