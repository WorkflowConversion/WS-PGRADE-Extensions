package com.workflowconversion.portlet.appmanager;

import java.util.Map;
import java.util.TreeMap;

import org.jsoup.helper.Validate;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.workflowconversion.portlet.core.exception.ProviderNotEditableException;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.core.resource.ResourceProvider;
import com.workflowconversion.portlet.core.settings.Settings;
import com.workflowconversion.portlet.ui.HorizontalSeparator;
import com.workflowconversion.portlet.ui.NotificationUtils;
import com.workflowconversion.portlet.ui.WorkflowConversionUI;
import com.workflowconversion.portlet.ui.table.TableWithControls;
import com.workflowconversion.portlet.ui.table.resource.ResourcesTable;
import com.workflowconversion.portlet.ui.table.resource.ResourcesTable.ResourceTableFactory;
import com.workflowconversion.portlet.ui.upload.resource.BulkUploadResourcesDialog;

/**
 * Entry point for this portlet.
 * 
 * @author delagarza
 *
 */
public class ApplicationManagerUI extends WorkflowConversionUI {

	private static final long serialVersionUID = 2714264868836832769L;

	private static final String PROPERTY_NAME_CAPTION = "ApplicationManagerUI_property_caption";
	private static final String PROPERTY_NAME_ICON = "ApplicationManagerUI_property_icon";

	/**
	 * Constructor.
	 */
	public ApplicationManagerUI() {
		super(Settings.getInstance().getPortletSanityCheck(), Settings.getInstance().getApplicationProviders());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Layout prepareContent() {
		final Layout resourceTableLayout = new VerticalLayout();
		final ComboBox resourceProviderComboBox = getResourceProviderComboBox();
		final CheckBox enableEditionCheckBox = new CheckBox("Enable edition");
		enableEditionCheckBox.setImmediate(true);
		enableEditionCheckBox.setWidth(650, Unit.PIXELS);
		final Button saveButton = new Button("Save changes");
		saveButton.setImmediate(true);
		saveButton.setEnabled(false);
		saveButton.setDisableOnClick(true);
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
			newItem.getItemProperty(PROPERTY_NAME_CAPTION)
					.setValue(resourceProvider.getName() + (resourceProvider.isEditable() ? "" : " (read-only)"));
			newItem.getItemProperty(PROPERTY_NAME_ICON)
					.setValue(resourceProvider.isEditable() ? new ThemeResource("../runo/icons/16/settings.png")
							: new ThemeResource("../runo/icons/16/lock.png"));
		}

		resourceProviderComboBox.addValueChangeListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 7819796474153825187L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				// make sure we have a valid selection
				if (event != null && event.getProperty() != null && event.getProperty().getValue() != null) {
					final int selectedResourceProviderId = (Integer) event.getProperty().getValue();
					final UIComponents uiComponents = uiComponentsMap.get(selectedResourceProviderId);

					enableEditionCheckBox.setValue(uiComponents.isEditionEnabled);
					enableEditionCheckBox.setEnabled(uiComponents.resourceProvider.isEditable());
					saveButton
							.setEnabled(uiComponents.resourceProvider.isEditable() && enableEditionCheckBox.getValue());
					bulkUploadButton
							.setEnabled(uiComponents.resourceProvider.isEditable() && enableEditionCheckBox.getValue());

					resourceTableLayout.removeAllComponents();

					resourceTableLayout.addComponent(uiComponents.resourceTable);

					// make sure that the checkbox and the state of the tables is consistent
					propagateReadOnlyStatus(uiComponents, uiComponents.isEditionEnabled);
					resourceTableLayout.markAsDirtyRecursive();
				}
			}
		});

		enableEditionCheckBox.addValueChangeListener(new CheckBox.ValueChangeListener() {
			private static final long serialVersionUID = 1834213227006809779L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				final int selectedResourceProviderId = ((Integer) resourceProviderComboBox.getValue());
				final UIComponents uiComponents = uiComponentsMap.get(selectedResourceProviderId);
				final boolean editable = Boolean.parseBoolean(event.getProperty().getValue().toString());

				saveButton.setEnabled(editable);
				bulkUploadButton.setEnabled(editable);
				propagateReadOnlyStatus(uiComponents, editable);

				uiComponents.isEditionEnabled = editable;
			}
		});

		saveButton.addClickListener(new Button.ClickListener() {
			private static final long serialVersionUID = -4706192615658615512L;

			@Override
			public void buttonClick(final ClickEvent event) {
				try {
					final int selectedResourceProviderId = ((Integer) resourceProviderComboBox.getValue());
					final UIComponents uiComponents = uiComponentsMap.get(selectedResourceProviderId);
					uiComponents.resourceTable.clearSelection();
					uiComponents.resourceTable.saveAllChanges();
					uiComponents.resourceProvider.commitChanges();
				} finally {
					saveButton.setEnabled(true);
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

		final HorizontalLayout editControlsLayout = new HorizontalLayout();
		editControlsLayout.setMargin(false);
		editControlsLayout.setSpacing(true);
		editControlsLayout.addComponent(enableEditionCheckBox);
		editControlsLayout.addComponent(saveButton);
		editControlsLayout.addComponent(bulkUploadButton);
		editControlsLayout.setWidth(ResourcesTable.WIDTH_PIXELS, Unit.PIXELS);

		final VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.addComponent(resourceProviderComboBox);
		mainLayout.addComponent(new HorizontalSeparator());
		mainLayout.addComponent(resourceTableLayout);
		mainLayout.addComponent(new HorizontalSeparator());
		mainLayout.addComponent(editControlsLayout);
		mainLayout.setMargin(true);

		// select the first provider
		resourceProviderComboBox.select(0);

		return mainLayout;
	}

	private void bulkUploadButtonClicked(final UIComponents uiComponents) {
		if (!uiComponents.resourceProvider.isEditable()) {
			throw new ProviderNotEditableException(
					"This resource provider is not editable. This seems to be a coding problem and should be reported.");
		}

		final Window bulkUploadDialog = new BulkUploadResourcesDialog(uiComponents.resourceTable,
				Settings.getInstance().getMiddlewareProvider());

		if (bulkUploadDialog.getParent() != null) {
			NotificationUtils.displayWarning(
					"The 'Bulk Upload' dialog is already open. Please complete the upload and close the dialog.");
		}
		UI.getCurrent().addWindow(bulkUploadDialog);
	}

	private void propagateReadOnlyStatus(final UIComponents uiComponents, final boolean enableEdition) {
		uiComponents.resourceTable.setReadOnly(!enableEdition);
	}

	private UIComponents buildUiComponentsForResourceProvider(final ResourceProvider resourceProvider) {
		final ResourceTableFactory factory = new ResourceTableFactory();
		factory.withResourceProvider(resourceProvider)
				.withMiddlewareTypes(Settings.getInstance().getMiddlewareProvider().getAllMiddlewareTypes())
				.withDetails(true).withTitle("Resources").allowEdition(resourceProvider.isEditable())
				.allowDuplicates(false);
		final TableWithControls<Resource> resourceTable = factory.build();
		resourceTable.init(resourceProvider.getResources());
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
		resourceProviderComboBox.setWidth(ResourcesTable.WIDTH_PIXELS, Unit.PIXELS);

		resourceProviderComboBox.addContainerProperty(PROPERTY_NAME_CAPTION, String.class, null);
		resourceProviderComboBox.addContainerProperty(PROPERTY_NAME_ICON, com.vaadin.server.Resource.class, null);
		resourceProviderComboBox.setItemCaptionPropertyId(PROPERTY_NAME_CAPTION);
		resourceProviderComboBox.setItemIconPropertyId(PROPERTY_NAME_ICON);

		return resourceProviderComboBox;
	}

	// simple struct-like class
	private static class UIComponents {
		private final ResourceProvider resourceProvider;
		private final TableWithControls<Resource> resourceTable;
		private volatile boolean isEditionEnabled;

		UIComponents(final ResourceProvider resourceProvider, final TableWithControls<Resource> resourceTable) {
			Validate.notNull(resourceProvider, "resourceProvider cannot be null");
			Validate.notNull(resourceTable, "resourceTable cannot be null");
			this.resourceProvider = resourceProvider;
			this.resourceTable = resourceTable;
			this.isEditionEnabled = false;
		}
	}
}
