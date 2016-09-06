package com.workflowconversion.importer.guse.appdb.filter;

/**
 * Application fields.
 * 
 * @author delagarza
 *
 */
public enum ApplicationField {
	/**
	 * Name of the application.
	 */
	Name,
	/**
	 * Description.
	 */
	Description,
	/**
	 * Path on which the application is found.
	 */
	Path,
	/**
	 * Resource (e.g., cluster.university.edu) on which the application resides.
	 */
	Resource,
	/**
	 * Resource type (e.g., unicore, moab, lsf).
	 */
	ResourceType,
	/**
	 * Version of the application.
	 */
	Version;
}
