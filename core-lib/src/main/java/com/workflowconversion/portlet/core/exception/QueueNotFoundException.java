package com.workflowconversion.portlet.core.exception;

import com.workflowconversion.portlet.core.resource.Queue;

/**
 * Thrown when a queue that doesnt' exist in a resource is attempted to be removed.
 * 
 * @author delagarza
 *
 */
public class QueueNotFoundException extends ApplicationException {
	private static final long serialVersionUID = -4549232744696821109L;

	public QueueNotFoundException(final Queue queue) {
		super("The queue does not exist in this resource. Queue: " + queue);
	}
}
