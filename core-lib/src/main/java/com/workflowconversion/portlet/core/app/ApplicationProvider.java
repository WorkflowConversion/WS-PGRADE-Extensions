package com.workflowconversion.portlet.core.app;

import java.io.Serializable;
import java.util.Collection;

import com.workflowconversion.portlet.core.exception.NotEditableApplicationProviderException;

/**
 * Interface for application databases. So far, there are a couple of implementations, the custom gUSE application
 * database, which uses gUSE's database and UNICORE.
 * 
 * @author delagarza
 *
 */
public interface ApplicationProvider extends Serializable {

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
	 * Returns all applications for the user.
	 * 
	 * @return All applications.
	 */
	public Collection<Application> getApplications();

	/**
	 * Adds application. If this provider is not editable (i.e., {@link #isEditable()} returns {@code false}),
	 * implementations should throw an exception.
	 * 
	 * @param app
	 *            An application to add. After adding the application, the id will be modified to reflect the id given
	 *            by the storage.
	 * @throws NotEditableApplicationProviderException
	 *             if this provider is not editable.
	 * @return the id of the added application.
	 */
	public String addApplication(final Application app) throws NotEditableApplicationProviderException;

	/**
	 * Saves application. If this provider is not editable (i.e., {@link #isEditable()} returns {@code false}),
	 * implementations should throw an exception.
	 * 
	 * @param app
	 *            An application to save.
	 * @throws NotEditableApplicationProviderException
	 *             if this provider is not editable.
	 */
	public void saveApplication(final Application app) throws NotEditableApplicationProviderException;

	/**
	 * Removes an application. If this provider is not editable, implementations should throw an exception.
	 * 
	 * @param app
	 *            the application to remove.
	 * @throws NotEditableApplicationProviderException
	 *             if this provider is not editable.
	 */
	public void removeApplication(final Application app) throws NotEditableApplicationProviderException;
}
