package com.workflowconversion.portlet.core.workflow;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

/**
 * Simple class that represents workflows uploaded to the portlet.
 * 
 * @author delagarza
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Workflow {

	@XmlAttribute
	private String id;
	@XmlAttribute
	private String name;
	@XmlAttribute
	private File location;

	// we won't serialize the jobs to an xml file
	@XmlTransient
	private final Set<Job> jobs;

	/**
	 * Constructor.
	 */
	public Workflow() {
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

	public void setId(final String id) {
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
	 * @return the location
	 */
	public File getLocation() {
		return location;
	}

	/**
	 * @param location
	 *            the location to set
	 */
	public void setLocation(final File location) {
		this.location = location;
	}

}
