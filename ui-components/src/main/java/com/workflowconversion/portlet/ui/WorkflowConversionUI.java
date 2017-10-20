package com.workflowconversion.portlet.ui;

import java.util.Collection;

import javax.portlet.PortletRequest;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.util.PortalUtil;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinPortletService;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Layout;
import com.vaadin.ui.UI;
import com.workflowconversion.portlet.core.exception.ApplicationException;
import com.workflowconversion.portlet.core.resource.ResourceProvider;
import com.workflowconversion.portlet.core.user.PortletUser;
import com.workflowconversion.portlet.core.validation.PortletSanityCheck;

/**
 * The entry point of every portlet in this project.
 * 
 * @author delagarza
 *
 */
@Theme("mytheme")
public abstract class WorkflowConversionUI extends UI {

	private static final long serialVersionUID = -1691439006632825854L;

	private final static Logger LOG = LoggerFactory.getLogger(WorkflowConversionUI.class);

	protected PortletUser currentUser;
	// a non-empty collection of resource providers
	protected final Collection<ResourceProvider> resourceProviders;
	protected final PortletSanityCheck portletSanityCheck;

	/**
	 * Constructor.
	 * 
	 * @param portletSanityCheck
	 *            the portlet sanity check.
	 * @param resourceProviders
	 *            the resource providers.
	 */
	protected WorkflowConversionUI(final PortletSanityCheck portletSanityCheck,
			final Collection<ResourceProvider> resourceProviders) {
		Validate.notEmpty(resourceProviders, "resourceProviders cannot be null or empty");
		Validate.notNull(portletSanityCheck, "portletSanityCheck cannot be null");
		this.resourceProviders = resourceProviders;
		this.portletSanityCheck = portletSanityCheck;
	}

	@Override
	public final void init(final VaadinRequest vaadinRequest) {
		LOG.info("initializing WorkflowConversionApplication");

		this.currentUser = extractCurrentUser(VaadinPortletService.getCurrentPortletRequest());
		final Layout content;

		if (currentUser.isAuthenticated()) {
			if (portletSanityCheck.isPortletProperlyInitialized()) {
				// happy path!
				initApplicationProviders();
				content = prepareContent();
			} else {
				content = new SimpleContent.Builder().withIconLocation("../runo/icons/64/lock.png")
						.withStyle(UIConstants.ERROR_THEME)
						.withMessage(
								"This portlet has not been properly initialized.<br/>If the problem persists after restarting gUSE, please check the logs and report the problem.<br/>This might be caused by a bug or a configuration error.")
						.newContent();
			}
		} else {
			content = new SimpleContent.Builder().withIconLocation("../runo/icons/64/attention.png")
					.withStyle(UIConstants.WARNING_THEME)
					.withMessage("You need to be logged-in to access this portlet.").newContent();
		}
		setContent(content);
	}

	/**
	 * Prepares the main window to set.
	 * 
	 * @return the main window of this application.
	 */
	protected abstract Layout prepareContent();

	private void initApplicationProviders() {
		// init any provider that needs initialization
		LOG.info("initializing resource providers");
		for (final ResourceProvider provider : resourceProviders) {
			if (LOG.isInfoEnabled()) {
				LOG.info("initializing " + provider.getClass());
			}
			provider.init();
		}
	}

	private PortletUser extractCurrentUser(final PortletRequest request) {
		try {
			return new PortletUser(PortalUtil.getUser(request));
		} catch (SystemException | PortalException e) {
			// there isn't much we can do, really
			throw new ApplicationException("Could not extract current user from the PortletRequest.", e);
		}
	}
}
