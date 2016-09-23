package com.workflowconversion.portlet.core.validation.impl;

import com.workflowconversion.portlet.core.validation.PortletSanityCheck;

/**
 * As its name implies, this class will always tell client code that portlets have been properly initialized.
 * 
 * @author delagarza
 *
 */
public class MockPortletSanityCheck implements PortletSanityCheck {

	@Override
	public boolean isPortletProperlyInitialized() {
		return true;
	}

}
