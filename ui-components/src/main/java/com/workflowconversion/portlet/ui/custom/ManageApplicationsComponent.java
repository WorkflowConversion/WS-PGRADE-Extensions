package com.workflowconversion.portlet.ui.custom;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.Validate;

import com.vaadin.terminal.Resource;
import com.vaadin.terminal.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;
import com.workflowconversion.portlet.core.app.ApplicationProvider;
import com.workflowconversion.portlet.core.middleware.MiddlewareProvider;
import com.workflowconversion.portlet.ui.custom.apptable.ApplicationsViewComponent;

/**
 * View to manage applications.
 * 
 * Displays a split-panel. On the left side the user will see the list of application providers and on the right side a
 * table will the applications in the selected provider.
 * 
 * Client code is responsible of displaying this component only to users with the required permissions.
 * 
 * @author delagarza
 *
 */
public class ManageApplicationsComponent extends CustomComponent {
	private static final long serialVersionUID = -7570455814597876485L;

	private final HorizontalSplitPanel splitPanel;
	private final Map<Integer, ApplicationsViewComponent> tableMap;
	private final Collection<ApplicationProvider> applicationProviders;
	private final MiddlewareProvider middlewareProvider;

	/**
	 * Constructor.
	 */
	public ManageApplicationsComponent(final Collection<ApplicationProvider> applicationProviders,
			final MiddlewareProvider middlewareProvider) {
		Validate.notEmpty(applicationProviders, "applicationProviders cannot be null or empty");
		Validate.notNull(middlewareProvider, "middlewareProvider cannot be null");
		this.splitPanel = new HorizontalSplitPanel();
		this.tableMap = new TreeMap<Integer, ApplicationsViewComponent>();
		this.applicationProviders = applicationProviders;
		this.middlewareProvider = middlewareProvider;
		initSplitPanel();
		initUI();
	}

	private void initSplitPanel() {
		initLeftSide();
		initRightSide();
		splitPanel.setImmediate(true);
		splitPanel.setWidth(100, UNITS_PERCENTAGE);
		splitPanel.setHeight(600, UNITS_PIXELS);
		splitPanel.setSplitPosition(20);
	}

	// add a button and some click listeners for each of the system application providers
	private void initLeftSide() {
		final Layout layout = new VerticalLayout();
		int applicationProviderId = 0;
		for (final ApplicationProvider applicationProvider : applicationProviders) {
			final Button button = new Button(applicationProvider.getName());
			button.setStyleName(BaseTheme.BUTTON_LINK);
			final Resource icon;
			final String toolTip;
			if (applicationProvider.isEditable()) {
				icon = new ThemeResource("../runo/icons/32/settings.png");
				toolTip = "Edit " + applicationProvider.getName();
			} else {
				icon = new ThemeResource("../runo/icons/32/lock.png");
				toolTip = applicationProvider.getName() + " is a read-only database";
			}
			button.setDescription(toolTip);
			button.setIcon(icon);
			button.setData(applicationProviderId);
			button.addListener(new Button.ClickListener() {
				private static final long serialVersionUID = 1500801734383287175L;

				@Override
				public void buttonClick(final ClickEvent event) {
					applicationProviderSelected((Integer) event.getButton().getData());
				}
			});
			// make it a big tall and make sure all of the content fits
			button.setHeight(5, UNITS_EM);
			button.setWidth(-1, UNITS_PIXELS);
			final ApplicationsViewComponent applicationsView = new ApplicationsViewComponent(applicationProvider,
					middlewareProvider, applicationProvider.isEditable());
			tableMap.put(applicationProviderId, applicationsView);
			layout.addComponent(button);
			applicationProviderId++;
		}
		layout.setSizeUndefined();
		splitPanel.setFirstComponent(layout);
	}

	private void applicationProviderSelected(final int applicationProviderId) {
		final ApplicationsViewComponent component = tableMap.get(applicationProviderId);
		final VerticalLayout targetLayout = (VerticalLayout) splitPanel.getSecondComponent();
		targetLayout.removeAllComponents();
		targetLayout.addComponent(component);
		targetLayout.requestRepaintAll();
	}

	private void initRightSide() {
		final VerticalLayout layout = new VerticalLayout();
		splitPanel.setSecondComponent(layout);
	}

	private void initUI() {
		final Layout layout = new VerticalLayout();
		layout.addComponent(this.splitPanel);
		setCompositionRoot(layout);
	}

}
