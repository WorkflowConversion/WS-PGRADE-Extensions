package com.workflowconversion.portlet.core.workflow.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.workflowconversion.portlet.core.exception.ApplicationException;
import com.workflowconversion.portlet.core.execution.impl.DefaultJobExecutionPropertiesHandler;
import com.workflowconversion.portlet.core.resource.ResourceProvider;
import com.workflowconversion.portlet.core.search.AssetFinder;
import com.workflowconversion.portlet.core.settings.Settings;
import com.workflowconversion.portlet.core.user.PortletUser;
import com.workflowconversion.portlet.core.workflow.WorkflowManager;
import com.workflowconversion.portlet.core.workflow.WorkflowManagerFactory;

/**
 * Factory that constructs instances of {@link WorkflowManager}s.
 * 
 * @author delagarza
 *
 */
public class DefaultWorkflowManagerFactory implements WorkflowManagerFactory {

	private PortletUser portletUser;
	private Collection<ResourceProvider> resourceProviders;

	@Override
	public WorkflowManagerFactory withPortletUser(final PortletUser portletUser) {
		this.portletUser = portletUser;
		return this;
	}

	@Override
	public WorkflowManagerFactory withResourceProviders(final Collection<ResourceProvider> resourceProviders) {
		this.resourceProviders = resourceProviders;
		return this;
	}

	@Override
	public WorkflowManager newInstance() {
		Validate.notNull(portletUser,
				"portletUser cannot be null; please use the withPortletUser() method to set a valid instance.");
		Validate.notEmpty(resourceProviders,
				"resourceProviders cannot be null or empty; please use the withResourceProviders() method to set the applications.");
		final String stagingAreaPath = Settings.getInstance().getWorkflowStagingAreaPath();
		Validate.isTrue(StringUtils.isNotBlank(stagingAreaPath),
				"invalid staging area, please configure the 'workflow.stagingArea.path' property in the web.xml file.");
		try {
			final File stagingArea = new File(stagingAreaPath + File.pathSeparator + portletUser.getUserId())
					.getCanonicalFile();
			if (!stagingArea.exists()) {
				FileUtils.forceMkdir(stagingArea);
			}
			final AssetFinder assetFinder = new AssetFinder();
			assetFinder.init(resourceProviders);
			return new DefaultWorkflowManager(stagingArea, assetFinder, new DefaultJobExecutionPropertiesHandler());
		} catch (final IOException e) {
			throw new ApplicationException(
					"There was a problem in creating the staging area for the user with id " + portletUser.getUserId(),
					e);
		}
	}

}
