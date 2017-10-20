package com.workflowconversion.portlet.ui.table.application;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.ui.Label;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.core.resource.ResourceProvider;
import com.workflowconversion.portlet.core.utils.KeyUtils;
import com.workflowconversion.portlet.ui.table.AbstractAddGenericElementDialog;
import com.workflowconversion.portlet.ui.table.AbstractTableWithControls;
import com.workflowconversion.portlet.ui.table.AbstractTableWithControlsFactory;
import com.workflowconversion.portlet.ui.table.Size;
import com.workflowconversion.portlet.ui.table.TableWithControls;

/**
 * Component containing the applications to be displayed in a table, plus controls to add/save application if set on
 * editable mode.
 * 
 * Clients are responsible of validating that instances of this class based on a read-only {@link ResourceProvider} are
 * not editable.
 */
public class ApplicationsTable extends AbstractTableWithControls<Application> {

	private final static long serialVersionUID = -5169354278787921392L;

	private final Resource owningResource;

	private ApplicationsTable(final Resource owningResource, final String title, final boolean allowEdition) {
		super(title, allowEdition, false);
		Validate.notNull(owningResource, "owningResource cannot be null");
		this.owningResource = owningResource;
	}

	@Override
	public Size getSize() {
		final Size tableDimensions = new Size();
		tableDimensions.width = 1100;
		tableDimensions.widthUnit = Unit.PIXELS;
		tableDimensions.height = 450;
		tableDimensions.heightUnit = Unit.PIXELS;
		return tableDimensions;
	}

	@Override
	protected String getKeyForItem(final Item item) {
		final String name;
		final String version;
		final String path;

		if (allowEdition) {
			name = ((TextField) (item.getItemProperty(Application.Field.Name).getValue())).getValue();
			version = ((TextField) (item.getItemProperty(Application.Field.Version).getValue())).getValue();
			path = ((TextField) (item.getItemProperty(Application.Field.Path).getValue())).getValue();
		} else {
			name = ((Label) (item.getItemProperty(Application.Field.Name).getValue())).getValue();
			version = ((Label) (item.getItemProperty(Application.Field.Version).getValue())).getValue();
			path = ((Label) (item.getItemProperty(Application.Field.Path).getValue())).getValue();
		}

		return KeyUtils.generateApplicationKey(name, version, path);
	}

	@Override
	protected Object[] getVisibleColumns() {
		return new Object[] { Application.Field.Name, Application.Field.Version, Application.Field.Path,
				Application.Field.Description };
	}

	@Override
	protected AbstractAddGenericElementDialog<Application> createAddElementDialog() {
		return new AddApplicationDialog(this);
	}

	@Override
	protected void setUpContainerProperties() {
		if (allowEdition) {
			addContainerProperty(Application.Field.Name, TextField.class);
			addContainerProperty(Application.Field.Version, TextField.class);
			addContainerProperty(Application.Field.Path, TextField.class);
			addContainerProperty(Application.Field.Description, TextArea.class);
		} else {
			addContainerProperty(Application.Field.Name, Label.class);
			addContainerProperty(Application.Field.Version, Label.class);
			addContainerProperty(Application.Field.Path, Label.class);
			addContainerProperty(Application.Field.Description, Label.class);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void fillItemProperties(final Application application, final Item item) {
		if (allowEdition) {
			item.getItemProperty(Application.Field.Name)
					.setValue(newTextFieldWithValue(StringUtils.trimToEmpty(application.getName())));
			item.getItemProperty(Application.Field.Version)
					.setValue(newTextFieldWithValue(StringUtils.trimToEmpty(application.getVersion())));
			item.getItemProperty(Application.Field.Description)
					.setValue(newTextAreaWithValue(StringUtils.trimToEmpty(application.getDescription())));
			item.getItemProperty(Application.Field.Path)
					.setValue(newTextFieldWithValue(StringUtils.trimToEmpty(application.getPath())));
		} else {
			item.getItemProperty(Application.Field.Name)
					.setValue(newLabelWithValue(StringUtils.trimToEmpty(application.getName())));
			item.getItemProperty(Application.Field.Version)
					.setValue(newLabelWithValue(StringUtils.trimToEmpty(application.getVersion())));
			item.getItemProperty(Application.Field.Description)
					.setValue(newLabelWithValue(StringUtils.trimToEmpty(application.getDescription())));
			item.getItemProperty(Application.Field.Path)
					.setValue(newLabelWithValue(StringUtils.trimToEmpty(application.getPath())));
		}
	}

	@Override
	protected void beforeSaveAllChanges() {
		owningResource.removeAllApplications();
	}

	@Override
	protected void save(final Application application) {
		if (owningResource.getApplication(application.getName(), application.getVersion(),
				application.getPath()) == null) {
			// new application
			owningResource.addApplication(application);
		} else {
			owningResource.saveApplication(application);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Application convertFromItem(final Item item) {
		final Application.Builder applicationBuilder = new Application.Builder();
		applicationBuilder
				.withName(((Property<String>) item.getItemProperty(Application.Field.Name).getValue()).getValue());
		applicationBuilder.withVersion(
				((Property<String>) item.getItemProperty(Application.Field.Version).getValue()).getValue());
		applicationBuilder
				.withPath(((Property<String>) item.getItemProperty(Application.Field.Path).getValue()).getValue());
		applicationBuilder.withDescription(
				((Property<String>) item.getItemProperty(Application.Field.Description).getValue()).getValue());
		return applicationBuilder.newInstance();
	}

	/**
	 * Factory for application tables.
	 * 
	 * @author delagarza
	 *
	 */
	public static class ApplicationsTableFactory extends AbstractTableWithControlsFactory<Application> {
		private boolean allowEdition;
		private Resource owningResource;

		/**
		 * @param allowEdition
		 *            whether adding/editing applications is allowed.
		 * @return {@code this} factory.
		 */
		public ApplicationsTableFactory allowEdition(final boolean allowEdition) {
			this.allowEdition = allowEdition;
			return this;
		}

		/**
		 * Sets the owning resource.
		 * 
		 * @param owningResource
		 *            the owning resource.
		 * @return {@code this} factory.
		 */
		public ApplicationsTableFactory withOwningResource(final Resource owningResource) {
			this.owningResource = owningResource;
			return this;
		}

		@Override
		public TableWithControls<Application> newInstance() {
			return new ApplicationsTable(owningResource, super.title, allowEdition);
		}
	}
}
