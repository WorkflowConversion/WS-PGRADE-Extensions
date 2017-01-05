package com.workflowconversion.portlet.core.workflow;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.workflowconversion.portlet.core.resource.Application;

/**
 * Simple class that represents jobs in imported workflows.
 * 
 * @author delagarza
 *
 */
public class Job implements Comparable<Job> {

	private final String id;
	private String name;
	private Application application;
	private ConfigurationState state;

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
	public void setName(String name) {
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
	public void setApplication(Application application) {
		this.application = application;
	}

	/**
	 * @return the state
	 */
	public ConfigurationState getState() {
		return state;
	}

	/**
	 * @param state
	 *            the state to set
	 */
	public void setConfigurationState(ConfigurationState state) {
		this.state = state;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Job other = (Job) obj;
		if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(final Job o) {
		return this.id.compareTo(o.id);
	}

	/**
	 * Enum to represent the possible configuration state of a job.
	 * 
	 * @author delagarza
	 */
	public enum ConfigurationState {
		/**
		 * Job is fully configured to be executed on a WS-PGRADE workflow.
		 */
		Complete,
		/**
		 * Job is partially configured.
		 */
		Incomplete
	}
}
