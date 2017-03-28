package com.workflowconversion.portlet.core.workflow.impl;

import com.workflowconversion.portlet.core.user.PortletUser;
import com.workflowconversion.portlet.core.workflow.Workflow;
import com.workflowconversion.portlet.core.workflow.WorkflowExportDestination;
import com.workflowconversion.portlet.core.workflow.WorkflowExporter;
import com.workflowconversion.portlet.core.workflow.WorkflowExporterFactory;

/**
 * Mock factory. Useful for testing purposes.
 * 
 * @author delagarza
 *
 */
public class MockWorkflowExporterFactory implements WorkflowExporterFactory {

	@Override
	public WorkflowExporterFactory withPortletUser(PortletUser portletUser) {
		return this;
	}

	@Override
	public WorkflowExporterFactory withDestination(WorkflowExportDestination destination) {
		return this;
	}

	@Override
	public WorkflowExporter newInstance() {
		return new WorkflowExporter() {

			@Override
			public void export(final Workflow workflow) throws Exception {
				// nop
			}
		};
	}

}
