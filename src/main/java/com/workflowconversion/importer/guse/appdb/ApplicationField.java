package com.workflowconversion.importer.guse.appdb;

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
	Name(256),
	/**
	 * Description.
	 */
	Description(512),
	/**
	 * Path on which the application is found.
	 */
	Path(512),
	/**
	 * Resource (e.g., cluster.university.edu) on which the application resides.
	 */
	Resource(256),
	/**
	 * Resource type (e.g., unicore, moab, lsf).
	 */
	ResourceType(64),
	/**
	 * Version of the application.
	 */
	Version(16);

	private final int maxLength;

	ApplicationField(final int maxLength) {
		this.maxLength = maxLength;
	}

	/**
	 * Returns the maximum length of this field.
	 * 
	 * @return the maximum length of this field.
	 */
	public int getMaxLength() {
		return maxLength;
	}
}
