package com.workflowconversion.portlet.core.execution.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import com.workflowconversion.portlet.core.exception.JobExecutionPropertiesHandlerNotFoundException;
import com.workflowconversion.portlet.core.execution.JobExecutionPropertiesHandler;
import com.workflowconversion.portlet.core.workflow.Job;

/**
 * Encompassing handler that uses a list of specific handlers. As more handlers are coded, they should be added to this
 * one.
 * 
 * @author delagarza
 *
 */
public class DefaultJobExecutionPropertiesHandler implements JobExecutionPropertiesHandler {

	private final Collection<JobExecutionPropertiesHandler> knownHandlers;

	/**
	 * Default constructor.
	 */
	public DefaultJobExecutionPropertiesHandler() {
		this.knownHandlers = new LinkedList<JobExecutionPropertiesHandler>();
		fillKnownHandlers();
	}

	private void fillKnownHandlers() {
		knownHandlers.add(new ClusterJobExecutionPropertiesHandler());
		knownHandlers.add(new LocalJobExecutionPropertiesHandler());
		knownHandlers.add(new UnicoreJobExecutionPropertiesHandler());
	}

	@Override
	public boolean canHandle(final Job job) {
		return findHandler(job) != null;
	}

	@Override
	public void handle(final Job job, final Map<String, String> jobExecutionProperties) {
		final JobExecutionPropertiesHandler handler = findHandler(job);
		// sanity check
		if (handler == null) {
			throw new JobExecutionPropertiesHandlerNotFoundException(job);
		}
		handler.handle(job, jobExecutionProperties);
	}

	private JobExecutionPropertiesHandler findHandler(final Job job) {
		for (final JobExecutionPropertiesHandler handler : knownHandlers) {
			if (handler.canHandle(job)) {
				return handler;
			}
		}
		return null;
	}

}
