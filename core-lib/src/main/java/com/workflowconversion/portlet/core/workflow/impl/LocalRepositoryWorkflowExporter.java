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
public class LocalRepositoryWorkflowExporter implements WorkflowExporter {

	private final PortletUser portletUser;

	LocalRepositoryWorkflowExporter(final PortletUser portletUser) {
		this.portletUser = portletUser;
	}

	@Override
	public void export(final Workflow workflow) throws Exception {
		// TODO Auto-generated method stub

	}

}
