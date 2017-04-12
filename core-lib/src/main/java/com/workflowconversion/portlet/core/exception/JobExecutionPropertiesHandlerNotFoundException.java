package com.workflowconversion.portlet.core.exception;

import com.workflowconversion.portlet.core.workflow.Job;

/**
 * Exception to be thrown when a job cannot be handled.
 * 
 * @author delagarza
 *
 */
public class JobExecutionPropertiesHandlerNotFoundException extends ApplicationException {

	private static final long serialVersionUID = 4008959161751527314L;

	/**
	 * @param job
	 *            the job that cannot be handled.
	 */
	public JobExecutionPropertiesHandlerNotFoundException(final Job job) {
		super("Jobs " + (job.getResourceType() == null ? "without type" : "of type '" + job.getResourceType() + '\'')
				+ " cannot be handled.");
	}

	/**
	 * @param message
	 *            a message.
	 */
	public JobExecutionPropertiesHandlerNotFoundException(final String message) {
		super(message);
	}
}
