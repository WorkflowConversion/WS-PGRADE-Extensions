package com.workflowconversion.portlet.ui.table.resource;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.vaadin.data.Item;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.core.resource.ResourceProvider;
import com.workflowconversion.portlet.ui.table.AbstractAddGenericElementDialog;
import com.workflowconversion.portlet.ui.table.AbstractTableWithControls;
import com.workflowconversion.portlet.ui.table.Dimensions;
import com.workflowconversion.portlet.ui.table.GenericElementCommitedListener;

/**
 * Table on which resources are displayed.
 * 
 * @author delagarza
 *
 */
public class ResourcesTable extends AbstractTableWithControls<Resource>
		implements GenericElementCommitedListener<Resource> {
	private static final long serialVersionUID = 4634915248824534764L;

	private final ResourceProvider resourceProvider;
	private final Collection<String> middlewareTypes;

	protected ResourcesTable(final ResourceProvider resourceProvider, final Collection<String> middlewareTypes,
			final boolean allowEdition) {
		super("Resources", allowEdition, resourceProvider.getResources());
		Validate.notEmpty(middlewareTypes, "middlewareTypes cannot be null or empty");
		this.resourceProvider = resourceProvider;
		this.middlewareTypes = middlewareTypes;
	}

	@Override
	protected void setUpContainerPropertiesWithEditableFields() {
		addContainerProperty(Resource.Field.Name, TextField.class);
		addContainerProperty(Resource.Field.Type, ComboBox.class);
	}

	@Override
	protected void setUpContainerPropertiesWithStrings() {
		addContainerProperty(Resource.Field.Name, String.class);
		addContainerProperty(Resource.Field.Type, String.class);
	}

	@Override
	protected Dimensions getTableDimensions() {
		final Dimensions tableDimensions = new Dimensions();
		tableDimensions.width = 300;
		tableDimensions.widthUnit = Unit.PIXELS;
		tableDimensions.height = 400;
		tableDimensions.heightUnit = Unit.PIXELS;
		return tableDimensions;
	}

	@Override
	protected Object[] getVisibleColumns() {
		return new Object[] { Resource.Field.Name, Resource.Field.Type };
	}

	@Override
	protected void validate(final Resource resource) {
		Validate.notNull(resource, "resource cannot be null");
		Validate.isTrue(StringUtils.isNotBlank(resource.getName()),
				"resource name cannot be null, empty, or contain only whitespace characters.");
		Validate.isTrue(middlewareTypes.contains(resource.getType()), "invalid resource type: " + resource.getType());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void fillNewItemProperties(final Resource resource, final Item item) {
		if (allowEdition) {
			item.getItemProperty(Resource.Field.Name).setValue(newTextFieldWithValue(resource.getName()));
			item.getItemProperty(Resource.Field.Type).setValue(newComboBox(resource.getType(), middlewareTypes));
		} else {
			item.getItemProperty(Resource.Field.Name).setValue(resource.getName());
			item.getItemProperty(Resource.Field.Type).setValue(resource.getType());
		}
	}

	@Override
	protected void beforeBatchSave() {
		resourceProvider.removeAllResources();
	}

	@Override
	protected void save(final Resource resource) {
		resourceProvider.addResource(resource);
	}

	@Override
	protected Resource convert(final Item item) {
		final Resource resource = new Resource();
		resource.setName((String) item.getItemProperty(Resource.Field.Name).getValue());
		resource.setType((String) item.getItemProperty(Resource.Field.Type).getValue());
		return resource;
	}

	@Override
	protected AbstractAddGenericElementDialog<Resource> createAddElementDialog() {
		return new AddResourceDialog(middlewareTypes, this);
	}

}
