package com.workflowconversion.portlet.core.exception;

import com.workflowconversion.portlet.core.resource.Resource;

/**
 * Exception thrown when a resource to be saved is a duplicate.
 * 
 * @author delagarza
 *
 */
public class DuplicateResourceException extends ApplicationException {

	private static final long serialVersionUID = 836761887772390533L;

	/**
	 * @param duplicateResource
	 *            the duplicate resource.
	 */
	public DuplicateResourceException(final Resource duplicateResource) {
		super("Another resource with the same name and type already exists. Duplicate resource: " + duplicateResource);
	}

}
