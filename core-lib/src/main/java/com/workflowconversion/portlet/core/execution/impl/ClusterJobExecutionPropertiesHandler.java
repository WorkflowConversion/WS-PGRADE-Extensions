package com.workflowconversion.portlet.core.execution.impl;

import java.util.Map;

import com.workflowconversion.portlet.core.SupportedClusters;
import com.workflowconversion.portlet.core.execution.JobExecutionPropertiesHandler;
import com.workflowconversion.portlet.core.workflow.Job;

/**
 * Handles jobs that will be executed on the WS-PGRADE supported clusters:
 * <ul>
 * <li>{@code moab}.
 * <li>{@code pbs}.
 * <li>{@code lsf}.
 * <li>{@code sge}.
 * </ul>
 * 
 * @author delagarza
 *
 */
class ClusterJobExecutionPropertiesHandler implements JobExecutionPropertiesHandler {

	@Override
	public boolean canHandle(final Job job) {
		return SupportedClusters.isSupported(job.getResourceType());
	}

	@Override
	public void handle(final Job job, final Map<String, String> jobExecutionProperties) {
		jobExecutionProperties.put(JOB_EXECUTION_PROPERTY_GRID_TYPE, job.getResourceType());
		jobExecutionProperties.put(JOB_EXECUTION_COMMAND_LINE,
				job.getApplication().getPath() + ' ' + job.getParameters());
		jobExecutionProperties.put(JOB_EXECUTION_PROPERTY_JOB_MANAGER, "-");
		jobExecutionProperties.put(JOB_EXECUTION_PROPERTY_JOB_TYPE, "binary");
		if (job.getQueue() != null) {
			jobExecutionProperties.put(JOB_EXECUTION_PROPERTY_RESOURCE, job.getQueue().getName());
		} else {
			jobExecutionProperties.remove(JOB_EXECUTION_PROPERTY_RESOURCE);
		}
		jobExecutionProperties.put(JOB_EXECUTION_PROPERTY_GRID, job.getResourceName());
	}

}
