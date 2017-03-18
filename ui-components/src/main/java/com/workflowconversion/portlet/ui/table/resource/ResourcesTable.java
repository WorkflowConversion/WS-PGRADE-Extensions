package com.workflowconversion.portlet.ui.table.resource;

import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.vaadin.data.Item;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.TextField;
import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.core.resource.Queue;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.core.resource.ResourceProvider;
import com.workflowconversion.portlet.ui.table.AbstractAddGenericElementDialog;
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

	/**
	 * The size of a couple of components depends on the width of this table.
	 */
	public static final int WIDTH_PIXELS = 650;

	private final ResourceProvider resourceProvider;
	private final Collection<String> middlewareTypes;

	private ResourcesTable(final ResourceProvider resourceProvider, final String title,
			final Collection<String> middlewareTypes, final boolean allowEdition, final boolean withDetails,
			final boolean allowDuplicates, final boolean allowMultipleSelection) {
		super(title, allowEdition, withDetails, allowDuplicates, allowMultipleSelection);
		Validate.notEmpty(middlewareTypes, "middlewareTypes cannot be null or empty");
		Validate.notNull(resourceProvider, "resourceProvider cannot be null");
		this.resourceProvider = resourceProvider;
		this.middlewareTypes = middlewareTypes;
	}

	@Override
	protected void setUpContainerProperties() {
		addContainerProperty(Resource.Field.Name, TextField.class);
		addContainerProperty(Resource.Field.Type, ComboBox.class);
		// no need to provide a type, since these two properties are hidden
		// and their type will be set to Object.class
		addContainerProperty(Resource.Field.Queues);
		addContainerProperty(Resource.Field.Applications);
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

	@Override
	protected void validate(final Resource resource) {
		Validate.notNull(resource, "resource cannot be null");
		Validate.isTrue(StringUtils.isNotBlank(resource.getName()),
				"resource name cannot be null, empty, or contain only whitespace characters.");
		Validate.isTrue(middlewareTypes.contains(resource.getType()), "invalid resource type: " + resource.getType());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void fillItemProperties(final Resource resource, final Item item) {
		item.getItemProperty(Resource.Field.Name).setValue(newTextFieldWithValue(resource.getName()));
		item.getItemProperty(Resource.Field.Type).setValue(newComboBox(resource.getType(), middlewareTypes));
		item.getItemProperty(Resource.Field.Queues).setValue(resource.getQueues());
		item.getItemProperty(Resource.Field.Applications).setValue(resource.getApplications());
	}

	@Override
	protected void beforeSaveAllChanges() {
		resourceProvider.removeAllResources();
	}

	@Override
	protected void save(final Resource resource) {
		resourceProvider.addResource(resource);
	}

	@Override
	protected Resource convertFromItem(final Item item) {
		final Resource resource = new Resource();
		resource.setName(((TextField) item.getItemProperty(Resource.Field.Name).getValue()).getValue());
		resource.setType(((ComboBox) item.getItemProperty(Resource.Field.Type).getValue()).getValue().toString());
		addApplicationsAndQueues(resource, item);
		return resource;
	}

	@SuppressWarnings("unchecked")
	private void addApplicationsAndQueues(final Resource resource, final Item item) {
		for (final Application application : (Collection<Application>) item.getItemProperty(Resource.Field.Applications)
				.getValue()) {
			resource.addApplication(application);
		}
		for (final Queue queue : (Collection<Queue>) item.getItemProperty(Resource.Field.Queues).getValue()) {
			resource.addQueue(queue);
		}
	}

	@Override
	protected AbstractAddGenericElementDialog<Resource> createAddElementDialog() {
		return new AddResourceDialog(middlewareTypes, this);
	}

	@Override
	protected AbstractGenericElementDetailDialog<Resource> createElementDetailDialog(final Object itemId,
			final Resource element) {
		return new ResourceDetaislDialog(itemId, element, this, super.allowEdition);
	}

	/**
	 * Factory for resource tables.
	 * 
	 * @author delagarza
	 */
	public static class ResourceTableFactory extends AbstractTableWithControlsFactory<Resource> {
		private ResourceProvider resourceProvider;
		private Collection<String> middlewareTypes;

		@Override
		public TableWithControls<Resource> build() {
			return new ResourcesTable(resourceProvider, super.title, middlewareTypes, super.allowEdition,
					super.withDetails, super.allowDuplicates, super.allowMultipleSelection);
		}

		/**
		 * Sets the resource provider to use.
		 * 
		 * @param resourceProvider
		 *            the resource provider.
		 * @return {@code this}.
		 */
		public ResourceTableFactory withResourceProvider(final ResourceProvider resourceProvider) {
			this.resourceProvider = resourceProvider;
			return this;
		}

		/**
		 * Sets the valid middleware types.
		 * 
		 * @param middlewareTypes
		 *            the middleware types.
		 * @return {@code this}.
		 */
		public ResourceTableFactory withMiddlewareTypes(final Collection<String> middlewareTypes) {
			this.middlewareTypes = middlewareTypes;
			return this;
		}

	}
}
