package com.workflowconversion.portlet.ui.workflow.upload;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.workflowconversion.portlet.core.exception.ApplicationException;
import com.workflowconversion.portlet.ui.HorizontalSeparator;

/**
 * Modal dialog to upload workflows to the staging area.
 * 
 * @author delagarza
 *
 */
public class WorkflowUploadDialog extends Window {
	private static final long serialVersionUID = -451586894660395610L;

	private final static Logger LOG = LoggerFactory.getLogger(WorkflowUploadDialog.class);
	private static final String NOTIFICATION_TITLE = "Workflow Upload";

	private final WorkflowUploadedListener listener;
	private final Upload upload;
	private final Label status;
	private File serverSideFile;

	public WorkflowUploadDialog(final WorkflowUploadedListener listener) {
		Validate.notNull(listener, "listener cannot be null");
		this.listener = listener;

		this.upload = new Upload();
		this.status = new Label("Waiting for file");

		setCaption("Upload a Workflow");
		setModal(true);
		initUI();
	}

	private void initUI() {
		final Receiver fileReceiver = new FileReceiver();
		upload.setReceiver(fileReceiver);
		upload.setImmediate(true);
		upload.setButtonCaption("Upload file...");

		upload.addListener(new Upload.StartedListener() {
			private static final long serialVersionUID = -6254312847336221333L;

			@Override
			public void uploadStarted(final StartedEvent event) {
				upload.setEnabled(false);
				status.setValue("Uploading workflow");
			}
		});

		upload.addListener(new Upload.SucceededListener() {
			private static final long serialVersionUID = -2722969929750545998L;

			@Override
			public void uploadSucceeded(final SucceededEvent event) {
				try {
					status.setValue("Workflow uploaded");
					listener.workflowUploaded(serverSideFile);
					getWindow().showNotification(NOTIFICATION_TITLE, "The workflow was uploaded to the staging area.",
							Notification.TYPE_TRAY_NOTIFICATION, true);
				} catch (Exception e) {
					LOG.error("Could not upload workflow file", e);
					getWindow().showNotification(NOTIFICATION_TITLE,
							"There was an error adding the uploaded workflow to the staging area. Reason: "
									+ e.getMessage(),
							Notification.TYPE_ERROR_MESSAGE, true);
				} finally {
					upload.setEnabled(true);
				}
			}
		});

		upload.addListener(new Upload.FailedListener() {
			private static final long serialVersionUID = 4889495034113765498L;

			@Override
			public void uploadFailed(final FailedEvent event) {
				status.setValue("Upload failed");
				LOG.error("Could not upload workflow file", event.getReason());
				showNotification(NOTIFICATION_TITLE, "Could not upload file. Reason: " + event.getReason().getMessage(),
						Notification.TYPE_ERROR_MESSAGE);
				upload.setEnabled(true);
			}
		});

		status.setCaption("Current state");

		final Layout statusDisplayLayout = new FormLayout();
		statusDisplayLayout.addComponent(status);
		final Panel statusDisplayPanel = new Panel("Status");
		statusDisplayPanel.addComponent(statusDisplayLayout);

		final VerticalLayout layout = (VerticalLayout) getContent();
		layout.addComponent(upload);
		layout.addComponent(new HorizontalSeparator());
		layout.addComponent(statusDisplayPanel);
		layout.setMargin(true);
		layout.setSpacing(true);
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
}
