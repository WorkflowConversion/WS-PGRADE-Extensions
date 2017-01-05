package com.workflowconversion.portlet.ui.upload.resource;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Link;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.workflowconversion.portlet.core.exception.ApplicationException;
import com.workflowconversion.portlet.core.middleware.MiddlewareProvider;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.ui.HorizontalSeparator;
import com.workflowconversion.portlet.ui.NotificationUtils;
import com.workflowconversion.portlet.ui.table.resource.ResourceCommittedListener;

/**
 * Modal dialog through which a file containing resources can be uploaded.
 * 
 * @author delagarza
 *
 */
public class BulkUploadResourcesDialog extends Window {

	private static final long serialVersionUID = -2587575066352088585L;

	private final static Logger LOG = LoggerFactory.getLogger(BulkUploadResourcesDialog.class);

	private final static String FILE_TYPE_TOOLTIP_HELP = "Currently only XML file types are supported";

	private File serverSideFile;
	// polling file upload progress happens in another thread
	private final AtomicLong contentLength;
	private final Collection<String> errors;
	private final ResourceCommittedListener resourceCommittedListener;
	private final MiddlewareProvider middlewareProvider;
	private final Upload upload;

	/**
	 * @param resourceCommittedListener
	 *            a listener that will be informed of parsed, valid, uploaded resources.
	 * @param middlewareProvider
	 *            the middleware provider.
	 */
	public BulkUploadResourcesDialog(final ResourceCommittedListener resourceCommittedListener,
			final MiddlewareProvider middlewareProvider) {
		Validate.notNull(resourceCommittedListener, "resourceCommittedListener cannot be null");
		Validate.notNull(middlewareProvider, "middlewareProvider cannot be null");

		this.resourceCommittedListener = resourceCommittedListener;
		this.middlewareProvider = middlewareProvider;

		this.contentLength = new AtomicLong();
		this.errors = new LinkedList<String>();
		this.upload = new Upload();

		setCaption("Upload CSV or XML File with Applications");
		setModal(true);
		setUpLayout();
	}

	private void setUpLayout() {
		// xml is the only format supported, for now
		final ComboBox fileTypeComboBox = new ComboBox("File type", Arrays.asList(FileType.XML));
		fileTypeComboBox.setNullSelectionAllowed(false);
		fileTypeComboBox.setImmediate(true);
		fileTypeComboBox.setWidth(70, Unit.POINTS);
		fileTypeComboBox.select(FileType.XML);
		fileTypeComboBox.setDescription(FILE_TYPE_TOOLTIP_HELP);

		final Link xmlSampleLink = new Link("Click to download a sample XML file", createSampleXmlStreamResource());

		final VerticalLayout fileOptionsLayout = new VerticalLayout();
		fileOptionsLayout.setMargin(true);
		fileOptionsLayout.setSpacing(true);
		fileOptionsLayout.addComponent(fileTypeComboBox);
		fileOptionsLayout.addComponent(xmlSampleLink);

		// upload control
		final Receiver fileReceiver = new FileReceiver();
		upload.setReceiver(fileReceiver);
		upload.setImmediate(true);
		upload.setButtonCaption("Upload file...");

		// set the listeners
		upload.addStartedListener(new Upload.StartedListener() {
			private static final long serialVersionUID = -6254312847336221333L;

			@Override
			public void uploadStarted(final StartedEvent event) {
				upload.setEnabled(false);
				errors.clear();
				contentLength.set(event.getContentLength());
				NotificationUtils.displayTrayMessage("Uploading file");
			}
		});

		upload.addProgressListener(new Upload.ProgressListener() {
			private static final long serialVersionUID = 6805421002888765593L;

			@Override
			public void updateProgress(final long readBytes, final long contentLength) {
				// TODO: do we need this much detail?
				// someLabel.setValue("Read " + readBytes + " of " + contentLength + " bytes");
			}
		});

		upload.addSucceededListener(new Upload.SucceededListener() {
			private static final long serialVersionUID = -2722969929750545998L;

			@Override
			public void uploadSucceeded(final SucceededEvent event) {
				NotificationUtils.displayTrayMessage("Parsing file");
				processFile(serverSideFile, (FileType) fileTypeComboBox.getValue());
			}
		});

		upload.addFailedListener(new Upload.FailedListener() {
			private static final long serialVersionUID = 4889495034113765498L;

			@Override
			public void uploadFailed(final FailedEvent event) {
				NotificationUtils.displayError("Could not upload file.", event.getReason());
				upload.setEnabled(true);
			}
		});

		final VerticalLayout layout = new VerticalLayout();
		layout.addComponent(fileOptionsLayout);
		layout.addComponent(new HorizontalSeparator());
		layout.addComponent(upload);
		layout.addComponent(new HorizontalSeparator());
		layout.setMargin(true);
		layout.setWidth(400, Unit.PIXELS);
		layout.setSpacing(true);
		setContent(layout);
	}

	private StreamResource createSampleXmlStreamResource() {
		final String fileName = "sample_resource_file.xml";
		return new StreamResource(new StreamSource() {
			private static final long serialVersionUID = -482825331518184714L;

			@Override
			public InputStream getStream() {
				try {
					return new BufferedInputStream(new FileInputStream(fileName));
				} catch (final IOException e) {
					LOG.error("Could not create stream for sample file", e);
					return null;
				}
			}
		}, fileName);
	}

	void processFile(final File file, final FileType fileType) {
		final BulkUploadListener bulkUploadListener = new DefaultBulkUploadListener(resourceCommittedListener);
		final AbstractFileProcessor fileProcessor;
		switch (fileType) {
		case XML:
			fileProcessor = new XMLBulkResourcesFileProcessor(file, bulkUploadListener, middlewareProvider.getAllMiddlewareTypes());
			break;
		default:
			LOG.error("Upload file format not handled: " + fileType);
			throw new ApplicationException("Unrecognized file format " + fileType);
		}
		// this has to be done in the same thread, otherwise, repaint requests from other thread
		// will not be processed!
		fileProcessor.start();
	}

	private class DefaultBulkUploadListener implements BulkUploadListener {

		private final ResourceCommittedListener resourceCommittedListener;
		private final Collection<String> errors;
		private final Collection<String> warnings;

		private DefaultBulkUploadListener(final ResourceCommittedListener resourceCommittedListener) {
			this.resourceCommittedListener = resourceCommittedListener;
			this.errors = new LinkedList<String>();
			this.warnings = new LinkedList<String>();
		}

		@Override
		public void parsingStarted() {
			NotificationUtils.displayTrayMessage("Parsing file, please be patient");
		}

		@Override
		public void parsingError(final String error, final long lineNumber) {
			parsingError("Line " + lineNumber + ": " + error);
		}

		@Override
		public void parsingError(final String error) {
			errors.add(error);
		}

		@Override
		public void parsingWarning(final String warning, final long lineNumber) {
			parsingWarning("Line " + lineNumber + ": " + warning);
		}

		@Override
		public void parsingWarning(String warning) {
			warnings.add(warning);
		}

		@Override
		public void parsingCompleted(final Collection<Resource> parsedResources) {
			int nAddedResources = 0;
			try {
				NotificationUtils.displayTrayMessage("Parsed " + parsedResources.size() + " resource(s).");
				for (final Resource parsedResource : parsedResources) {
					try {
						resourceCommittedListener.resourceCommitted(parsedResource);
						nAddedResources++;
					} catch (Exception e) {
						LOG.error("Could not add resource " + nAddedResources, e);
						errors.add("Could not add resource " + nAddedResources + ", reason: " + e.getMessage());
					}
				}
			} finally {
				try {
					if (errors.isEmpty()) {
						final String message = "Parsed and added " + nAddedResources + " resource(s) without errors.";
						NotificationUtils.displayTrayMessage(message);
					} else {
						final StringBuilder formattedError = new StringBuilder("<h3>Parsed and added ");
						formattedError.append(nAddedResources)
								.append(" resource(s) and found the following errors:</h3><ul>");
						for (final String error : errors) {
							formattedError.append("<li>").append(error);
						}
						formattedError.append("</ul>");
						NotificationUtils.displayError(formattedError.toString());
					}
					if (!warnings.isEmpty()) {
						final StringBuilder formattedWarnings = new StringBuilder(
								"<h3>The following warnings were generated while parsing the resources file:");
						for (final String warning : warnings) {
							formattedWarnings.append("<li>").append(warning);
						}
						formattedWarnings.append("</ul>");
						NotificationUtils.displayWarning(formattedWarnings.toString());
					}
				} finally {
					upload.setEnabled(true);
				}
			}
		}

	}

	private class FileReceiver implements Receiver {

		private static final long serialVersionUID = 5729540081679884127L;

		@Override
		public OutputStream receiveUpload(final String filename, final String mimeType) {
			// create a temporary file, server-side
			try {
				serverSideFile = File.createTempFile("upload_", null);
				return new BufferedOutputStream(new FileOutputStream(serverSideFile));
			} catch (IOException e) {
				throw new ApplicationException("Could not upload file", e);
			}
		}
	}

	private enum FileType {
		// TODO: add CSV support... maybe?
		CSV, XML
	}
}
