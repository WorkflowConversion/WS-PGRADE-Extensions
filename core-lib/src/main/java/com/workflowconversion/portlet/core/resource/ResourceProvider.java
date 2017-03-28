package com.workflowconversion.portlet.core.resource;

import java.io.Serializable;
import java.util.Collection;

import com.workflowconversion.portlet.core.exception.DuplicateResourceException;
import com.workflowconversion.portlet.core.exception.ProviderNotEditableException;
import com.workflowconversion.portlet.core.exception.ResourceNotFoundException;

/**
 * Interface for application databases.
 * 
 * @author delagarza
 *
 */
public interface ResourceProvider extends Serializable {

	/**
	 * Whether the provider allows changes.
	 * 
	 * @return {@code true} if changes are allowed, {@code false} otherwise.
	 */
	public boolean isEditable();

	/**
	 * Returns the name of this application provider.
	 * 
	 * @return the name of this application provider.
	 */
	public String getName();

	/**
	 * Initializes this provider. It is the responsibility of the caller to query if this provider requires
	 * initialization (i.e., call the {@link #needsInit()} method) and to invoke this method if required.
	 */
	public void init();

	/**
	 * Returns all computing resources for the user.
	 * 
	 * @return All computing resources.
	 */
	public Collection<Resource> getResources();

	/**
	 * Adds a computing resource. If this provider is not editable (i.e., {@link #isEditable()} returns {@code false}),
	 * implementations should throw an exception.
	 * 
	 * @param resource
	 *            An application to add. After adding the application, the id will be modified to reflect the id given
	 *            by the storage.
	 * @throws ProviderNotEditableException
	 *             if this provider is not editable.
	 * @throws DuplicateResourceException
	 *             if the resource already exists.
	 */
	public void addResource(final Resource resource) throws ProviderNotEditableException, DuplicateResourceException;

	/**
	 * Saves a computing resource. If this provider is not editable (i.e., {@link #isEditable()} returns {@code false}),
	 * implementations should throw an exception.
	 * 
	 * @param resource
	 *            A computing resource to save.
	 * @throws ProviderNotEditableException
	 *             if this provider is not editable.
	 * @throws ResourceNotFoundException
	 *             if the resource doesn't exist already.
	 */
	public void saveResource(final Resource resource) throws ProviderNotEditableException, ResourceNotFoundException;

	/**
	 * Removes a computing resource. If this provider is not editable, implementations should throw an exception.
	 * 
	 * @param resource
	 *            the computing resource to remove.
	 * @throws ResourceNotFoundException
	 *             if the resource doesn't exist already.
	 */
	public void removeResource(final Resource resource) throws ProviderNotEditableException, ResourceNotFoundException;

	/**
	 * Removes all resources.
	 * 
	 * @throws ProviderNotEditableException
	 *             if this provider is not editable.
	 */
	public void removeAllResources() throws ProviderNotEditableException;

	/**
	 * Returns whether this provider contains the given resource.
	 * 
	 * @param resource
	 *            the resource.
	 * @return {@code true} if this provider contains the given resource.
	 */
	public boolean containsResource(final Resource resource);

	/**
	 * Signals implementations that changes need to be permanently saved.
	 * 
	 * @throws ProviderNotEditableException
	 *             if this provider is not editable.
	 */
	public void commitChanges() throws ProviderNotEditableException;
}
