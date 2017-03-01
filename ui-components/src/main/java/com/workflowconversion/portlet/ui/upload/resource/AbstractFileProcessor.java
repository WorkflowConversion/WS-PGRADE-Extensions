package com.workflowconversion.portlet.ui.upload.resource;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.core.resource.Queue;
import com.workflowconversion.portlet.core.resource.Resource;

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
	 * @param resource
	 *            the resource to add after validation
	 * @param lineNumber
	 *            the line number
	 */
	final protected void addParsedResource(final Resource resource, final long lineNumber) {
		if (isResourceValid(resource, lineNumber)) {
			final String key = resource.generateKey();
			if (!parsedResources.containsKey(key)) {
				parsedResources.put(key, resource);
			} else {
				listener.parsingWarning("The file already contains a resource with the same name and type (duplicate: "
						+ resource + ')', lineNumber);
			}
		}
	}

	private boolean isResourceValid(final Resource resource, final long lineNumber) {
		final StringBuilder error = new StringBuilder();
		if (StringUtils.isBlank(resource.getName())) {
			error.append("empty name, ");
		}
		if (!validMiddlewareTypes.contains(resource.getType())) {
			error.append("invalid resource type [" + resource.getType() + "], ");
		}

		if (error.length() > 0) {
			listener.parsingError(error.toString(), lineNumber);
		}

		return error.length() == 0;
	}

	/**
	 * Attempts to add a parsed queue to the passed resource.
	 * 
	 * @param resource
	 *            the resource to which the queue will belong to
	 * @param queue
	 *            the parsed queue
	 * @param lineNumber
	 *            the line number
	 */
	final protected void addParsedQueue(final Resource resource, final Queue queue, final long lineNumber) {
		if (isQueueValid(queue, lineNumber)) {
			if (!resource.containsQueue(queue)) {
				queue.setResource(resource);
				resource.addQueue(queue);
			} else {
				listener.parsingWarning("The resource [" + resource
						+ "] declared in the upload file already contains a queue with the same name (duplicate: "
						+ queue + ')', lineNumber);
			}
		}
	}

	private boolean isQueueValid(final Queue queue, final long lineNumber) {
		if (StringUtils.isBlank(queue.getName())) {
			listener.parsingError("empty queue name", lineNumber);
			return false;
		}
		return true;
	}

	/**
	 * Attempts to add a parsed application to the passed resource.
	 * 
	 * @param resource
	 *            the resource to which the application belongs.
	 * @param application
	 *            the application to add after validation.
	 * 
	 * @param lineNumber
	 *            the current line number.
	 */
	final protected void addParsedApplication(final Resource resource, final Application application,
			final long lineNumber) {
		if (isApplicationValid(application, lineNumber)) {
			if (!resource.containsApplication(application)) {
				application.setResource(resource);
				resource.addApplication(application);
			} else {
				listener.parsingWarning("The resource [" + resource
						+ "] declared in the upload file already contains an application with the same name, version, path (duplicate: "
						+ application + ')', lineNumber);
			}
		}
	}

	// checks if an application is valid, if not, an error will be added
	private boolean isApplicationValid(final Application application, final long lineNumber) {
		final StringBuilder error = new StringBuilder();
		if (StringUtils.isBlank(application.getName())) {
			error.append("empty name, ");
		}
		if (StringUtils.isBlank(application.getPath())) {
			error.append("empty path, ");
		}
		if (StringUtils.isBlank(application.getVersion())) {
			error.append("empty version, ");
		}

		if (error.length() > 0) {
			listener.parsingError(error.toString(), lineNumber);
		}

		return error.length() == 0;
	}

}
