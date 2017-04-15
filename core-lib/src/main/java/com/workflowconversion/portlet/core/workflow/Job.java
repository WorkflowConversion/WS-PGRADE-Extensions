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
	private String parameters;
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
	 * @return the parameters
	 */
	public String getParameters() {
		return parameters;
	}

	/**
	 * @param parameters
	 *            the parameters to set
	 */
	public void setParameters(final String parameters) {
		this.parameters = parameters;
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
		Validate.notNull(application, "application cannot be null, this is a coding problem and should be reported.");
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
		Validate.notNull(queue, "queue cannot be null, this is a coding problem and should be reported.");
		this.queue = queue;
	}

	/**
	 * Shortcut method to obtain the resource type.
	 * 
	 * @return the resource type associated with this job's application or {@code null} if there's no application or
	 *         resource associated to this job.
	 */
	public String getResourceType() {
		if (application != null && application.getResource() != null) {
			return application.getResource().getType();
		}
		return null;
	}

	/**
	 * Shortcut method to obtain the resource name.
	 * 
	 * @return the resource name associated with this job's application or {@code null} if there's no application or
	 *         resource associated to this job.
	 */
	public String getResourceName() {
		if (application != null && application.getResource() != null) {
			return application.getResource().getName();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Job [name=" + name + ", parameters=" + parameters + ", application=" + application + ", queue=" + queue
				+ "]";
	}

}
