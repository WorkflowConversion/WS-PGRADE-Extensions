package com.workflowconversion.portlet.ui.workflow.upload;

import java.io.File;

/**
 * Interface to notify objects interested in the event of a workflow being uploaded to the staging area.
 * 
 * @author delagarza
 *
 */
public interface WorkflowUploadListener {

	/**
	 * Invoked when a workflow was uploaded.
	 * 
	 * @param location
	 *            the location on which the workflow was uploaded.
	 */
	public void workflowUploaded(final File location);

	/**
	 * Invoked when the upload failed.
	 * 
	 * @reason the failure reason.
	 */
	public void uploadFailed(final Exception reason);
}
