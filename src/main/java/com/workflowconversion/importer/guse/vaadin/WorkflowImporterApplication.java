package com.workflowconversion.importer.guse.vaadin;

import java.io.File;
import java.util.Hashtable;
import java.util.Vector;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.util.PortalUtil;
import com.vaadin.Application;
import com.vaadin.terminal.gwt.server.PortletRequestListener;
import com.vaadin.ui.Window;
import com.workflowconversion.importer.guse.config.PortletConfiguration;
import com.workflowconversion.importer.guse.exception.ApplicationException;
import com.workflowconversion.importer.guse.user.PortletUser;
import com.workflowconversion.importer.guse.vaadin.ui.SimpleWarningWindow;
import com.workflowconversion.importer.guse.vaadin.ui.WorkflowImporterMainWindow;

import hu.sztaki.lpds.information.com.ServiceType;
import hu.sztaki.lpds.information.local.InformationBase;
import hu.sztaki.lpds.information.local.PropertyLoader;
import hu.sztaki.lpds.storage.inf.PortalStorageClient;

public class WorkflowImporterApplication extends Application implements PortletRequestListener {

	private static final long serialVersionUID = -1691439006632825854L;
	// a simple key for logging MDC
	private static final String MDC_REFERENCE_KEY = "reference";

	protected final static Logger LOG = LoggerFactory.getLogger(WorkflowImporterApplication.class);

	private PortletUser currentUser;

	@Override
	public void init() {
		LOG.info("initializing WorkflowImporterApplication");

		final Window mainWindow;

		if (currentUser.isAuthenticated()) {
			if (PortletConfiguration.getInstance().isValid()) {
				// happy path!
				mainWindow = new WorkflowImporterMainWindow(currentUser);
			} else {
				mainWindow = new SimpleWarningWindow.Builder().setIconLocation("../runo/icons/64/lock.png")
						.setLongDescription(
								"This portlet has not been properly initialized. You probably need to restart gUSE.")
						.newWarningWindow();
			}
		} else {
			mainWindow = new SimpleWarningWindow.Builder().setIconLocation("../runo/icons/64/attention.png")
					.setLongDescription("You need to be logged-in to access this portlet.").newWarningWindow();
		}
		setMainWindow(mainWindow);
	}

	private PortletUser extractCurrentUser(final PortletRequest request) {
		try {
			return new PortletUser(PortalUtil.getUser(request));
		} catch (SystemException | PortalException e) {
			// there isn't much we can do, really
			throw new ApplicationException("Could not extract current user from the PortletRequest.", e);
		}
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
			// h.put("userID", currentRequest.getRemoteUser());

			Hashtable<String, Object> hsh = new Hashtable<String, Object>();
			st = InformationBase.getI().getService("storage", "portal", hsh, new Vector<Object>());
			PortalStorageClient psc = (PortalStorageClient) Class.forName(st.getClientObject()).newInstance();
			psc.setServiceURL(st.getServiceUrl());
			psc.setServiceID("/receiver");
			psc.fileUpload(serverSideFile, "fileName", h);
			LOG.info("workflow has been imported");
		} catch (Exception e) {
			// mainWindow.showNotification(new Notification("Workflow Importer",
			// "Could not import workflow." + e.getMessage(), Notification.TYPE_ERROR_MESSAGE));
			LOG.error("Error while importing workflow", e);
		}
	}

	/**
	 * As defined in {@link PortletRequestListener#onRequestStart(PortletRequest, PortletResponse)}.
	 */
	public void onRequestStart(final PortletRequest request, final PortletResponse response) {
		MDC.put(MDC_REFERENCE_KEY, Integer.toHexString(System.identityHashCode(this)));
		currentUser = extractCurrentUser(request);
	}

	/**
	 * As defined in {@link PortletRequestListener#onRequestEnd(PortletRequest, PortletResponse)}.
	 */
	public void onRequestEnd(final PortletRequest request, final PortletResponse response) {
		MDC.remove(MDC_REFERENCE_KEY);
	}
}
