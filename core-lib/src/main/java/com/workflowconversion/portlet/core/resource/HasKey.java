package com.workflowconversion.portlet.core.resource;

/**
 * Implementations provide a key that is to be used as a global ID, for instance as map keys.
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
