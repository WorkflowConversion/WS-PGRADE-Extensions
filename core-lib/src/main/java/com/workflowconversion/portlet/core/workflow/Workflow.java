package com.workflowconversion.portlet.core.workflow;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

/**
 * Simple class that represents workflows uploaded to the portlet.
 * 
 * @author delagarza
 *
 */
public class Workflow {
	// TODO: add status based on jobs: if the jobs are fully configured, then the workflow is ready, otherwise, flag it
	// as not ready

	private final String id;
	private String name;
	private File location;
	private final Set<Job> jobs;

	public Workflow(final String id) {
		Validate.isTrue(StringUtils.isNotBlank(id), "id cannot be null, contain only whitespaces or be empty");
		this.id = id;
		jobs = new TreeSet<Job>();
	}

	/**
	 * Adds a job.
	 * 
	 * @param job
	 *            the job to add.
	 */
	public void addJob(final Job job) {
		this.jobs.add(job);
	}

	/**
	 * @return a collection containing all jobs.
	 */
	public Collection<Job> getJobs() {
		return new ArrayList<Job>(jobs);
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
	 * @return the location
	 */
	public File getLocation() {
		return location;
	}

	/**
	 * @param location
	 *            the location to set
	 */
	public void setLocation(File location) {
		this.location = location;
	}

}
