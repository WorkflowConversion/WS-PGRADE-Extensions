package com.workflowconversion.importer.guse.vaadin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Vector;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.PortletRequestListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

import hu.sztaki.lpds.information.com.ServiceType;
import hu.sztaki.lpds.information.local.InformationBase;
import hu.sztaki.lpds.information.local.PropertyLoader;
import hu.sztaki.lpds.storage.inf.PortalStorageClient;

public class WorkflowImporterApplication extends Application
		implements Upload.SucceededListener, Upload.FailedListener, Upload.Receiver, PortletRequestListener {

	private static final long serialVersionUID = -1691439006632825854L;

	protected final Logger logger = LoggerFactory.getLogger(WorkflowImporterApplication.class);
	private Window mainWindow;
	private File uploadFile;
	private PortletRequest currentRequest;

	@Override
	public void init() {
		// TODO Auto-generated method stub
		logger.info("init()");
		mainWindow = new Window("Application");
		Label label = new Label("WorkflowImporterVaadinApplication!");
		mainWindow.addComponent(label);

		final Upload upload = new Upload("Upload your workflow here", this);
		upload.setButtonCaption("Upload now");
		upload.addListener((Upload.SucceededListener) this);
		upload.addListener((Upload.FailedListener) this);
		mainWindow.addComponent(upload);

		setMainWindow(mainWindow);
	}

	public OutputStream receiveUpload(String filename, String mimeType) {
		logger.info("receiving " + filename + " of the type " + mimeType);
		OutputStream outputStream;
		try {
			uploadFile = File.createTempFile("userupload_", ".tmp");
			logger.info("Uploading to " + uploadFile.getCanonicalFile());
			outputStream = new FileOutputStream(uploadFile);
		} catch (IOException e) {
			logger.error("Could not receive file", e);
			outputStream = null;
		}
		return outputStream;
	}

	// This is called if the upload is finished.
	public void uploadSucceeded(Upload.SucceededEvent event) {
		mainWindow.showNotification(new Notification("Workflow Importer", "File " + event.getFilename() + " uploaded.",
				Notification.TYPE_TRAY_NOTIFICATION));

		final Button submitButton = new Button("Submit");
		submitButton.addListener(new ClickListener() {

			public void buttonClick(ClickEvent event) {
				submitWorkflowFromFile(uploadFile);
			}
		});
		mainWindow.addComponent(submitButton);

	}

	// copies workflow into guse's internal storage... it's magic
	private void importWorkflow(final File serverSideFile) {
		try {
			logger.info("importing Workflow from " + serverSideFile.getCanonicalPath());
			Hashtable h = new Hashtable();
			ServiceType st = InformationBase.getI().getService("wfs", "portal", new Hashtable(), new Vector());
			h.put("senderObj", "ZipFileSender");
			h.put("portalURL", PropertyLoader.getInstance().getProperty("service.url"));
			h.put("wfsID", st.getServiceUrl());
			h.put("userID", currentRequest.getRemoteUser());

			Hashtable hsh = new Hashtable();
			st = InformationBase.getI().getService("storage", "portal", hsh, new Vector());
			PortalStorageClient psc = (PortalStorageClient) Class.forName(st.getClientObject()).newInstance();
			psc.setServiceURL(st.getServiceUrl());
			psc.setServiceID("/receiver");
			if (serverSideFile != null) {
				psc.fileUpload(serverSideFile, "fileName", h);
				logger.info("workflow has been imported");
			} else {
				throw new RuntimeException("serverSideFile is null, something went wrong with the data transfer.");
			}
		} catch (Exception e) {
			mainWindow.showNotification(new Notification("Workflow Importer",
					"Could not import workflow." + e.getMessage(), Notification.TYPE_ERROR_MESSAGE));
			logger.error("Error while importing workflow", e);
		}
	}

	// This is called if the upload fails.
	public void uploadFailed(Upload.FailedEvent event) {
		// Log the failure on screen.
		mainWindow.showNotification(new Notification("Workflow Importer", "Could not upload ." + event.getFilename(),
				Notification.TYPE_ERROR_MESSAGE));
	}

	private void submitWorkflowFromFile(final File file) {
		try {
			logger.info("submitting workflow from " + file.getCanonicalFile());
			importWorkflow(file);
		} catch (Exception e) {

		}
	}

	public void onRequestStart(PortletRequest request, PortletResponse response) {
		MDC.put("reference", Integer.toHexString(System.identityHashCode(this)));
		logger.info("onRequestStart() for UID: " + request.getRemoteUser() + ", name: "
				+ request.getUserPrincipal().getName());
		currentRequest = request;

	}

	public void onRequestEnd(PortletRequest request, PortletResponse response) {
		logger.info("onRequestEnd()");
		MDC.remove("reference");
	}
}
