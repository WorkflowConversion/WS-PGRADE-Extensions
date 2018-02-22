package com.workflowconversion.portlet.core.workflow;

import com.workflowconversion.portlet.core.user.PortletUser;

/**
 * Factory interface for other workflow exporter factories.
 * 
 * @author delagarza
 *
 */
public interface WorkflowExporterFactory {

	/**
	 * Sets the current portlet user and returns {@code this} factory.
	 * 
	 * @param portletUser
	 *            the current portlet user.
	 * @return {@code this} factory.
	 */
	public WorkflowExporterFactory withPortletUser(final PortletUser portletUser);

	/**
	 * Sets the desired export destination.
	 * 
	 * @param destination
	 *            the desired export destination.
	 * @return {@code this} factory.
	 */
	public WorkflowExporterFactory withDestination(final WorkflowExportDestination destination);

	/**
	 * Builds a new instance a workflow provider with the current settings.
	 * 
	 * @return a new workflow provider
	 */
	public WorkflowExporter newInstance();

}
