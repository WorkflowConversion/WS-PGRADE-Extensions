package com.workflowconversion.portlet.core.execution.impl;

import java.util.Map;

import com.workflowconversion.portlet.core.execution.JobExecutionPropertiesHandler;
import com.workflowconversion.portlet.core.workflow.Job;

/**
 * Handles Unicore jobs.
 * 
 * @author delagarza
 *
 */
public class UnicoreJobExecutionPropertiesHandler implements JobExecutionPropertiesHandler {

	private static final String GRID_TYPE_UNICORE = "unicore";

	@Override
	public boolean canHandle(final Job job) {
		return GRID_TYPE_UNICORE.equalsIgnoreCase(job.getApplication().getResource().getType());
	}

	@Override
	public void handle(final Job job, final Map<String, String> jobExecutionProperties) {
		jobExecutionProperties.put(JOB_EXECUTION_PROPERTY_GRID_TYPE, GRID_TYPE_UNICORE);
		jobExecutionProperties.put(JOB_EXECUTION_PROPERTY_JOB_MANAGER, job.getApplication().getName());
		jobExecutionProperties.put(JOB_EXECUTION_PROPERTY_JOB_TYPE, "binary");
		jobExecutionProperties.put(JOB_EXECUTION_PROPERTY_GRID, job.getApplication().getResource().getName());
	}

}
