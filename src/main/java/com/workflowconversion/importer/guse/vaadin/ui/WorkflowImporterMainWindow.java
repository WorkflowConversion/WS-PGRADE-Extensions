package com.workflowconversion.importer.guse.vaadin.ui;

import com.vaadin.ui.Panel;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Window;
import com.workflowconversion.importer.guse.exception.UserNotAuthenticatedException;
import com.workflowconversion.importer.guse.permission.PermissionManager;
import com.workflowconversion.importer.guse.user.PortletUser;

/**
 * This class encapsulates the main window of the portlet.
 * 
 * @author delagarza
 *
 */
public class WorkflowImporterMainWindow extends Window {

	private static final long serialVersionUID = -946393776684310488L;
	private static final String CAPTION = "Workflow Importer Portlet";

	private final PermissionManager permissionManager;
	private final PortletUser user;

	/**
	 * Constructor.
	 * 
	 * @param user
	 *            The user requesting the UI to be rendered.
	 * @param permissionManager
	 *            The permission manager.
	 * @param theme
	 *            The Vaadin theme to use.
	 * 
	 * @throws UserNotAuthenticatedException
	 *             if the passed user is not authenticated.
	 */
	public WorkflowImporterMainWindow(final PortletUser user, final PermissionManager permissionManager,
			final String theme) {
		super(CAPTION);
		if (!user.isAuthenticated()) {
			throw new UserNotAuthenticatedException(user);
		}
		super.setTheme(theme);
		this.user = user;
		this.permissionManager = permissionManager;
		initComponents();
	}

	private void initComponents() {
		if (permissionManager.hasReadAccess(user) || permissionManager.hasWriteAccess(user)) {
			final TabSheet tabSheet = new TabSheet();
			tabSheet.setCaption("Workflow Importer Portlet");
			addEndUserContent(tabSheet);
			addAdminContent(tabSheet);
			setContent(tabSheet);
		} else {
			// well, this is awkward... somehow an authenticated user without read/write access made it here...
			// display a "content not available" window
			setContent(new SimpleWarningWindow.Builder().setIconLocation("../runo/icons/64/attention.png")
					.setLongDescription("You are authenticated but lack the permissions to access this content. "
							+ "Please contact your portal administrator.")
					.newWarningWindow().getContent());
		}
	}

	// adds content that every user can access
	private void addEndUserContent(final TabSheet tabSheet) {
		if (permissionManager.hasReadAccess(user)) {
			final Panel a = new Panel("Readable Content");
			tabSheet.addTab(a, "First tab");
		}
	}

	// adds admin-only content
	private void addAdminContent(final TabSheet tabSheet) {
		if (permissionManager.hasWriteAccess(user)) {
			final Panel a = new Panel("Editable Content");
			tabSheet.addTab(a, "Second tab");
		}
	}
}
