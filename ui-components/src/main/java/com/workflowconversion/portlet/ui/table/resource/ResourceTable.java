package com.workflowconversion.portlet.ui.table.resource;

import org.apache.commons.lang3.Validate;

import com.vaadin.data.Item;
import com.vaadin.ui.Label;
import com.workflowconversion.portlet.core.exception.ApplicationException;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.core.resource.ResourceProvider;
import com.workflowconversion.portlet.core.utils.KeyUtils;
import com.workflowconversion.portlet.ui.table.AbstractGenericElementDetailsDialog;
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
public class ResourceTable extends AbstractTableWithControls<Resource> {
	private final static long serialVersionUID = 4634915248824534764L;

	private final static String PROPERTY_RESOURCE = "ResourcesTable_property_resource";

	private final ResourceProvider resourceProvider;

	/**
	 * The size of a couple of components depends on the width of this table.
	 */
	public static final int WIDTH_PIXELS = 650;

	private ResourceTable(final String title, final ResourceProvider resourceProvider) {
		// cannot modify resources
		super(title, false, true);
		Validate.notNull(resourceProvider,
				"resourceProvider cannot be null. This seems to be a coding problem and should be reported.");
		this.resourceProvider = resourceProvider;
	}

	@Override
	protected void setUpContainerProperties() {
		addContainerProperty(Resource.Field.Name, Label.class);
		addContainerProperty(Resource.Field.Type, Label.class);
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
		item.getItemProperty(Resource.Field.Name).setValue(newLabelWithValue(resource.getName()));
		item.getItemProperty(Resource.Field.Type).setValue(newLabelWithValue(resource.getType()));
		item.getItemProperty(Resource.Field.Queues).setValue(resource.getQueues());
		item.getItemProperty(Resource.Field.Applications).setValue(resource.getApplications());
		item.getItemProperty(PROPERTY_RESOURCE).setValue(resource);
	}

	@Override
	protected String getKeyForItem(final Item item) {
		final String name = ((Label) (item.getItemProperty(Resource.Field.Name).getValue())).getValue();
		final String type = ((Label) (item.getItemProperty(Resource.Field.Type).getValue())).getValue();
		return KeyUtils.generateResourceKey(name, type);
	}

	@Override
	protected Resource convertFromItem(final Item item) {
		return (Resource) item.getItemProperty(PROPERTY_RESOURCE).getValue();
	}

	@Override
	protected AbstractGenericElementDetailsDialog<Resource> createElementDetailDialog(final Object itemId,
			final Resource element) {
		return new ResourceDetailsDialog(itemId, element, this, element.canModifyApplications());
	}

	@Override
	public void elementDetailsSaved(final Object itemId, final Resource resource) {
		if (resourceProvider.canAddApplications() && resource.canModifyApplications()) {
			super.elementDetailsSaved(itemId, resource);
			resourceProvider.save(resource);
		} else {
			throw new ApplicationException(
					"The resource and/or the provider don't support modifying/adding applications. This seems to be a coding problem and should be reported.");
		}

	}

	/**
	 * Factory for resource tables.
	 * 
	 * @author delagarza
	 */
	public static class ResourceTableFactory extends AbstractTableWithControlsFactory<Resource> {
		private ResourceProvider resourceProvider;

		/**
		 * @param resourceProvider
		 *            the resource provider.
		 * @return {@code this} factory.
		 */
		public ResourceTableFactory withResourceProvider(final ResourceProvider resourceProvider) {
			this.resourceProvider = resourceProvider;
			return this;
		}

		@Override
		public TableWithControls<Resource> newInstance() {
			return new ResourceTable(super.title, resourceProvider);
		}
	}
}
