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
	Name(256, "name"),
	/**
	 * Description.
	 */
	Description(512, "description"),
	/**
	 * Path on which the application is found.
	 */
	Path(512, "path"),
	/**
	 * Resource (e.g., cluster.university.edu) on which the application resides.
	 */
	Resource(256, "resource"),
	/**
	 * Resource type (e.g., unicore, moab, lsf).
	 */
	ResourceType(64, "resourceType"),
	/**
	 * Version of the application.
	 */
	Version(16, "version");

	private final int maxLength;
	private final String memberName;

	ApplicationField(final int maxLength, final String memberName) {
		this.maxLength = maxLength;
		this.memberName = memberName;
	}

	/**
	 * Returns the maximum length of this field.
	 * 
	 * @return the maximum length of this field.
	 */
	public int getMaxLength() {
		return maxLength;
	}

	/**
	 * Returns the internal name of this field. This is the name of the member in the {@link Application} class.
	 * 
	 * @return the member name of this field.
	 */
	public String getMemberName() {
		return memberName;
	}
}
