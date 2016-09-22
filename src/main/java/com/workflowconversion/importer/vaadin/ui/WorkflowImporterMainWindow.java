package com.workflowconversion.importer.vaadin.ui;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Layout;
import com.vaadin.ui.MenuBar;
import com.vaadin.ui.MenuBar.Command;
import com.vaadin.ui.MenuBar.MenuItem;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.workflowconversion.importer.exception.ApplicationException;
import com.workflowconversion.importer.exception.UserNotAuthenticatedException;
import com.workflowconversion.importer.guse.permission.PermissionManager;
import com.workflowconversion.importer.user.PortletUser;
import com.workflowconversion.importer.vaadin.ui.custom.AboutComponent;
import com.workflowconversion.importer.vaadin.ui.custom.BulkImportApplicationsComponent;
import com.workflowconversion.importer.vaadin.ui.custom.HelpComponent;
import com.workflowconversion.importer.vaadin.ui.custom.ImportWorkflowComponent;
import com.workflowconversion.importer.vaadin.ui.custom.ManageApplicationsComponent;
import com.workflowconversion.importer.vaadin.ui.custom.ManageWorkflowsComponent;

/**
 * This class encapsulates the main window of the portlet. It contains a menu and a component on which different
 * components will be displayed.
 * 
 * @author delagarza
 *
 */
public class WorkflowImporterMainWindow extends Window {

	private static final long serialVersionUID = -946393776684310488L;

	private final PermissionManager permissionManager;
	private final PortletUser user;
	private final MenuBar menuBar;
	// keep track of all of the custom components that the menus would display
	final Map<NavigationTarget, CustomComponent> navigationMap = new TreeMap<NavigationTarget, CustomComponent>();
	// component on which our custom components will be displayed
	final VerticalLayout displayedComponent;

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
		super("Workflow Importer Portlet");
		if (!user.isAuthenticated()) {
			throw new UserNotAuthenticatedException(user);
		}
		setTheme(theme);
		this.user = user;
		this.permissionManager = permissionManager;
		this.menuBar = new MenuBar();
		this.displayedComponent = new VerticalLayout();

		initUI();

		// initialize what is needed
		if (hasAccess()) {
			navigationMap.put(NavigationTarget.ManageWorkflows, new ManageWorkflowsComponent());
			navigationMap.put(NavigationTarget.ImportWorkflow, new ImportWorkflowComponent());
			navigationMap.put(NavigationTarget.About, new AboutComponent());
			navigationMap.put(NavigationTarget.Help, new HelpComponent());
		}
		if (permissionManager.hasWriteAccess(user)) {
			navigationMap.put(NavigationTarget.BulkImportApplications, new BulkImportApplicationsComponent());
			navigationMap.put(NavigationTarget.ManageApplications, new ManageApplicationsComponent());
		}
	}

	private void initUI() {
		setWidth(100, UNITS_PERCENTAGE);
		setHeight(600, UNITS_PIXELS);
		displayedComponent.setSizeFull();

		if (hasAccess()) {
			initMenu();
			// add the menu, a separator and a content panel
			final Layout layout = new VerticalLayout();
			layout.addComponent(menuBar);
			layout.addComponent(new HorizontalSeparator());
			layout.addComponent(displayedComponent);
			setContent(layout);
		} else {
			// well, this is awkward... somehow an authenticated user without read/write access made it here...
			// display a "content not available" window
			setContent(new SimpleWarningWindow.Builder().setIconLocation("../runo/icons/64/attention.png")
					.setLongDescription("You are authenticated but lack the permissions to access this content. "
							+ "Please contact your portal administrator.")
					.newWarningWindow().getContent());
		}
	}

	private boolean hasAccess() {
		return permissionManager.hasReadAccess(user) || permissionManager.hasWriteAccess(user);
	}

	private void initMenu() {
		final MenuItem workflowItem = menuBar.addItem("Workflow", null);
		workflowItem.addItem("Import Workflow", new MenuCommand(NavigationTarget.ImportWorkflow));
		workflowItem.addItem("Manage my imported Workflows", new MenuCommand(NavigationTarget.ManageWorkflows));

		final MenuItem applicationDatabaseItem = menuBar.addItem("Application Database", null);
		final MenuItem importApplicationsItem = applicationDatabaseItem.addItem("Bulk Import Applications",
				new MenuCommand(NavigationTarget.BulkImportApplications));
		final MenuItem manageApplicationsItem = applicationDatabaseItem.addItem("Manage Applications",
				new MenuCommand(NavigationTarget.ManageApplications));

		final MenuItem helpItem = menuBar.addItem("Help", null);
		helpItem.addItem("User's Guide", new MenuCommand(NavigationTarget.Help));
		helpItem.addItem("About", new MenuCommand(NavigationTarget.About));

		// init permissions: by default all menu items are enabled, so make sure we disable these for users without
		// privileges
		if (!permissionManager.hasWriteAccess(user)) {
			importApplicationsItem.setEnabled(false);
			manageApplicationsItem.setEnabled(false);
		}
	}

	private class MenuCommand implements Command {
		private static final long serialVersionUID = 5357106266790214204L;

		// destination of this command
		private final NavigationTarget target;

		private MenuCommand(final NavigationTarget target) {
			Validate.notNull(target, "target cannot be null; this is a problem in the code and should be reported.");
			this.target = target;
		}

		@Override
		public void menuSelected(final MenuItem selectedItem) {
			final CustomComponent componentToDisplay = navigationMap.get(target);
			if (componentToDisplay == null) {
				throw new ApplicationException("Navigation target: " + target.name()
						+ " has not been registered. This seems to be a bug in the code and should be reported.");
			}
			displayedComponent.removeAllComponents();
			displayedComponent.addComponent(componentToDisplay);
			displayedComponent.requestRepaintAll();
		}

	}
}
