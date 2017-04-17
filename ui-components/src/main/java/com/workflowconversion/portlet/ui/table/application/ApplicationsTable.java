package com.workflowconversion.portlet.ui.table.application;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.vaadin.data.Item;
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
	private final static String PROPERTY_APPLICATION = "ApplicationsTable_property_application";

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
		final String name = ((TextField) (item.getItemProperty(Application.Field.Name).getValue())).getValue();
		final String version = ((TextField) (item.getItemProperty(Application.Field.Version).getValue())).getValue();
		final String path = ((TextField) (item.getItemProperty(Application.Field.Path).getValue())).getValue();
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
		addContainerProperty(Application.Field.Name, TextField.class);
		addContainerProperty(Application.Field.Version, TextField.class);
		addContainerProperty(Application.Field.Path, TextField.class);
		addContainerProperty(Application.Field.Description, TextArea.class);
		addContainerProperty(PROPERTY_APPLICATION);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void fillItemProperties(final Application application, final Item item) {
		item.getItemProperty(Application.Field.Name)
				.setValue(newTextFieldWithValue(StringUtils.trimToEmpty(application.getName())));
		item.getItemProperty(Application.Field.Version)
				.setValue(newTextFieldWithValue(StringUtils.trimToEmpty(application.getVersion())));
		item.getItemProperty(Application.Field.Description)
				.setValue(newTextAreaWithValue(StringUtils.trimToEmpty(application.getDescription())));
		item.getItemProperty(Application.Field.Path)
				.setValue(newTextFieldWithValue(StringUtils.trimToEmpty(application.getPath())));
		item.getItemProperty(PROPERTY_APPLICATION).setValue(application);
	}

	@Override
	protected void beforeSaveAllChanges() {
		owningResource.removeAllApplications();
	}

	@Override
	protected void save(final Application application) {
		owningResource.addApplication(application);
	}

	@Override
	protected Application convertFromItem(final Item item) {
		return (Application) item.getItemProperty(PROPERTY_APPLICATION).getValue();
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
