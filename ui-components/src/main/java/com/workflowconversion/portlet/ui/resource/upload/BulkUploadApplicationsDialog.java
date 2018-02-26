package com.workflowconversion.portlet.ui.resource.upload;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.FinishedEvent;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.workflowconversion.portlet.core.exception.ApplicationException;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.core.resource.ResourceProvider;
import com.workflowconversion.portlet.ui.HorizontalSeparator;
import com.workflowconversion.portlet.ui.NotificationUtils;

/**
 * Modal dialog through which a file containing resources can be uploaded.
 * 
 * @author delagarza
 *
 */
public class BulkUploadApplicationsDialog extends Window {

	private static final String SAMPLE_RESOURCE_FILE_NAME = "sample_resource_file.xml";

	private static final long serialVersionUID = -2587575066352088585L;

	private final static Logger LOG = LoggerFactory.getLogger(BulkUploadApplicationsDialog.class);

	private File serverSideFile;
	// polling file upload progress happens in another thread
	private final AtomicLong contentLength;
	private final Collection<String> errors;
	private final ResourceProvider resourceProvider;
	private final Upload upload;

	/**
	 * @param resourceProvider
	 *            the resource provider with which the applications will be associated.
	 */
	public BulkUploadApplicationsDialog(final ResourceProvider resourceProvider) {
		Validate.notNull(resourceProvider, "resourceProvider cannot be null.");
		Validate.isTrue(resourceProvider.canAddApplications(),
				"the passed resource provider does not support adding applicatons.");

		this.resourceProvider = resourceProvider;

		this.contentLength = new AtomicLong();
		this.errors = new LinkedList<String>();
		this.upload = new Upload();

		setCaption("Upload applications XML file");
		setModal(true);
		setClosable(false);
		setResizable(false);
		setUpLayout();
	}

	private void setUpLayout() {
		final Link xmlSampleLink = new Link();
		xmlSampleLink.setCaption("Download a sample XML file");

		final FileDownloader fileDownloader = new FileDownloader(createSampleXmlStreamResource());
		fileDownloader.extend(xmlSampleLink);

		final VerticalLayout fileOptionsLayout = new VerticalLayout();
		fileOptionsLayout.setSpacing(true);
		fileOptionsLayout.addComponent(xmlSampleLink);

		final Button closeButton = new Button("Close");
		closeButton.setImmediate(true);
		closeButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 8033668816964396887L;

			@Override
			public void buttonClick(final ClickEvent event) {
				BulkUploadApplicationsDialog.this.close();
			}
		});

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
				closeButton.setEnabled(false);
				upload.setEnabled(false);
				errors.clear();
				contentLength.set(event.getContentLength());
				NotificationUtils.displayTrayMessage("Uploading file");
			}
		});

		upload.addSucceededListener(new Upload.SucceededListener() {
			private static final long serialVersionUID = -2722969929750545998L;

			@Override
			public void uploadSucceeded(final SucceededEvent event) {
				NotificationUtils.displayTrayMessage("Parsing file");
				try {
					// support only xml for now
					processFile(serverSideFile, FileType.XML);
				} finally {
					upload.setEnabled(true);
				}
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

		upload.addFinishedListener(new Upload.FinishedListener() {
			private static final long serialVersionUID = -2182290023265251004L;

			@Override
			public void uploadFinished(final FinishedEvent event) {
				closeButton.setEnabled(true);
			}
		});

		final HorizontalLayout footerLayout = new HorizontalLayout();
		footerLayout.setWidth(100, Unit.PERCENTAGE);
		footerLayout.addComponent(closeButton);
		footerLayout.setComponentAlignment(closeButton, Alignment.BOTTOM_RIGHT);

		final VerticalLayout layout = new VerticalLayout();
		layout.setMargin(true);
		layout.setSpacing(true);
		layout.addComponent(upload);
		layout.addComponent(fileOptionsLayout);
		layout.addComponent(new HorizontalSeparator());
		layout.addComponent(footerLayout);
		layout.setWidth(400, Unit.PIXELS);
		setContent(layout);
	}

	private StreamResource createSampleXmlStreamResource() {
		return new StreamResource(new StreamSource() {
			private static final long serialVersionUID = -482825331518184714L;

			@Override
			public InputStream getStream() {
				try {
					return new BufferedInputStream(
							BulkUploadApplicationsDialog.class.getResourceAsStream('/' + SAMPLE_RESOURCE_FILE_NAME));
				} catch (final Exception e) {
					LOG.error("Could not create stream for sample file", e);
					return null;
				}
			}
		}, SAMPLE_RESOURCE_FILE_NAME);
	}

	void processFile(final File file, final FileType fileType) {
		final BulkUploadListener<Resource> bulkUploadListener = new DefaultBulkUploadListener();
		final AbstractFileProcessor<Resource> fileProcessor;
		switch (fileType) {
		case XML:
			fileProcessor = new XMLBulkResourcesFileProcessor(file, bulkUploadListener, resourceProvider);
			break;
		default:
			LOG.error("Upload file format not handled: " + fileType);
			throw new ApplicationException("Unrecognized file format " + fileType);
		}
		// this has to be done in the same thread, otherwise, repaint requests from other thread
		// will not be processed!
		fileProcessor.start();
	}

	private class DefaultBulkUploadListener implements BulkUploadListener<Resource> {

		private final Collection<String> errors;

		private DefaultBulkUploadListener() {
			this.errors = new LinkedList<String>();
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
		public void parsingCompleted(final Collection<Resource> parsedResources) {
			try {
				for (final Resource parsedResource : parsedResources) {
					resourceProvider.save(parsedResource);
				}
				final int numberOfParsedApplications = getApplicationCount(parsedResources);
				if (errors.isEmpty()) {
					final String message = "Processed and added " + numberOfParsedApplications
							+ " application(s) without errors.";
					NotificationUtils.displayTrayMessage(message);
				} else {
					final StringBuilder formattedError = new StringBuilder("<h3>Processed and added ");
					formattedError.append(numberOfParsedApplications)
							.append(" applications(s) and found the following errors:</h3><ul>");
					for (final String error : errors) {
						formattedError.append("<li>").append(error);
					}
					formattedError.append("</ul>");
					NotificationUtils.displayError(formattedError.toString());
				}
			} finally {
				upload.setEnabled(true);
			}
		}

		private int getApplicationCount(final Collection<Resource> parsedResources) {
			int nApps = 0;

			for (final Resource parsedResource : parsedResources) {
				nApps += parsedResource.getApplications().size();
			}

			return nApps;
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
			} catch (final IOException e) {
				throw new ApplicationException("Could not upload file", e);
			}
		}
	}

	private enum FileType {
		// TODO: add CSV support... maybe?
		CSV, XML
	}
}
