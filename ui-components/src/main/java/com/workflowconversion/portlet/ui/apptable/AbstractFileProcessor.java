package com.workflowconversion.portlet.ui.apptable;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.Label;
import com.vaadin.ui.ProgressIndicator;
import com.workflowconversion.portlet.core.app.Application;
import com.workflowconversion.portlet.core.exception.ApplicationException;

/**
 * Abstract class that contains the common methods for processing bulk uploads.
 * 
 * @author delagarza
 *
 */
abstract class AbstractFileProcessor implements Runnable {

	private final static Logger LOG = LoggerFactory.getLogger(AbstractFileProcessor.class);

	protected final ApplicationCommittedListener listener;
	protected final ProgressIndicator progressIndicator;
	protected final Label verboseProgressLabel;
	protected final File serverSideFile;
	protected final Set<String> validMiddlewareTypes;
	private final Collection<Application> parsedApplications;
	private final Collection<String> errors;

	AbstractFileProcessor(final File serverSideFile, final ApplicationCommittedListener listener,
			final ProgressIndicator progressIndicator, final Label verboseProgressLabel,
			final Set<String> validMiddlewareTypes) {
		Validate.notNull(serverSideFile, "serverSideFile cannot be null");
		Validate.isTrue(serverSideFile.exists(), "serverSideFile does not exist");
		Validate.notNull(listener, "listener cannot be null");
		Validate.notNull(progressIndicator, "progressIndicator cannot be null");
		Validate.notNull(verboseProgressLabel, "verboseProgressLabel cannot be null");
		Validate.notEmpty(validMiddlewareTypes, "validMiddlewareTypes cannot be null or empty");
		this.listener = listener;
		this.progressIndicator = progressIndicator;
		this.verboseProgressLabel = verboseProgressLabel;
		this.serverSideFile = serverSideFile;
		this.parsedApplications = new LinkedList<Application>();
		this.validMiddlewareTypes = validMiddlewareTypes;
		this.errors = new LinkedList<String>();
	}

	@Override
	public final void run() {
		verboseProgressLabel.setValue("Parsing file");
		try {
			parseFile(serverSideFile);
		} catch (Exception e) {
			LOG.error("Could not parse file from " + serverSideFile.getAbsolutePath(), e);
			throw new ApplicationException("Could not parse file from " + serverSideFile, e);
		}

		verboseProgressLabel.setValue("Adding applications");
		progressIndicator.setValue(0f);
		int addedApplications = 0;
		for (final Application parsedApplication : parsedApplications) {
			progressIndicator.setValue(((float) addedApplications) / parsedApplications.size());
			listener.applicationCommitted(parsedApplication);
		}
		verboseProgressLabel.setValue("Processing complete");

	}

	/**
	 * Parsed an uploaded file.
	 */
	abstract void parseFile(final File serverSideFile) throws Exception;

	final Collection<String> getProcessingErrors() {
		return this.errors;
	}

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
			errors.add("Line " + lineNumber + ": " + error.toString());
		}

		return error.length() == 0;
	}

}
