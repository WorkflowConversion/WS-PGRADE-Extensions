package com.workflowconversion.portlet.appdb;

import java.util.Map;
import java.util.TreeMap;

import org.jsoup.helper.Validate;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.workflowconversion.portlet.core.Settings;
import com.workflowconversion.portlet.core.exception.ApplicationException;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.core.resource.ResourceProvider;
import com.workflowconversion.portlet.ui.HorizontalSeparator;
import com.workflowconversion.portlet.ui.NotificationUtils;
import com.workflowconversion.portlet.ui.WorkflowConversionUI;
import com.workflowconversion.portlet.ui.resource.upload.BulkUploadApplicationsDialog;
import com.workflowconversion.portlet.ui.table.TableWithControls;
import com.workflowconversion.portlet.ui.table.resource.ResourceTable;
import com.workflowconversion.portlet.ui.table.resource.ResourceTable.ResourceTableFactory;

/**
 * Entry point for this portlet.
 * 
 * @author delagarza
 *
 */
public class ApplicationDatabasePortlet extends WorkflowConversionUI {

	private static final long serialVersionUID = 2714264868836832769L;

	private static final String PROPERTY_NAME_CAPTION = "ApplicationManagerUI_property_caption";
	private static final String PROPERTY_NAME_ICON = "ApplicationManagerUI_property_icon";

	/**
	 * Constructor.
	 */
	public ApplicationDatabasePortlet() {
		super(Settings.getInstance().getPortletSanityCheck(), Settings.getInstance().getResourceProviders());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Layout prepareContent() {
		final Layout resourceTableLayout = new VerticalLayout();
		final ComboBox resourceProviderComboBox = getResourceProviderComboBox();

		final Button bulkUploadButton = new Button("Bulk upload...");
		bulkUploadButton.setEnabled(false);
		bulkUploadButton.setImmediate(true);
		bulkUploadButton.setDisableOnClick(true);

		// key: resource provider id, value: components to display
		final Map<Integer, UIComponents> uiComponentsMap = new TreeMap<Integer, UIComponents>();

		int resourceProviderId = 0;
		for (final ResourceProvider resourceProvider : super.resourceProviders) {
			final UIComponents uiComponents = buildUiComponentsForResourceProvider(resourceProvider);
			uiComponentsMap.put(resourceProviderId, uiComponents);
			final Item newItem = resourceProviderComboBox.addItem(resourceProviderId++);
			newItem.getItemProperty(PROPERTY_NAME_CAPTION).setValue(resourceProvider.getName());
			newItem.getItemProperty(PROPERTY_NAME_ICON).setValue(new ThemeResource("../runo/icons/16/settings.png"));
		}

		resourceProviderComboBox.addValueChangeListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 7819796474153825187L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				// make sure we have a valid selection
				if (event != null && event.getProperty() != null && event.getProperty().getValue() != null) {
					final int selectedResourceProviderId = (Integer) event.getProperty().getValue();
					final UIComponents uiComponents = uiComponentsMap.get(selectedResourceProviderId);

					resourceTableLayout.removeAllComponents();

					if (uiComponents.resourceProvider.hasInitErrors()) {
						NotificationUtils.displayError("The resource provider named '"
								+ uiComponents.resourceProvider.getName()
								+ "' could not be initialized. Contact your admin or check the log for further details.");
						bulkUploadButton.setEnabled(false);
					} else {
						bulkUploadButton.setEnabled(uiComponents.resourceProvider.canAddApplications());
						resourceTableLayout.addComponent(uiComponents.resourceTable);
						// make sure that the checkbox and the state of the tables is consistent
						resourceTableLayout.markAsDirtyRecursive();
					}
				}
			}
		});

		bulkUploadButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = 7416359828419027737L;

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					bulkUploadButtonClicked(uiComponentsMap.get(resourceProviderComboBox.getValue()));
				} finally {
					bulkUploadButton.setEnabled(true);
				}
			}
		});

		final HorizontalLayout buttonsLayout = new HorizontalLayout();
		buttonsLayout.setMargin(false);
		buttonsLayout.setSpacing(true);
		buttonsLayout.addComponent(bulkUploadButton);

		final HorizontalLayout editControlsLayout = new HorizontalLayout();
		editControlsLayout.setMargin(false);
		editControlsLayout.setSpacing(true);
		editControlsLayout.addComponent(buttonsLayout);
		editControlsLayout.setComponentAlignment(buttonsLayout, Alignment.MIDDLE_RIGHT);

		editControlsLayout.setWidth(ResourceTable.WIDTH_PIXELS, Unit.PIXELS);

		final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.addComponent(resourceProviderComboBox);
		mainLayout.addComponent(new HorizontalSeparator());
		mainLayout.addComponent(resourceTableLayout);
		mainLayout.addComponent(new HorizontalSeparator());
		mainLayout.addComponent(editControlsLayout);

		// select the first provider
		resourceProviderComboBox.select(0);

		return mainLayout;
	}

	private void bulkUploadButtonClicked(final UIComponents uiComponents) {
		if (!uiComponents.resourceProvider.canAddApplications()) {
			throw new ApplicationException(
					"It is not posssible to add applications to this provider. This seems to be a coding problem and should be reported.");
		}

		final Window bulkUploadDialog = new BulkUploadApplicationsDialog(uiComponents.resourceProvider);

		if (bulkUploadDialog.getParent() != null) {
			NotificationUtils.displayWarning(
					"The 'Bulk Upload' dialog is already open. Please complete the upload and close the dialog.");
		}
		UI.getCurrent().addWindow(bulkUploadDialog);
	}

	private UIComponents buildUiComponentsForResourceProvider(final ResourceProvider resourceProvider) {
		final ResourceTableFactory factory = new ResourceTableFactory();
		// cannot add/remove resources, because the list of resources comes from a web service
		factory.withResourceProvider(resourceProvider).withTitle("Resources");
		final TableWithControls<Resource> resourceTable = factory.newInstance();
		if (!resourceProvider.hasInitErrors()) {
			resourceTable.setInitialItems(resourceProvider.getResources());
		}
		final UIComponents uiComponents = new UIComponents(resourceProvider, resourceTable);

		return uiComponents;
	}

	private ComboBox getResourceProviderComboBox() {
		final ComboBox resourceProviderComboBox = new ComboBox();
		resourceProviderComboBox.setCaptionAsHtml(true);
		resourceProviderComboBox.setCaption("<h3>Resource database</h3>");
		resourceProviderComboBox.setNullSelectionAllowed(false);
		resourceProviderComboBox.setImmediate(true);
		resourceProviderComboBox.setDescription("Select a resource database to manage");
		resourceProviderComboBox.setInputPrompt("Select a resource database to manage");
		resourceProviderComboBox.setWidth(ResourceTable.WIDTH_PIXELS, Unit.PIXELS);

		resourceProviderComboBox.addContainerProperty(PROPERTY_NAME_CAPTION, String.class, null);
		resourceProviderComboBox.addContainerProperty(PROPERTY_NAME_ICON, com.vaadin.server.Resource.class, null);
		resourceProviderComboBox.setItemCaptionPropertyId(PROPERTY_NAME_CAPTION);
		resourceProviderComboBox.setItemIconPropertyId(PROPERTY_NAME_ICON);

		return resourceProviderComboBox;
	}

	@Override
	protected void refresh(final VaadinRequest vaadinRequest) {
		super.refresh(vaadinRequest);

	}

	// simple struct-like class
	private static class UIComponents {
		private final ResourceProvider resourceProvider;
		private final TableWithControls<Resource> resourceTable;

		UIComponents(final ResourceProvider resourceProvider, final TableWithControls<Resource> resourceTable) {
			Validate.notNull(resourceProvider, "resourceProvider cannot be null");
			Validate.notNull(resourceTable, "resourceTable cannot be null");
			this.resourceProvider = resourceProvider;
			this.resourceTable = resourceTable;
		}
	}
}
