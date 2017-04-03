package com.workflowconversion.portlet.core.exception;

/**
 * Exception thrown when a job cannot be found in a workflow.
 * 
 * @author delagarza
 *
 */
public class JobNotFoundException extends ApplicationException {
	private static final long serialVersionUID = -1985676294540736207L;

	/**
	 * @param jobName
	 *            the name of the job that could not be found.
	 */
	public JobNotFoundException(final String jobName) {
		super("Job with name " + jobName + " was not found in this workflow.");
	}
}
