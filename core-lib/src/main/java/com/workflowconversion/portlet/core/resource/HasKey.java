package com.workflowconversion.portlet.core.resource;

/**
 * Implementations provide a key that is to be used as an ID for map insertion.
 * 
 * @author delagarza
 *
 */
public interface HasKey {

	/**
	 * @return the key.
	 */
	String generateKey();
}
