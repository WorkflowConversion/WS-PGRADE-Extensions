package com.workflowconversion.portlet.ui.table.application;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.vaadin.data.Item;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.core.resource.FormField;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.core.resource.ResourceProvider;
import com.workflowconversion.portlet.ui.table.AbstractAddGenericElementDialog;
import com.workflowconversion.portlet.ui.table.AbstractTableWithControls;
import com.workflowconversion.portlet.ui.table.Dimensions;
import com.workflowconversion.portlet.ui.table.GenericElementCommitedListener;

/**
 * Component containing the applications to be displayed in a table, plus controls to add/save application if set on
 * editable mode.
 * 
 * Clients are responsible of validating that instances of this class based on a read-only {@link ResourceProvider} are
 * not editable.
 */
public class ApplicationsTable extends AbstractTableWithControls<Application>
		implements GenericElementCommitedListener<Application> {

	private static final long serialVersionUID = -5169354278787921392L;

	private final Resource owningResource;
	private final Object[] visibleColumns;

	/**
	 * Builds a new instance.
	 * 
	 * @param owningResource
	 *            the resource to which the applications belong.
	 * @param withEditControls
	 *            whether the table with applications will be editable, regardless whether the passed application
	 *            provider is editable or not.
	 * @param visibleColumns
	 *            the name of the fields that will be visible on this table.
	 */
	ApplicationsTable(final Resource owningResource, final boolean withEditControls,
			final FormField... visibleColumns) {
		super("Applications", withEditControls, owningResource.getApplications());
		Validate.notEmpty(visibleColumns, "visibleColumns cannot be null or empty");
		this.owningResource = owningResource;
		this.visibleColumns = Arrays.copyOf(visibleColumns, visibleColumns.length);
	}

	@Override
	protected Dimensions getTableDimensions() {
		final Dimensions tableDimensions = new Dimensions();
		tableDimensions.width = 100;
		tableDimensions.widthUnit = Unit.PERCENTAGE;
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
	protected void setUpContainerPropertiesWithEditableFields() {
		addContainerProperty(Application.Field.Name, TextField.class);
		addContainerProperty(Application.Field.Version, TextField.class);
		addContainerProperty(Application.Field.Description, TextArea.class);
		addContainerProperty(Application.Field.Path, TextField.class);
	}

	@Override
	protected void setUpContainerPropertiesWithStrings() {
		addContainerProperty(Application.Field.Name, String.class);
		addContainerProperty(Application.Field.Version, String.class);
		addContainerProperty(Application.Field.Description, String.class);
		addContainerProperty(Application.Field.Path, String.class);
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
		Validate.notNull(application.getResource(), "resource cannot be null");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void fillNewItemProperties(final Application application, final Item item) {
		if (super.allowEdition) {
			item.getItemProperty(Application.Field.Name)
					.setValue(newTextFieldWithValue(StringUtils.trimToEmpty(application.getName())));
			item.getItemProperty(Application.Field.Version)
					.setValue(newTextFieldWithValue(StringUtils.trimToEmpty(application.getVersion())));
			item.getItemProperty(Application.Field.Description)
					.setValue(newTextAreaWithValue(StringUtils.trimToEmpty(application.getDescription())));
			item.getItemProperty(Application.Field.Path)
					.setValue(newTextFieldWithValue(StringUtils.trimToEmpty(application.getPath())));
		} else {
			item.getItemProperty(Application.Field.Name).setValue(StringUtils.trimToEmpty(application.getName()));
			item.getItemProperty(Application.Field.Version).setValue(StringUtils.trimToEmpty(application.getVersion()));
			item.getItemProperty(Application.Field.Description)
					.setValue(StringUtils.trimToEmpty(application.getDescription()));
			item.getItemProperty(Application.Field.Path).setValue(StringUtils.trimToEmpty(application.getPath()));
		}
	}

	@Override
	protected void beforeBatchSave() {
		owningResource.removeAllApplications();
	}

	@Override
	protected void save(final Application application) {
		owningResource.addApplication(application);
	}

	@Override
	protected Application convert(final Item item) {
		final Application application = new Application();
		application.setName(item.getItemProperty(Application.Field.Name).getValue().toString());
		application.setVersion(item.getItemProperty(Application.Field.Version).getValue().toString());
		application.setDescription(item.getItemProperty(Application.Field.Description).getValue().toString());
		application.setPath(item.getItemProperty(Application.Field.Path).getValue().toString());
		return application;
	}

}
