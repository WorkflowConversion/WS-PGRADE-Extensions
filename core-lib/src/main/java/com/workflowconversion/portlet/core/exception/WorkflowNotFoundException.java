package com.workflowconversion.portlet.core.exception;

import com.workflowconversion.portlet.core.workflow.Workflow;

/**
 * Exception to be thrown when a workflow is not found.
 * 
 * @author delagarza
 *
 */
public class WorkflowNotFoundException extends ApplicationException {
	private static final long serialVersionUID = 3198425995223842877L;

	public WorkflowNotFoundException(final Workflow workflow) {
		super("Workflow named " + workflow.getName() + " was not found.");
	}

}
