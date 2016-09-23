package com.workflowconversion.portlet.core.validation.impl;

import com.workflowconversion.portlet.core.validation.PortletSanityCheck;

import hu.sztaki.lpds.information.local.PropertyLoader;

/**
 * Determines if the portlet has been <i>gUSE initialized</i>.
 * 
 * @author delagarza
 *
 */
public class GUSEPortletSanityCheck implements PortletSanityCheck {

	@Override
	public boolean isPortletProperlyInitialized() {
		// invoke one of gUSE's web services that we know should always return a non-null value
		return PropertyLoader.getInstance().getProperty("is.url") != null;
	}

}
