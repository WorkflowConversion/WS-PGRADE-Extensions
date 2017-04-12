package com.workflowconversion.portlet.core.execution.impl;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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

	private final static Set<String> SUPPORTED_CLUSTERS = new TreeSet<String>(
			Arrays.asList("pbs", "lsf", "sge", "moab"));

	@Override
	public boolean canHandle(final Job job) {
		return SUPPORTED_CLUSTERS.contains(job.getResourceType());
	}

	@Override
	public void handle(final Job job, final Map<String, String> jobExecutionProperties) {
		jobExecutionProperties.put(JOB_EXECUTION_PROPERTY_GRID_TYPE, job.getResourceType());
		jobExecutionProperties.put(JOB_EXECUTION_COMMAND_LINE,
				job.getApplication().getPath() + ' ' + job.getParameters());
		jobExecutionProperties.put(JOB_EXECUTION_PROPERTY_JOB_MANAGER, "-");
		jobExecutionProperties.put(JOB_EXECUTION_PROPERTY_JOB_TYPE, "binary");
		jobExecutionProperties.put(JOB_EXECUTION_PROPERTY_RESOURCE, job.getQueue().getName());
		jobExecutionProperties.put(JOB_EXECUTION_PROPERTY_GRID, job.getResourceName());
	}

}
