package com.workflowconversion.portlet.core.servlet;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workflowconversion.portlet.core.Settings;
import com.workflowconversion.portlet.core.resource.ResourceProvider;

import hu.sztaki.lpds.information.local.InitAxisServices;

/**
 * Invoked directly from gUSE's initialization. Here we perform initialization tasks that must happen after references
 * to gUSE services have been initialized.
 * 
 * @author delagarza
 *
 */
public class WorkflowConversionInitServlet extends InitAxisServices {

	private static final long serialVersionUID = -8169502673458273931L;

	private static final Logger LOG = LoggerFactory.getLogger(WorkflowConversionInitServlet.class);

	@Override
	protected void processRequest(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {
		super.processRequest(request, response);
		LOG.info("gUSE services were bootstrapped; initializing our resource providers");
		initResourceProviders(Settings.getInstance().getResourceProviders());
	}

	private void initResourceProviders(final Collection<ResourceProvider> resourceProviders) {
		// init any provider that needs initialization
		for (final ResourceProvider provider : resourceProviders) {
			if (LOG.isInfoEnabled()) {
				LOG.info("initializing " + provider.getClass());
			}
			provider.init();
		}
	}

}
