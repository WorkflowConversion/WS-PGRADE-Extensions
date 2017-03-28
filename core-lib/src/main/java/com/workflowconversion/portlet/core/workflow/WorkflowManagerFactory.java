package com.workflowconversion.portlet.core.workflow;

import java.util.Collection;

import com.workflowconversion.portlet.core.resource.ResourceProvider;
import com.workflowconversion.portlet.core.user.PortletUser;

/**
 * Factory for workflow managers.
 * 
 * @author delagarza
 *
 */
public interface WorkflowManagerFactory {

	/**
	 * Sets the current portlet user and returns {@code this} factory.
	 * 
	 * @param portletUser
	 *            the current portlet user.
	 * @return {@code this} factory.
	 */
	public WorkflowManagerFactory withPortletUser(final PortletUser portletUser);

	/**
	 * Sets the collection of available resource providers.
	 * 
	 * @param resourceProviders
	 *            the resource providers.
	 * @return {@code this factory}.
	 */
	public WorkflowManagerFactory withResourceProviders(final Collection<ResourceProvider> resourceProviders);

	/**
	 * Builds a new instance a workflow provider with the current settings.
	 * 
	 * @return a new workflow provider
	 */
	public WorkflowManager newInstance();
}
