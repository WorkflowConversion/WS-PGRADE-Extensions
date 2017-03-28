package com.workflowconversion.portlet.ui.table.application;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.vaadin.data.Item;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.core.resource.ResourceProvider;
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

	private static final long serialVersionUID = -5169354278787921392L;

	private final Resource owningResource;
	private final Application.Field[] visibleColumns;

	private ApplicationsTable(final Resource owningResource, final Application.Field[] visibleColumns,
			final String title, final boolean allowEdition, final boolean withDetails, final boolean allowDuplicates,
			final boolean allowMultipleSelection) {
		super(title, allowEdition, withDetails, allowDuplicates, allowMultipleSelection);
		Validate.notNull(owningResource, "owningResource cannot be null");
		Validate.notEmpty(visibleColumns, "visibleColumns cannot be null or empty");
		this.owningResource = owningResource;
		this.visibleColumns = Arrays.copyOf(visibleColumns, visibleColumns.length);
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
	protected Object[] getVisibleColumns() {
		return visibleColumns;
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
	}

	@Override
	protected void validate(final Application application) {
		Validate.notNull(application, "application cannot be null, this is quite likely a bug and should be reported");
		Validate.isTrue(StringUtils.isNotBlank(application.getName()),
				"application name cannot be empty, null or contain only whitespace elements");
		Validate.isTrue(StringUtils.isNotBlank(application.getVersion()),
				"version cannot be empty, null or contain only whitespace elements");
		Validate.isTrue(StringUtils.isNotBlank(application.getPath()),
				"application path cannot be empty, null or contain only whitespace elements");
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
		final Application application = new Application();
		application.setName(((TextField) item.getItemProperty(Application.Field.Name).getValue()).getValue());
		application.setVersion(((TextField) item.getItemProperty(Application.Field.Version).getValue()).getValue());
		application
				.setDescription(((TextArea) item.getItemProperty(Application.Field.Description).getValue()).getValue());
		application.setPath(((TextField) item.getItemProperty(Application.Field.Path).getValue()).getValue());
		return application;
	}

	/**
	 * Factory for application tables.
	 * 
	 * @author delagarza
	 *
	 */
	public static class ApplicationsTableFactory extends AbstractTableWithControlsFactory<Application> {
		private Resource owningResource;
		private Application.Field[] visibleColumns;

		@Override
		public TableWithControls<Application> newInstance() {
			return new ApplicationsTable(owningResource, visibleColumns, super.title, super.allowEdition,
					super.withDetails, super.allowDuplicates, super.allowMultipleSelection);
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

		/**
		 * Sets the visible columns.
		 * 
		 * @param visibleColumns
		 *            the visible columns.
		 * @return {@code this} factory.
		 */
		public ApplicationsTableFactory withVisibleColumns(final Application.Field[] visibleColumns) {
			this.visibleColumns = visibleColumns;
			return this;
		}
	}
}
