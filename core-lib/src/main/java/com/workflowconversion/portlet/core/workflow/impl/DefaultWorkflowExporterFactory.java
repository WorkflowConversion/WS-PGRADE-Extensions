package com.workflowconversion.portlet.core.workflow.impl;

import org.apache.commons.lang3.Validate;

import com.workflowconversion.portlet.core.exception.InvalidExportDestinationException;
import com.workflowconversion.portlet.core.user.PortletUser;
import com.workflowconversion.portlet.core.workflow.WorkflowExportDestination;
import com.workflowconversion.portlet.core.workflow.WorkflowExporter;
import com.workflowconversion.portlet.core.workflow.WorkflowExporterFactory;

public class DefaultWorkflowExporterFactory implements WorkflowExporterFactory {

	private PortletUser portletUser;
	private WorkflowExportDestination destination;

	@Override
	public WorkflowExporterFactory withPortletUser(final PortletUser portletUser) {
		this.portletUser = portletUser;
		return this;
	}

	@Override
	public WorkflowExporterFactory withDestination(final WorkflowExportDestination destination) {
		this.destination = destination;
		return this;
	}

	@Override
	public WorkflowExporter newInstance() {
		Validate.notNull(portletUser,
				"portletUser cannot be null; please use the withPortletUser() method to set a valid portlet user.");
		Validate.notNull(destination,
				"destination cannot be null; please use the withDestination() method to set a valid destination.");

		final WorkflowExporter workflowExporter;

		switch (destination) {
		case Archive:
			workflowExporter = new ArchiveDownloadWorkflowExporter();
			break;
		case LocalRepository:
			workflowExporter = new LocalRepositoryWorkflowExporter(portletUser);
			break;
		default:
			throw new InvalidExportDestinationException(destination);
		}

		return workflowExporter;
	}

}