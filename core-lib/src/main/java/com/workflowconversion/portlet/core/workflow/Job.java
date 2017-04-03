package com.workflowconversion.portlet.core.workflow;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.core.resource.Queue;

/**
 * Simple class that represents jobs in imported workflows.
 * 
 * @author delagarza
 *
 */
public class Job {

	private final String name;
	private Application application;
	private Queue queue;

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            the name of this job.
	 */
	public Job(final String name) {
		Validate.isTrue(StringUtils.isNotBlank(name), "name cannot be null, contain only whitespaces or be empty");
		this.name = name;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the application
	 */
	public Application getApplication() {
		return application;
	}

	/**
	 * @param application
	 *            the application to set
	 */
	public void setApplication(final Application application) {
		this.application = application;
	}

	/**
	 * @return the queue
	 */
	public Queue getQueue() {
		return queue;
	}

	/**
	 * @param queue
	 *            the queue to set
	 */
	public void setQueue(final Queue queue) {
		this.queue = queue;
	}
}
