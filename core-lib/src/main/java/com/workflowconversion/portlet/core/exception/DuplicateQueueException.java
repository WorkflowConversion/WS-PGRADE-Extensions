package com.workflowconversion.portlet.core.exception;

import com.workflowconversion.portlet.core.resource.Queue;

/**
 * Thrown whenever a resource already contains a queue.
 * 
 * @author delagarza
 *
 */
public class DuplicateQueueException extends ApplicationException {
	private static final long serialVersionUID = 8199546424710839623L;

	/**
	 * @param duplicateQueue
	 *            the duplicate queue.
	 */
	public DuplicateQueueException(final Queue duplicateQueue) {
		super("The resource already contains a queue with the same name. Duplicate queue: " + duplicateQueue);
	}
}
