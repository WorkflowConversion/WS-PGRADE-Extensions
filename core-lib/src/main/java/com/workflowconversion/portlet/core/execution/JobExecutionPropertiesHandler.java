package com.workflowconversion.portlet.core.execution;

import java.util.Map;

import com.workflowconversion.portlet.core.workflow.Job;

/**
 * Each of the supported resources in WS-PGRADE requires a different set of execution properties that is reflected on
 * the {@code <execute>} nodes in {@code workflow.xml}. Each of the supported resource types implements this interface.
 * 
 * @author delagarza
 *
 */
public interface JobExecutionPropertiesHandler {

	/**
	 * Grid type property name.
	 */
	public final static String JOB_EXECUTION_PROPERTY_GRID_TYPE = "gridtype";
	/**
	 * Job manager property name.
	 */
	public final static String JOB_EXECUTION_PROPERTY_JOB_MANAGER = "jobmanager";
	/**
	 * Job type property name.
	 */
	public final static String JOB_EXECUTION_PROPERTY_JOB_TYPE = "jobistype";
	/**
	 * Grid property name.
	 */
	public final static String JOB_EXECUTION_PROPERTY_GRID = "grid";
	/**
	 * Resource property name.
	 */
	public final static String JOB_EXECUTION_PROPERTY_RESOURCE = "resource";

	/**
	 * @param job
	 *            the job whose execution properties need to be handled.
	 * @return whether the implementation can handle the passed job.
	 */
	boolean canHandle(final Job job);

	/**
	 * Handles the execution properties of the passed job.
	 * 
	 * @param job
	 *            the job.
	 * @param jobExecutionProperties
	 *            the execution properties. The keys of the map are the names of the properties and the values are the
	 *            value of the properties.
	 */
	void handle(final Job job, final Map<String, String> jobExecutionProperties);
}
