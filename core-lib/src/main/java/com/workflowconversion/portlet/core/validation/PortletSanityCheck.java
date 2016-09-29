package com.workflowconversion.portlet.core.validation;

import java.io.Serializable;

/**
 * Interface containing methods that serve as a sanity check.
 * 
 * @author delagarza
 *
 */
public interface PortletSanityCheck extends Serializable {

	/**
	 * Whether the portlet invoking this method has been properly initialized (gUSE requires certain specific
	 * initialization).
	 * 
	 * @return {@code true} if this portlet has been properly initialized.
	 */
	public boolean isPortletProperlyInitialized();
}
