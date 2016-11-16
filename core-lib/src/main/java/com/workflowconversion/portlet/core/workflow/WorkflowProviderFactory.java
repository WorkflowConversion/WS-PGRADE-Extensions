package com.workflowconversion.portlet.core.workflow;

import com.workflowconversion.portlet.core.user.PortletUser;

/**
 * Factory for workflow providers.
 * 
 * @author delagarza
 *
 */
public interface WorkflowProviderFactory {

	/**
	 * Sets the current portlet user and returns {@code this} factory.
	 * 
	 * @param portletUser
	 *            the current portlet user.
	 * @return {@code this} factory.
	 */
	public WorkflowProviderFactory withPortletUser(final PortletUser portletUser);

	/**
	 * Builds a new instance a workflow provider with the current settings.
	 * 
	 * @return a new workflow provider
	 */
	public WorkflowProvider newWorkflowProvider();
}
