package com.workflowconversion.portlet.core.middleware;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;

import dci.data.Item;
import dci.data.Middleware;

/**
 * Defines the methods to retrieve gUSE middleware/items. gUSE stores information about computing resources on an xml
 * file which is accessible (read-only) through the {@dci_bridge_service} component. Since more than one class will be
 * accessing the contents of this xml file, it makes sense to define an interface to decouple components.
 * 
 * The xml file contains information about the resources and their associated queues and there is, of course, no
 * information regarding applications, so applications need to be accessed in some other way, and this depends on the
 * type of resource (see {@link com.workflowconversion.portlet.core.resource.ResourceProvider}).
 * 
 * @author delagarza
 *
 */
public interface MiddlewareProvider extends Serializable {

	/**
	 * Returns a collection with all the enabled middlewares.
	 * 
	 * @return All of the middlewares whose {@code enabled} attribute is set to {@code true}.
	 */
	public Collection<Middleware> getEnabledMiddlewares();

	/**
	 * Returns a collection with all the enabled middlewares of the given type.
	 * 
	 * @param middlewareType
	 *            The middleware type.
	 * @return The middlewares whose {@code enabled} attribute is set to {@code true} and their {@code type} matches the
	 *         passed {@code middlewareType}.
	 */
	public Collection<Middleware> getEnabledMiddlewares(final String middlewareType);

	/**
	 * Returns a collection with all the available items of the given middleware type.
	 * 
	 * @param middlewareType
	 *            The middleware type.
	 * @return The items whose {@code enabled} attribute is set to {@code true} and their middleware type matches the
	 *         passed {@code middlewareType}.
	 */
	public Collection<Item> getEnabledItems(final String middlewareType);

	/**
	 * Obtains a collection with all the middlewares.
	 * 
	 * @return All of the middlewares present in {@code dci-bridge.xml}.
	 */
	public Collection<Middleware> getAllMiddlewares();

	/**
	 * Obtains a set containing all the possible middleware types.
	 * 
	 * @return All of the middleware types.
	 */
	public Set<String> getAllMiddlewareTypes();
}
