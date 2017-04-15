package com.workflowconversion.portlet.core.workflow;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.workflowconversion.portlet.core.workflow.impl.ArchivePathXmlAdapter;

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
	@XmlJavaTypeAdapter(ArchivePathXmlAdapter.class)
	private Path archivePath;

	// we won't serialize the jobs to an xml file, or the status of the workflow
	@XmlTransient
	private final Collection<Job> jobs;

	/**
	 * Constructor.
	 */
	public Workflow() {
		jobs = new LinkedList<Job>();
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
		return Collections.unmodifiableCollection(jobs);
	}

	/**
	 * @param id
	 *            the id.
	 */
	public void setId(final String id) {
		Validate.isTrue(StringUtils.isNotBlank(id),
				"id cannot be null, empty or contain only whitespaces; this is a coding problem and should be reported.");
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
		Validate.isTrue(StringUtils.isNotBlank(name),
				"name cannot be null, empty or contain only whitespaces; this is a coding problem and should be reported.");
		this.name = name;
	}

	/**
	 * @return the location
	 */
	public Path getArchivePath() {
		return archivePath;
	}

	/**
	 * @param archivePath
	 *            the path.
	 */
	public void setArchivePath(final Path archivePath) {
		Validate.notNull(archivePath, "archivePath cannot be null; this is a coding problem and should be reported.");
		this.archivePath = archivePath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Workflow [id=" + id + ", name=" + name + ", archivePath=" + archivePath + ", jobs=" + jobs + "]";
	}

}
