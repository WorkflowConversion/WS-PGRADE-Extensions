package com.workflowconversion.portlet.ui.resource.upload;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.core.utils.KeyUtils;

/**
 * Abstract class that contains the common methods for processing bulk uploads.
 * 
 * @author delagarza
 *
 */
public abstract class AbstractFileProcessor {

	private final static Logger LOG = LoggerFactory.getLogger(AbstractFileProcessor.class);

	protected final File serverSideFile;
	protected final Set<String> validMiddlewareTypes;
	protected final BulkUploadListener listener;
	private final Map<String, Resource> parsedResources;

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
			final Set<String> validMiddlewareTypes) {
		Validate.notNull(serverSideFile, "serverSideFile cannot be null");
		Validate.isTrue(serverSideFile.exists(), "serverSideFile does not exist");
		Validate.notNull(listener, "listener cannot be null");
		Validate.notEmpty(validMiddlewareTypes, "validMiddlewareTypes cannot be null or empty");
		this.listener = listener;
		this.serverSideFile = serverSideFile;
		this.parsedResources = new TreeMap<String, Resource>();
		this.validMiddlewareTypes = validMiddlewareTypes;
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
			listener.parsingCompleted(parsedResources.values());
		}

	}

	/**
	 * Parsed an uploaded file.
	 */
	abstract void parseFile(final File serverSideFile) throws Exception;

	/**
	 * Attempts to add a parsed resource.
	 * 
	 * @param resourceBuilder
	 *            the resource builder to use to build the parsed resource.
	 * @param lineNumber
	 *            the line number.
	 * @param the
	 *            added resource, or {@code null} if nothing was added.
	 */
	final Resource addParsedResource(final Resource.Builder resourceBuilder, final long lineNumber) {
		final Resource resource = buildResource(resourceBuilder, lineNumber);
		if (resource != null) {
			final String key = KeyUtils.generate(resource);
			if (!parsedResources.containsKey(key)) {
				parsedResources.put(key, resource);
			} else {
				listener.parsingWarning("The file already contains a resource with the same name and type (duplicate: "
						+ resource + ')', lineNumber);
			}
		}
		return resource;
	}

	private Resource buildResource(final Resource.Builder resourceBuilder, final long lineNumber) {
		final StringBuilder error = new StringBuilder();
		if (StringUtils.isBlank(resourceBuilder.getName())) {
			error.append("empty name, ");
		}
		if (!validMiddlewareTypes.contains(resourceBuilder.getType())) {
			error.append("invalid resource type [" + resourceBuilder.getType() + "], ");
		}

		if (error.length() > 0) {
			listener.parsingError(error.toString(), lineNumber);
			return null;
		}

		resourceBuilder.canModifyApplications(true);
		return resourceBuilder.newInstance();
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
	final protected void addParsedApplication(final Resource resource, final Application.Builder applicationBuilder,
			final long lineNumber) {
		final Application parsedApplication = buildApplication(applicationBuilder, lineNumber);
		if (parsedApplication != null) {
			if (resource != null) {
				if (resource.getApplication(parsedApplication.getName(), parsedApplication.getVersion(),
						parsedApplication.getPath()) == null) {
					resource.addApplication(parsedApplication);
				} else {
					listener.parsingWarning("The resource [" + resource
							+ "] declared in the upload file already contains an application with the same name, version, path (duplicate: "
							+ parsedApplication + ')', lineNumber);
				}
			} else {
				listener.parsingWarning("Cannot add application [" + parsedApplication + "] to invalid resource.",
						lineNumber);
			}
		}
	}

	private Application buildApplication(final Application.Builder applicationBuilder, final long lineNumber) {
		final StringBuilder error = new StringBuilder();
		if (StringUtils.isBlank(applicationBuilder.getName())) {
			error.append("empty name, ");
		}
		if (StringUtils.isBlank(applicationBuilder.getPath())) {
			error.append("empty path, ");
		}
		if (StringUtils.isBlank(applicationBuilder.getVersion())) {
			error.append("empty version, ");
		}

		if (error.length() > 0) {
			listener.parsingError(error.toString(), lineNumber);
			return null;
		}

		return applicationBuilder.newInstance();
	}
}
