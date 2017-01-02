package com.workflowconversion.portlet.core.app;

import java.io.Serializable;
import java.util.Collection;

import com.workflowconversion.portlet.core.exception.ProviderNotEditableException;

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
	 * Whether the provider needs to be initialized.
	 * 
	 * @return {@code true} if this provider needs to be initialized, {@code false} otherwise.
	 */
	public boolean needsInit();

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
	 */
	public void addResource(final Resource resource) throws ProviderNotEditableException;

	/**
	 * Saves a computing resource. If this provider is not editable (i.e., {@link #isEditable()} returns {@code false}),
	 * implementations should throw an exception.
	 * 
	 * @param resource
	 *            A computing resource to save.
	 * @throws ProviderNotEditableException
	 *             if this provider is not editable.
	 */
	public void saveResource(final Resource resource) throws ProviderNotEditableException;

	/**
	 * Removes a computing resource. If this provider is not editable, implementations should throw an exception.
	 * 
	 * @param resource
	 *            the computing resource to remove.
	 * @throws ProviderNotEditableException
	 *             if this provider is not editable.
	 */
	public void removeResource(final Resource resource) throws ProviderNotEditableException;

	/**
	 * Signals implementations that changes need to be permanently saved.
	 * 
	 * @throws ProviderNotEditableException
	 *             if this provider is not editable.
	 */
	public void commitChanges() throws ProviderNotEditableException;
}
