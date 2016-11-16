package com.workflowconversion.portlet.core.workflow.impl;

import com.workflowconversion.portlet.core.workflow.Workflow;
import com.workflowconversion.portlet.core.workflow.WorkflowExporter;

/**
 * Exports workflows as zip archives that can be downloaded to the user's computer.
 * 
 * @author delagarza
 *
 */
public class ArchiveDownloadWorkflowExporter implements WorkflowExporter {

	ArchiveDownloadWorkflowExporter() {
		// constructor added to make it "package" accessible only
	}

	@Override
	public void export(final Workflow workflow) throws Exception {
		// TODO Auto-generated method stub

	}

}
