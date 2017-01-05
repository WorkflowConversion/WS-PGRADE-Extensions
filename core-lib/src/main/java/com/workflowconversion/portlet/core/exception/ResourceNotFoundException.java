package com.workflowconversion.portlet.core.exception;

import com.workflowconversion.portlet.core.resource.Resource;

/**
 * Exception thrown when a resource that doesn't exist is to be edited.
 */
public class ResourceNotFoundException extends ApplicationException {
	private static final long serialVersionUID = 7515682643836180882L;

	/**
	 * @param resource
	 *            the non-existent resource.
	 */
	public ResourceNotFoundException(final Resource resource) {
		super("This resource does not exist. Resource: " + resource);
	}
}
