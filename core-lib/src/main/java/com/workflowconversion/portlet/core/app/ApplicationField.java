package com.workflowconversion.portlet.core.app;

/**
 * Application fields.
 * 
 * @author delagarza
 *
 */
public enum ApplicationField {
	/**
	 * Id.
	 */
	Id(256, "id", "ID"),
	/**
	 * Name of the application.
	 */
	Name(256, "name", "Name"),
	/**
	 * Description.
	 */
	Description(512, "description", "Description"),
	/**
	 * Path on which the application is found.
	 */
	Path(512, "path", "Path"),
	/**
	 * Resource (e.g., cluster.university.edu) on which the application resides.
	 */
	Resource(256, "resource", "Resource"),
	/**
	 * Resource type (e.g., unicore, moab, lsf).
	 */
	ResourceType(64, "resourceType", "Resource Type"),
	/**
	 * Version of the application.
	 */
	Version(16, "version", "Version"),
	/**
	 * Some resources such as moab support the use of queues.
	 */
	Queue(128, "queue", "Queue");

	private final int maxLength;
	private final String memberName;
	private final String displayName;

	ApplicationField(final int maxLength, final String memberName, final String displayName) {
		this.maxLength = maxLength;
		this.memberName = memberName;
		this.displayName = displayName;
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

	/**
	 * A <i>nice</i> name that can be presented to the end user.
	 * 
	 * @return the display name.
	 */
	public String getDisplayName() {
		return displayName;
	}
}
