package com.workflowconversion.portlet.ui.table.application;

import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.ui.table.Wrapper;

public class ApplicationWrapper implements Wrapper<Application> {

	private String name;
	private String version;
	private String path;
	private String description;

	@Override
	public Application get() {
		final Application.Builder applicationBuilder = new Application.Builder();
		applicationBuilder.withName(name).withVersion(version).withPath(path).withDescription(description);
		return applicationBuilder.newInstance();
	}

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
	public void setName(final String name) {
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
	public void setVersion(final String version) {
		this.version = version;
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
	public void setPath(final String path) {
		this.path = path;
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
	public void setDescription(final String description) {
		this.description = description;
	}

}
