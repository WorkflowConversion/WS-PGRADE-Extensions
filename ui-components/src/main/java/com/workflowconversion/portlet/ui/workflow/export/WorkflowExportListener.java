package com.workflowconversion.portlet.ui.workflow.export;

/**
 * Implementations of this interface will be notified of events occurring during a workflow export.
 * 
 * @author delagarza
 *
 */
public interface WorkflowExportListener {

	/**
	 * Invoked when an export failed.
	 * 
	 * @param reason
	 *            the failure reason.
	 */
	public void exportFailed(final Exception reason);
}
