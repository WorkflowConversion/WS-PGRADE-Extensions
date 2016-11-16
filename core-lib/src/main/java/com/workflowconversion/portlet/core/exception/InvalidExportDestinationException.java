package com.workflowconversion.portlet.core.exception;

import com.workflowconversion.portlet.core.workflow.WorkflowExportDestination;

/**
 * Thrown whenever a non-recognized or non-configured {@link WorkflowExportDestination} is encountered.
 * 
 * @author delagarza
 *
 */
public class InvalidExportDestinationException extends ApplicationException {
	private static final long serialVersionUID = -881480754381402L;

	/**
	 * Constructor.
	 * 
	 * @param message
	 * @param destination
	 */
	public InvalidExportDestinationException(final String message, final WorkflowExportDestination destination) {
		super(message + " [" + destination.name() + ']');
	}

	/**
	 * Constructor.
	 * 
	 * @param destination
	 */
	public InvalidExportDestinationException(final WorkflowExportDestination destination) {
		this("Invalid workflow destination", destination);
	}
}
