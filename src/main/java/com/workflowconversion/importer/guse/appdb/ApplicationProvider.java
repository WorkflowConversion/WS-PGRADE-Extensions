package com.workflowconversion.importer.guse.appdb;

import java.util.Collection;

import com.workflowconversion.importer.guse.exception.NotEditableApplicationProviderException;
import com.workflowconversion.importer.guse.user.PortletUser;

/**
 * Interface for application databases. So far, there are a couple of implementations, the custom gUSE application
 * database, which uses gUSE's database and UNICORE.
 * 
 * @author delagarza
 *
 */
public interface ApplicationProvider {

	/**
	 * Whether the provider allows changes.
	 * 
	 * @return {@code true} if changes are allowed, {@code false} otherwise.
	 */
	public boolean isEditable();

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
	 * @param user
	 *            The user requesting this operation.
	 * 
	 * @return All applications.
	 */
	public Collection<Application> getApplications(final PortletUser user);

	/**
	 * Adds application. If this database provider is not editable (i.e., {@link #isEditable()} returns {@code false}),
	 * implementations should throw an exception.
	 * 
	 * @param app
	 *            An application to add.
	 * @throws NotEditableApplicationProviderException
	 *             if this provider is not editable.
	 */
	public void addApplication(final Application app) throws NotEditableApplicationProviderException;

	/**
	 * Saves application. If this database provider is not editable (i.e., {@link #isEditable()} returns {@code false}),
	 * implementations should throw an exception.
	 * 
	 * @param app
	 *            An application to save.
	 * @throws NotEditableApplicationProviderException
	 *             if this provider is not editable.
	 */
	public void saveApplication(final Application app) throws NotEditableApplicationProviderException;

	/**
	 * Searches applications by name.
	 * 
	 * @param name
	 *            The name.
	 * @return A collection of applications whose name resembles the given {@code name}.
	 */
	public Collection<Application> searchApplicationsByName(final String name);

}
