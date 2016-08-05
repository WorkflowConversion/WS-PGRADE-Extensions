package com.workflowconversion.importer.guse.vaadin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Role;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import com.vaadin.Application;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.terminal.UserError;
import com.vaadin.terminal.gwt.server.PortletRequestListener;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

import hu.sztaki.lpds.information.com.ServiceType;
import hu.sztaki.lpds.information.local.InformationBase;
import hu.sztaki.lpds.information.local.PropertyLoader;
import hu.sztaki.lpds.storage.inf.PortalStorageClient;

public class WorkflowImporterApplication extends Application implements Upload.SucceededListener, Upload.FailedListener,
		Upload.Receiver, PortletRequestListener, ClickListener {

	private static final long serialVersionUID = -1691439006632825854L;
	// a simple key for logging MDC
	private static final String MDC_REFERENCE_KEY = "reference";
	// these are the name of the properties containing the values we need to
	// connect to the database and other needed gUSE-specific values
	private static final String GUSE_KEY_DATABASE_DRIVER = "guse.system.database.driver";
	private static final String GUSE_KEY_DATABASE_URL = "guse.system.database.url";
	private static final String GUSE_KEY_DATABASE_USER = "guse.system.database.user";
	private static final String GUSE_KEY_DATABASE_PASSWORD = "guse.system.database.password";
	private static final String GUSE_KEY_INFORMATION_SERVICE_URL = "is.url";
	private static final String GUSE_KEY_INFORMATION_SERVICE_ID = "is.id";

	protected final static Logger LOG = LoggerFactory.getLogger(WorkflowImporterApplication.class);

	private Window mainWindow;
	private File uploadFile;
	private PortletRequest currentRequest;
	private User currentUser;
	private Set<String> currentRoles;

	@Override
	public void init() {
		LOG.info("initializing WorkflowImporterApplication");
		// setTheme("reindeer");
		mainWindow = new Window("WorkflowImporterPortlet");
		setMainWindow(mainWindow);

		if (isCurrentUserAuthenticated()) {
			if (isGUSEInitialized()) {
				setUpUI();
			} else {
				setUpNonInitializedUI();
			}
		} else {
			setUpGuestUI();
		}
	}

	private boolean isGUSEInitialized() {
		// we should have all of the required properties, otherwise we could
		// assume that the portlet has not been "guse-initialized"
		boolean initialized = true;
		// check all of the required properties
		for (final String key : new String[] { GUSE_KEY_DATABASE_DRIVER, GUSE_KEY_DATABASE_URL, GUSE_KEY_DATABASE_USER,
				GUSE_KEY_DATABASE_PASSWORD, GUSE_KEY_INFORMATION_SERVICE_URL, GUSE_KEY_INFORMATION_SERVICE_ID }) {
			final String value = PropertyLoader.getInstance().getProperty(key);
			if (StringUtils.isBlank(value)) {
				LOG.error("The required gUSE property [" + key
						+ "] has not been defined. It is highly probable that the portlet needs to be initialized by restarting gUSE.");
				// let it fail if at least one property is missing
				initialized = false;
			} else {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Found gUSE property [" + key + "] with value [" + value + ']');
				}
			}
		}
		return initialized;
	}

	private void setUpNonInitializedUI() {
		final Embedded icon = new Embedded(null, new ThemeResource("../runo/icons/64/attention.png"));
		icon.setWidth("64px");
		icon.setHeight("64px");
		setUpWarningUI("This portlet has not been properly initialized! You probably need to restart gUSE.", icon);
	}

	// ui for unregistered users
	private void setUpGuestUI() {
		final Embedded icon = new Embedded(null, new ThemeResource("../runo/icons/64/lock.png"));
		icon.setWidth("64px");
		icon.setHeight("64px");
		setUpWarningUI("You need to be logged-in to access this portlet.", icon);
	}

	private void setUpWarningUI(final String description, final Embedded icon) {
		final Label warningLabel = new Label("<h2>" + description + "</h2>", Label.CONTENT_XHTML);

		final HorizontalLayout layout = new HorizontalLayout();
		layout.addComponent(icon);
		layout.addComponent(warningLabel);

		final Panel panel = new Panel("We're sorry");
		panel.setComponentError(new UserError("<h3>WorkflowImportPortlet is not available.</h3>",
				UserError.CONTENT_XHTML, UserError.ERROR));
		panel.setContent(layout);

		mainWindow.addComponent(panel);
		mainWindow.showNotification("Error", "<h2>" + description + "</h2>", Notification.TYPE_ERROR_MESSAGE, true);
	}

	// UI for normal end users
	private void setUpUI() {
		final Label userLabel = new Label(getUserFullName());
		mainWindow.addComponent(userLabel);

		final Upload upload = new Upload("Upload your workflow here", this);
		upload.setButtonCaption("Upload now");
		upload.addListener((Upload.SucceededListener) this);
		upload.addListener((Upload.FailedListener) this);
		mainWindow.addComponent(upload);
	}

	private String getUserFullName() {
		if (isCurrentUserAuthenticated()) {
			return currentUser.getFullName();
		} else {
			return "Liferay User";
		}
	}

	private boolean canEditDatabase() {
		return currentRoles.contains("Administrator");
	}

	private boolean canReadDatabase() {
		return currentRoles.contains("User") || currentRoles.contains("End User");
	}

	public OutputStream receiveUpload(String filename, String mimeType) {
		if (LOG.isInfoEnabled()) {
			LOG.info("receiving " + filename + " of the type " + mimeType);
		}
		OutputStream outputStream;
		try {
			uploadFile = File.createTempFile("userupload_", ".tmp");
			if (LOG.isInfoEnabled()) {
				LOG.info("Uploading to " + uploadFile.getCanonicalFile());
			}
			outputStream = new FileOutputStream(uploadFile);
		} catch (IOException e) {
			LOG.error("Could not receive file", e);
			outputStream = null;
		}
		return outputStream;
	}

	// This is called if the upload is finished.
	public void uploadSucceeded(Upload.SucceededEvent event) {
		mainWindow.showNotification(new Notification("Workflow Importer", "File " + event.getFilename() + " uploaded.",
				Notification.TYPE_TRAY_NOTIFICATION));

		final Button submitButton = new Button("Submit");
		submitButton.addListener(this);
		mainWindow.addComponent(submitButton);
	}

	public void buttonClick(final ClickEvent event) {
		submitWorkflowFromFile(uploadFile);
	}

	// copies workflow into guse's internal storage... it's magic
	private void importWorkflow(final File serverSideFile) {
		try {
			if (serverSideFile == null) {
				throw new NullPointerException("serverSideFile is null, something went wrong with the data transfer.");
			}
			if (LOG.isInfoEnabled()) {
				LOG.info("importing Workflow from " + serverSideFile.getCanonicalPath());
			}
			Hashtable<String, Object> h = new Hashtable<String, Object>();
			ServiceType st = InformationBase.getI().getService("wfs", "portal", new Hashtable<String, Object>(),
					new Vector<Object>());
			h.put("senderObj", "ZipFileSender");
			h.put("portalURL", PropertyLoader.getInstance().getProperty("service.url"));
			h.put("wfsID", st.getServiceUrl());
			h.put("userID", currentRequest.getRemoteUser());

			Hashtable<String, Object> hsh = new Hashtable<String, Object>();
			st = InformationBase.getI().getService("storage", "portal", hsh, new Vector<Object>());
			PortalStorageClient psc = (PortalStorageClient) Class.forName(st.getClientObject()).newInstance();
			psc.setServiceURL(st.getServiceUrl());
			psc.setServiceID("/receiver");
			psc.fileUpload(serverSideFile, "fileName", h);
			LOG.info("workflow has been imported");
		} catch (Exception e) {
			mainWindow.showNotification(new Notification("Workflow Importer",
					"Could not import workflow." + e.getMessage(), Notification.TYPE_ERROR_MESSAGE));
			LOG.error("Error while importing workflow", e);
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
			if (LOG.isInfoEnabled()) {
				LOG.info("submitting workflow from " + file.getCanonicalFile());
			}
			importWorkflow(file);
		} catch (Exception e) {

		}
	}

	public void onRequestStart(PortletRequest request, PortletResponse response) {
		MDC.put(MDC_REFERENCE_KEY, Integer.toHexString(System.identityHashCode(this)));
		currentRequest = request;
		saveUserInformation(request);
		testDatabase();
	}

	private void saveUserInformation(PortletRequest request) {
		List<Role> userRoles = Collections.emptyList();
		try {
			currentUser = PortalUtil.getUser(request);
			if (isCurrentUserAuthenticated()) {
				userRoles = currentUser.getRoles();
			}
		} catch (PortalException | SystemException e) {
			LOG.error("Could not retrieve user information from the current PortletRequest", e);
			throw new RuntimeException("Could not retrieve user information from the current PortletRequest", e);
		}
		if (LOG.isDebugEnabled() && isCurrentUserAuthenticated()) {
			LOG.debug("Access from uid=" + currentUser.getUserId() + ", fullName=" + currentUser.getFullName());
		}
		currentRoles = new TreeSet<String>();
		for (final Role role : userRoles) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("current user role found: name=" + role.getName() + ", typeLabel=" + role.getTypeLabel()
						+ ", title=" + role.getTitle());
			}
			currentRoles.add(role.getName());
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("can edit database = " + canEditDatabase());
			LOG.debug("can read database = " + canReadDatabase());
		}
	}

	public void onRequestEnd(PortletRequest request, PortletResponse response) {
		MDC.remove(MDC_REFERENCE_KEY);
	}

	private boolean isCurrentUserAuthenticated() {
		return currentUser != null;
	}

	private void testDatabase() {
		if (isGUSEInitialized()) {
			try (final Connection connection = DriverManager.getConnection(
					PropertyLoader.getInstance().getProperty(GUSE_KEY_DATABASE_URL),
					PropertyLoader.getInstance().getProperty(GUSE_KEY_DATABASE_USER),
					PropertyLoader.getInstance().getProperty(GUSE_KEY_DATABASE_PASSWORD))) {
				try (final PreparedStatement statement = connection.prepareStatement("SELECT * FROM WORKFLOW")) {
					final ResultSet resultSet = statement.executeQuery();
					while (resultSet.next()) {
						LOG.debug("id=" + resultSet.getInt("id") + ", name=" + resultSet.getString("name"));
					}
				} catch (SQLException e) {
					throw new RuntimeException("There was a problem executing a query on the database", e);
				}
			} catch (SQLException e) {
				throw new RuntimeException("There was a problem stablishing the connection with the database", e);
			}
		}
	}
}
