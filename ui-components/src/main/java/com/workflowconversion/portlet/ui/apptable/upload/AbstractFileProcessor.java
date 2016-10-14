package com.workflowconversion.portlet.ui.apptable.upload;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workflowconversion.portlet.core.app.Application;

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
	private final Collection<Application> parsedApplications;

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
		this.parsedApplications = new LinkedList<Application>();
		this.validMiddlewareTypes = validMiddlewareTypes;
	}

	/**
	 * Starts parsing of the provided file.
	 */
	public final void start() {
		try {
			listener.parsingStarted();
			parseFile(serverSideFile);
		} catch (Exception e) {
			LOG.error("Could not parse file from " + serverSideFile.getAbsolutePath(), e);
			listener.parsingError("Could not parse uploaded file, reason: " + e.getMessage());
		} finally {
			listener.parsingCompleted(parsedApplications);
		}

	}

	/**
	 * Parsed an uploaded file.
	 */
	abstract void parseFile(final File serverSideFile) throws Exception;

	/**
	 * Attempts to add a parsed application to the list of valid, parsed applications.
	 * 
	 * @param application
	 *            the application to add after validation.
	 * @param lineNumber
	 *            the current line number.
	 */
	final protected void addParsedApplication(final Application application, final long lineNumber) {
		if (isApplicationValid(application, lineNumber)) {
			this.parsedApplications.add(application);
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
		if (StringUtils.isBlank(application.getResource())) {
			error.append("empty resource, ");
		}
		if (StringUtils.isBlank(application.getVersion())) {
			error.append("empty version, ");
		}
		if (StringUtils.isBlank(application.getResourceType())) {
			error.append("empty resource type, ");
		} else {
			if (!validMiddlewareTypes.contains(application.getResourceType())) {
				error.append("invalid resource type [" + application.getResourceType() + "], ");
			}
		}

		if (error.length() > 0) {
			listener.parsingError(error.toString(), lineNumber);
		}

		return error.length() == 0;
	}

}
