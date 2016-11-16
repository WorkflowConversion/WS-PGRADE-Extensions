package com.workflowconversion.portlet.core.workflow.impl;

import java.io.File;

import org.apache.commons.lang.Validate;

import com.workflowconversion.portlet.core.settings.Settings;
import com.workflowconversion.portlet.core.user.PortletUser;
import com.workflowconversion.portlet.core.workflow.WorkflowProvider;
import com.workflowconversion.portlet.core.workflow.WorkflowProviderFactory;

/**
 * Factory that constructs instances of {@link WorkflowProvider}s.
 * 
 * @author delagarza
 *
 */
public class DefaultWorkflowProviderFactory implements WorkflowProviderFactory {

	private PortletUser portletUser;

	@Override
	public WorkflowProviderFactory withPortletUser(final PortletUser portletUser) {
		this.portletUser = portletUser;
		return this;
	}

	@Override
	public WorkflowProvider newWorkflowProvider() {
		Validate.notNull(portletUser,
				"portletUser cannot be null; please use the withPortletUser() method to set a valid instance.");
		final File stagingArea = new File(
				Settings.getInstance().getWorkflowStagingAreaPath() + File.pathSeparator + portletUser.getUserId());
		stagingArea.mkdirs();
		return new DefaultWorkflowProvider(stagingArea);
	}

}
