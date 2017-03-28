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

	private final String id;
	private String name;
	private Application application;
	private Queue queue;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            the id of this job.
	 */
	public Job(final String id) {
		Validate.isTrue(StringUtils.isNotBlank(id), "id cannot be null, contain only whitespaces or be empty");
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(final String name) {
		this.name = name;
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
