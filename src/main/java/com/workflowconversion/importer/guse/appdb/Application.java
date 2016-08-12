package com.workflowconversion.importer.guse.appdb;

import java.io.Serializable;

/**
 * Simple object that contains all of the information an application requires to be executed on gUSE.
 * 
 * @author delagarza
 */
public class Application implements Serializable, Comparable<Application> {

	private static final long serialVersionUID = -8200132807492156967L;

	private String name;
	private String version;
	private String description;
	private String path;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version
	 *            the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return generateKey().hashCode();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Application other = (Application) obj;
		return this.generateKey().equals(other.generateKey());

	}

	@Override
	public int compareTo(final Application other) {
		return generateKey().compareTo(other.generateKey());
	}

	// name and version are the attributes that define an application, path and description are optional attributes
	private String generateKey() {
		final StringBuilder key = new StringBuilder("name=");
		key.append(name).append(",version=");
		key.append(version);
		return key.toString();
	}

}
