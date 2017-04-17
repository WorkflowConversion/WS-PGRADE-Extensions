package com.workflowconversion.portlet.core.workflow.impl;

import com.workflowconversion.portlet.core.user.PortletUser;
import com.workflowconversion.portlet.core.workflow.Workflow;
import com.workflowconversion.portlet.core.workflow.WorkflowExporter;

/**
 * Exports workflows to the local WS-PGRADE repository.
 * 
 * @author delagarza
 *
 */
// TODO: Find out a way to actually export workflows to a local WS-PGRADE repository
public class LocalRepositoryWorkflowExporter implements WorkflowExporter {

	private final PortletUser portletUser;

	LocalRepositoryWorkflowExporter(final PortletUser portletUser) {
		this.portletUser = portletUser;
	}

	@Override
	public void export(final Workflow workflow) throws Exception {
		// TODO it seems that gUSE needs a refactoring to actually be able to export
		// a workflow from an ASM portlet
		throw new UnsupportedOperationException(
				"Exporting workflows to the local WS-PGRADE repository has not been implemented yet. Sorry.");
	}

}
