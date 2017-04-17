package com.workflowconversion.portlet.core.resource;

import java.io.Serializable;
import java.util.Collection;

/**
 * Interface for resource providers.
 * 
 * Resources come from the already installed grids/clusters on WS-PGRADE. Adding resources through implementations of
 * this interface is not allowed. However, each resource can have associated applications, so resources can be saved.
 * 
 * @author delagarza
 *
 */
public interface ResourceProvider extends Serializable {

	/**
	 * @return whether the resource provider allows applications to be added.
	 */
	public boolean canAddApplications();

	/**
	 * Returns the name of this application provider.
	 * 
	 * @return the name of this application provider.
	 */
	public String getName();

	/**
	 * Initializes this provider. Convenience method to provide a lazy initialization.
	 */
	public void init();

	/**
	 * Returns all computing resources for the user.
	 * 
	 * @return All computing resources.
	 */
	public Collection<Resource> getResources();

	/**
	 * Signals implementations that changes done to the resources should be saved.
	 */
	public void saveApplications();
}
