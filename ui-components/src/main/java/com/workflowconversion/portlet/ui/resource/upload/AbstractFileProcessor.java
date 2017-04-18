package com.workflowconversion.portlet.ui.resource.upload;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.core.resource.ResourceProvider;

/**
 * Abstract class that contains the common methods for processing bulk uploads.
 * 
 * @author delagarza
 *
 */
public abstract class AbstractFileProcessor {

	private final static Logger LOG = LoggerFactory.getLogger(AbstractFileProcessor.class);

	protected final File serverSideFile;
	protected final BulkUploadListener listener;
	protected final ResourceProvider resourceProvider;
	protected int nParsedApplications;

	/**
	 * Constructor.
	 * 
	 * @param serverSideFile
	 *            the file on the server to parse.
	 * @param listener
	 *            the listener.
	 * @param validMiddlewareTypes
	 *            the set of valid middleware types.
	 */
	protected AbstractFileProcessor(final File serverSideFile, final BulkUploadListener listener,
			final ResourceProvider resourceProvider) {
		Validate.notNull(serverSideFile,
				"serverSideFile cannot be null. This seems to be a coding problem and should be reported.");
		Validate.isTrue(serverSideFile.exists(),
				"serverSideFile does not exist. This seems to be a coding problem and should be reported.");
		Validate.notNull(listener,
				"listener cannot be null. This seems to be a coding problem and should be reported.");
		Validate.notNull(resourceProvider,
				"resourceProvider cannot be null. This seems to be a coding problem and should be reported.");
		Validate.isTrue(resourceProvider.canAddApplications(),
				"the passed resource provider does not support adding/editing applications. This seems to be a coding problem and should be reported.");
		this.listener = listener;
		this.serverSideFile = serverSideFile;
		this.resourceProvider = resourceProvider;
	}

	/**
	 * Starts parsing of the provided file.
	 */
	public final void start() {
		try {
			listener.parsingStarted();
			parseFile(serverSideFile);
		} catch (final Exception e) {
			LOG.error("Could not parse file from " + serverSideFile.getAbsolutePath(), e);
			listener.parsingError("Could not parse uploaded file, reason: " + e.getMessage());
		} finally {
			listener.parsingCompleted(nParsedApplications);
		}

	}

	/**
	 * Parsed an uploaded file.
	 */
	abstract void parseFile(final File serverSideFile) throws Exception;

	/**
	 * Attempts to add a parsed resource.
	 * 
	 * @param name
	 *            the name of the resource.
	 * @param type
	 *            the type of the resource.
	 * @param lineNumber
	 *            the line number.
	 * @param the
	 *            added resource, or {@code null} if nothing was added.
	 */
	final Resource findResource(final String name, final String type, final long lineNumber) {
		final StringBuilder error = new StringBuilder();
		if (StringUtils.isBlank(name)) {
			error.append("empty resource name, ");
		}
		if (StringUtils.isBlank(type)) {
			error.append("empty resource type, ");
		}
		if (error.length() > 0) {
			listener.parsingError(error.toString(), lineNumber);
			return null;
		}
		final Resource resource = resourceProvider.getResource(name, type);
		if (resource == null) {
			error.append("resource [name=" + name + ", type=" + type + "] not found");
			listener.parsingError(error.toString(), lineNumber);
		}
		return resource;
	}

	/**
	 * Attempts to add a parsed application to the passed resource.
	 * 
	 * @param resource
	 *            the resource to which the application belongs.
	 * @param applicationBuilder
	 *            the application builder to use to add the parsed application.
	 * 
	 * @param lineNumber
	 *            the current line number.
	 */
	final protected void addParsedApplication(final Resource resource, final String applicationName,
			final String applicationVersion, final String applicationPath, final String applicationDescription,
			final long lineNumber) {
		final Application parsedApplication = buildApplication(applicationName, applicationVersion, applicationPath,
				applicationDescription, lineNumber);
		if (parsedApplication != null) {
			if (resource != null) {
				if (resource.canModifyApplications()) {
					nParsedApplications++;
					if (resource.getApplication(parsedApplication.getName(), parsedApplication.getVersion(),
							parsedApplication.getPath()) == null) {
						resource.addApplication(parsedApplication);
						if (LOG.isInfoEnabled()) {
							LOG.info("Added application :" + parsedApplication);
						}
					} else {
						resource.saveApplication(parsedApplication);
						if (LOG.isInfoEnabled()) {
							LOG.info("Replacing application with: " + parsedApplication);
						}
					}
				} else {
					listener.parsingWarning("Cannot add application [" + parsedApplication + "] to read-only resource.",
							lineNumber);
				}
			} else {
				listener.parsingWarning("Cannot add application [" + parsedApplication + "] to invalid resource.",
						lineNumber);
			}
		}
	}

	private Application buildApplication(final String name, final String version, final String path,
			final String description, final long lineNumber) {
		final StringBuilder error = new StringBuilder();
		if (StringUtils.isBlank(name)) {
			error.append("empty name, ");
		}
		if (StringUtils.isBlank(version)) {
			error.append("empty version, ");
		}
		if (StringUtils.isBlank(path)) {
			error.append("empty path, ");
		}

		if (error.length() > 0) {
			listener.parsingError(error.toString(), lineNumber);
			return null;
		}
		final Application.Builder applicationBuilder = new Application.Builder();
		applicationBuilder.withName(name).withVersion(version).withPath(path).withDescription(description);
		return applicationBuilder.newInstance();
	}
}
