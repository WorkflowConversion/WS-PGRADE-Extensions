package com.workflowconversion.portlet.core.workflow;

/**
 * Simple interface to define the methods needed to export workflows.
 * 
 * @author delagarza
 *
 */
public interface WorkflowExporter {

	/**
	 * Exports a workflow.
	 * 
	 * @param workflow
	 *            the workflow to export.
	 * @throws Exception
	 *             if something goes wrong.
	 */
	public void export(final Workflow workflow) throws Exception;
}
