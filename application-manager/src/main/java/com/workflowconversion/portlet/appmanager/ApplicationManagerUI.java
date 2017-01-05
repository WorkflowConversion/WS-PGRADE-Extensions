package com.workflowconversion.portlet.appmanager;

import java.util.Map;
import java.util.TreeMap;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.Resource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;
import com.workflowconversion.portlet.core.resource.ResourceProvider;
import com.workflowconversion.portlet.core.settings.Settings;
import com.workflowconversion.portlet.ui.HorizontalSeparator;
import com.workflowconversion.portlet.ui.WorkflowConversionUI;
import com.workflowconversion.portlet.ui.table.application.ApplicationsTable;

/**
 * Entry point for this portlet.
 * 
 * @author delagarza
 *
 */
public class ApplicationManagerUI extends WorkflowConversionUI {

	private static final long serialVersionUID = 2714264868836832769L;

	private static final String PROPERTY_NAME_CAPTION = "caption";
	private static final String PROPERTY_NAME_ICON = "icon";

	private final Map<Integer, ApplicationsTable> tableMap;
	private final Layout applicationsTableDisplayPanel;

	/**
	 * Constructor.
	 */
	public ApplicationManagerUI() {
		super(Settings.getInstance().getPortletSanityCheck(), Settings.getInstance().getApplicationProviders());
		this.applicationsTableDisplayPanel = new VerticalLayout();
		this.tableMap = new TreeMap<Integer, ApplicationsTable>();
	}

	@Override
	protected Layout prepareContent() {
		final Layout layout = new VerticalLayout();

		layout.addComponent(getUIControls());
		layout.addComponent(new HorizontalSeparator());
		layout.addComponent(applicationsTableDisplayPanel);

		return layout;
	}

	private Layout getUIControls() {
		final Layout layout = new HorizontalLayout();
		layout.setWidth(100, Unit.PERCENTAGE);
		layout.setHeight(70, Unit.PERCENTAGE);

		layout.addComponent(getApplicationProviderComboBox());

		return layout;
	}

	private ComboBox getApplicationProviderComboBox() {
		final IndexedContainer container = new IndexedContainer();
		container.addContainerProperty(PROPERTY_NAME_CAPTION, String.class, null);
		container.addContainerProperty(PROPERTY_NAME_ICON, Resource.class, null);

		final ComboBox applicationProviderSelection = new ComboBox("Application database", container);
		applicationProviderSelection.setNullSelectionAllowed(false);
		applicationProviderSelection.setImmediate(true);
		applicationProviderSelection.setDescription("Select an application database to manage");
		applicationProviderSelection.setInputPrompt("Select an application database to manage");
		applicationProviderSelection.setWidth(30, Unit.EM);
		applicationProviderSelection.setItemCaptionPropertyId(PROPERTY_NAME_CAPTION);
		applicationProviderSelection.setItemIconPropertyId(PROPERTY_NAME_ICON);

		int applicationProviderId = 0;
		for (final ResourceProvider applicationProvider : super.applicationProviders) {
			final Item newItem = applicationProviderSelection.addItem(applicationProviderId);
			newItem.getItemProperty(PROPERTY_NAME_CAPTION)
					.setValue(applicationProvider.getName() + (applicationProvider.isEditable() ? "" : " (read-only)"));
			newItem.getItemProperty(PROPERTY_NAME_ICON)
					.setValue(applicationProvider.isEditable() ? new ThemeResource("../runo/icons/16/settings.png")
							: new ThemeResource("../runo/icons/16/lock.png"));
			final ApplicationsTableBuilder tableBuilder = new ApplicationsTableBuilder();
			// make the table editable depending on whether the applications provider is editable
			// and display all columns
			tableBuilder.withDisplayEditControls(applicationProvider.isEditable())
					.withApplicationProvider(applicationProvider)
					.withMiddlewareProvider(Settings.getInstance().getMiddlewareProvider())
					.withVisibleColumns(ApplicationField.Name, ApplicationField.Version, ApplicationField.Description,
							ApplicationField.Resource, ApplicationField.Path, ApplicationField.ResourceType);
			tableMap.put(applicationProviderId++, tableBuilder.newApplicationsTable());
		}

		applicationProviderSelection.addValueChangeListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 7819796474153825187L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				// make sure we have a valid selection
				if (event != null && event.getProperty() != null && event.getProperty().getValue() != null) {
					applicationProviderSelected(Integer.valueOf(event.getProperty().getValue().toString()));
				}
			}
		});

		// select the first provider
		applicationProviderSelection.select(0);
		return applicationProviderSelection;
	}

	private void applicationProviderSelected(final int applicationProviderId) {
		final ApplicationsTable component = tableMap.get(applicationProviderId);
		applicationsTableDisplayPanel.removeAllComponents();
		applicationsTableDisplayPanel.addComponent(component);
		applicationsTableDisplayPanel.markAsDirtyRecursive();
	}
}
