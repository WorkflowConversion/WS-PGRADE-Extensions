package com.workflowconversion.portlet.core.resource;

import java.io.Serializable;
import java.util.Collection;

import com.workflowconversion.portlet.core.middleware.MiddlewareProvider;

/**
 * Interface for resource providers.
 * 
 * Configured resources are defined in an xml file {@code dci-bridge.xml} and are read-only accessible via
 * {@code dci_bridge_service}.
 * 
 * Adding resources through implementations of this interface is not allowed. However, each resource can have associated
 * applications and, depending on the type of resource, it might be possible to store applications through a resource
 * provider. Queues associated to resources are also read-only.
 * 
 * A common usage of a resource provider is to populate tables that display information about computing resources in a
 * user-friendly way. Implementations should be thread safe and as stateless as possible. This is due to the fact that a
 * certain resource provider might be visible by more than one thread simultaneously. If an implementation stores state
 * in a certain form, problems with the consistency of the data could arise.
 * 
 * @author delagarza
 * 
 * @see MiddlewareProvider
 *
 */
public interface ResourceProvider extends Serializable {

	/**
	 * @return whether the resource provider allows applications to be added.
	 */
	public boolean canAddApplications();

	/**
	 * @return the name of this resource provider.
	 */
	public String getName();

	/**
	 * Initializes this provider. Convenience method to provide a lazy initialization.
	 */
	public void init();

	/**
	 * It is possible that some implementations will have problems during initialization due to, for instance, expired
	 * certificates, unavailable resources, etc. This method will tell users of {@link ResourceProvider} that this
	 * instance could not be properly initialized.
	 * 
	 * @return {@code true} if this provider had errors during initialization.
	 */
	public boolean hasInitErrors();

	/**
	 * Returns all computing resources for the user directly from the persistence layer. Implementations should not
	 * store references to the returned resources in order to avoid inconsistencies.
	 * 
	 * @return All computing resources.
	 */
	public Collection<Resource> getResources();

	/**
	 * @param name
	 *            the resource name.
	 * @param type
	 *            the resource type.
	 * @return the matching resource, or {@code null} if this provider doesn't contain such a resource.
	 */
	public Resource getResource(final String name, final String type);

	/**
	 * Saves the applications that have been associated to the passed resource. Any application present in persistence
	 * storage (e.g., a database) but missing in the passed resource should be removed. In other words, this method
	 * should synchronize the applications in the passed resource with the ones in the persistence layer.
	 * 
	 * Implementations should think of this method as the "save" button in a text editor. Using this analogy, the
	 * persistence layer would be a file on a disk; the applications contained in the passed resource would be the
	 * "dirty" contents of a text editor. When the "save" button in a text editor is pressed, the contents of the text
	 * file will be replaced by the contents in the text editor. If there are two text editors issuing the "save"
	 * command, the last one will overwrite any previous modifications on the text file. This is, of course,
	 * resource-wide.
	 * 
	 * @param resource
	 *            the {@link Resource} that contains the applications to be saved.
	 */
	public void save(final Resource resource);
}
