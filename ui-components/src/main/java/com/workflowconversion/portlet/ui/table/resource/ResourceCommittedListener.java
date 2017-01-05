package com.workflowconversion.portlet.ui.table.resource;

import com.workflowconversion.portlet.core.resource.Resource;

/**
 * Listener invoked whenever a resource has been committed by the user, such as in a "new resource" dialog.
 * 
 * @author delagarza
 *
 */
public interface ResourceCommittedListener {

	/**
	 * Invoked when a valid resource is to be added.
	 * 
	 * @param resource
	 *            the resource to add.
	 */
	void resourceCommitted(final Resource resource);
}
