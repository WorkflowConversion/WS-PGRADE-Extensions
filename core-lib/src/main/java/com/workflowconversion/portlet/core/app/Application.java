package com.workflowconversion.portlet.core.app;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

/**
 * Simple object that contains all of the information an application requires to be executed on gUSE.
 * 
 * @author delagarza
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Application implements Serializable {

	private static final long serialVersionUID = -8200132807492156967L;

	@XmlAttribute
	private String name;
	@XmlAttribute
	private String version;
	@XmlAttribute
	private String description;
	@XmlAttribute
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Application [name=" + name + ", version=" + version + ", description=" + description + ", path=" + path
				+ "]";
	}

	/**
	 * Generates a key using the relevant fields.
	 * 
	 * @return
	 */
	public String generateKey() {
		return "name=" + this.name + "_version=" + this.version + "_path=" + this.path;
	}

}
