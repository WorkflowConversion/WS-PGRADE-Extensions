package com.workflowconversion.portlet.core.workflow;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.core.resource.Queue;
import com.workflowconversion.portlet.core.resource.Resource;

/**
 * Simple class that represents jobs in imported workflows.
 * 
 * @author delagarza
 *
 */
public class Job {

	private final String name;
	private String parameters;
	private Resource resource;
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
	 * @return the resource
	 */
	public Resource getResource() {
		return resource;
	}

	/**
	 * @param resource
	 *            the resource to set
	 */
	public void setResource(final Resource resource) {
		this.resource = resource;
	}

	/**
	 * Shortcut method to access the resource type of this job.
	 * 
	 * @return the resource type of this job, or {@code null} if no resource has been associated with this job.
	 */
	public String getResourceType() {
		return (resource == null ? null : resource.getType());
	}

	/**
	 * Shortcut method to access the resource name of this job.
	 * 
	 * @return the resource name of this job, or {@code null} i fno resource has been associated with this job.
	 */
	public String getResourceName() {
		return (resource == null ? null : resource.getName());
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
