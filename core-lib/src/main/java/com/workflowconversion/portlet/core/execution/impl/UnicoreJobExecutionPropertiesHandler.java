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
class UnicoreJobExecutionPropertiesHandler implements JobExecutionPropertiesHandler {

	private static final String GRID_TYPE_UNICORE = "unicore";

	@Override
	public boolean canHandle(final Job job) {
		return GRID_TYPE_UNICORE.equalsIgnoreCase(job.getResourceType());
	}

	@Override
	public void handle(final Job job, final Map<String, String> jobExecutionProperties) {
		jobExecutionProperties.put(JOB_EXECUTION_PROPERTY_GRID_TYPE, GRID_TYPE_UNICORE);
		// as defined in dci_bridge_service/src/main/java/hu/sztaki/lpds/submitter/grids/Grid_unicore.java,
		// the 'jobmanager' property of a unicore job is the concatenation of an app's name and version
		jobExecutionProperties.put(JOB_EXECUTION_PROPERTY_JOB_MANAGER,
				job.getApplication().getName() + ' ' + job.getApplication().getVersion());
		jobExecutionProperties.put(JOB_EXECUTION_COMMAND_LINE, job.getParameters());
		jobExecutionProperties.put(JOB_EXECUTION_PROPERTY_JOB_TYPE, "binary");
		jobExecutionProperties.put(JOB_EXECUTION_PROPERTY_GRID, job.getResourceName());
	}

}
