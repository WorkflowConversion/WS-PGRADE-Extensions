package com.workflowconversion.portlet.ui;

/**
 * Callback interface.
 * 
 * @author delagarza
 *
 */
public interface ConfirmationDialogCloseListener {
	/**
	 * Method to be invoked when a confirmation dialog is closed.
	 * 
	 * @param response
	 *            {@code true} if the "Yes" button was pressed, {@code false} otherwise.
	 */
	void confirmationDialogClose(final boolean response);
}