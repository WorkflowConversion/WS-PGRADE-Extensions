package com.workflowconversion.portlet.ui.table.resource;

import com.vaadin.data.Item;
import com.vaadin.ui.TextField;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.core.utils.KeyUtils;
import com.workflowconversion.portlet.ui.table.AbstractGenericElementDetailDialog;
import com.workflowconversion.portlet.ui.table.AbstractTableWithControls;
import com.workflowconversion.portlet.ui.table.AbstractTableWithControlsFactory;
import com.workflowconversion.portlet.ui.table.Size;
import com.workflowconversion.portlet.ui.table.TableWithControls;

/**
 * Table on which resources are displayed.
 * 
 * @author delagarza
 *
 */
public class ResourcesTable extends AbstractTableWithControls<Resource> {
	private static final long serialVersionUID = 4634915248824534764L;

	private final static String PROPERTY_RESOURCE = "ResourcesTable_property_resource";

	/**
	 * The size of a couple of components depends on the width of this table.
	 */
	public static final int WIDTH_PIXELS = 650;

	private ResourcesTable(final String title) {
		// cannot modify resources
		super(title, false, true);
	}

	@Override
	protected void setUpContainerProperties() {
		addContainerProperty(Resource.Field.Name, TextField.class);
		addContainerProperty(Resource.Field.Type, TextField.class);
		// no need to provide a type, since these two properties are hidden
		// and their type will be set to Object.class
		addContainerProperty(Resource.Field.Queues);
		addContainerProperty(Resource.Field.Applications);
		addContainerProperty(PROPERTY_RESOURCE);
	}

	@Override
	public Size getSize() {
		final Size tableDimensions = new Size();
		tableDimensions.width = WIDTH_PIXELS;
		tableDimensions.widthUnit = Unit.PIXELS;
		tableDimensions.height = 350;
		tableDimensions.heightUnit = Unit.PIXELS;
		return tableDimensions;
	}

	@Override
	protected Object[] getVisibleColumns() {
		return new Object[] { Resource.Field.Name, Resource.Field.Type };
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void fillItemProperties(final Resource resource, final Item item) {
		item.getItemProperty(Resource.Field.Name).setValue(newTextFieldWithValue(resource.getName()));
		item.getItemProperty(Resource.Field.Type).setValue(newTextFieldWithValue(resource.getType()));
		item.getItemProperty(Resource.Field.Queues).setValue(resource.getQueues());
		item.getItemProperty(Resource.Field.Applications).setValue(resource.getApplications());
		item.getItemProperty(PROPERTY_RESOURCE).setValue(resource);
	}

	@Override
	protected String getKeyForItem(final Item item) {
		final String name = ((TextField) (item.getItemProperty(Resource.Field.Name).getValue())).getValue();
		final String type = ((TextField) (item.getItemProperty(Resource.Field.Type).getValue())).getValue();
		return KeyUtils.generateResourceKey(name, type);
	}

	@Override
	protected Resource convertFromItem(final Item item) {
		return (Resource) item.getItemProperty(PROPERTY_RESOURCE).getValue();
	}

	@Override
	protected AbstractGenericElementDetailDialog<Resource> createElementDetailDialog(final Object itemId,
			final Resource element) {
		return new ResourceDetaislDialog(itemId, element, this, element.canModifyApplications());
	}

	/**
	 * Factory for resource tables.
	 * 
	 * @author delagarza
	 */
	public static class ResourcesTableFactory extends AbstractTableWithControlsFactory<Resource> {

		@Override
		public TableWithControls<Resource> newInstance() {
			return new ResourcesTable(super.title);
		}
	}
}
