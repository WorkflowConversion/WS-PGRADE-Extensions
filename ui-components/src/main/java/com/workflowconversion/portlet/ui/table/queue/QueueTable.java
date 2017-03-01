package com.workflowconversion.portlet.ui.table.queue;

import org.apache.commons.lang.StringUtils;
import org.jsoup.helper.Validate;

import com.vaadin.data.Item;
import com.vaadin.ui.TextField;
import com.workflowconversion.portlet.core.resource.Queue;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.ui.table.AbstractAddGenericElementDialog;
import com.workflowconversion.portlet.ui.table.AbstractTableWithControls;
import com.workflowconversion.portlet.ui.table.AbstractTableWithControlsFactory;
import com.workflowconversion.portlet.ui.table.Size;
import com.workflowconversion.portlet.ui.table.TableWithControls;

/**
 * Table on which the queues of a resource are displayed.
 * 
 * @author delagarza
 *
 */
public class QueueTable extends AbstractTableWithControls<Queue> {

	private static final long serialVersionUID = -6243453484525350648L;

	private final Resource owningResource;

	private QueueTable(final Resource resource, final String title, final boolean withEditControls,
			final boolean withDetails, final boolean allowDuplicates) {
		super(title, withEditControls, withDetails, allowDuplicates);
		this.owningResource = resource;
	}

	@Override
	protected void setUpContainerProperties() {
		super.addContainerProperty(Queue.Field.Name, TextField.class);
	}

	@Override
	public Size getSize() {
		final Size tableDimensions = new Size();
		tableDimensions.width = 300;
		tableDimensions.widthUnit = Unit.PIXELS;
		tableDimensions.height = 400;
		tableDimensions.heightUnit = Unit.PIXELS;
		return tableDimensions;
	}

	@Override
	protected Object[] getVisibleColumns() {
		return new Object[] { Queue.Field.Name };
	}

	@Override
	protected void validate(final Queue queue) {
		Validate.notNull(queue, "queue cannot be null");
		Validate.isTrue(StringUtils.isNotBlank(queue.getName()),
				"queue name cannot be null, empty or contain only whitespace characters.");
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void fillItemProperties(final Queue queue, final Item item) {
		item.getItemProperty(Queue.Field.Name).setValue(super.newTextFieldWithValue(queue.getName()));
	}

	@Override
	protected void beforeSaveAllChanges() {
		owningResource.removeAllQueues();
	}

	@Override
	protected Queue convertFromItem(final Item item) {
		final Queue queue = new Queue();
		queue.setName(((TextField) item.getItemProperty(Queue.Field.Name).getValue()).getValue());
		return queue;
	}

	@Override
	protected void save(final Queue queue) {
		owningResource.addQueue(queue);
	}

	@Override
	protected AbstractAddGenericElementDialog<Queue> createAddElementDialog() {
		return new AddQueueDialog(this);
	}

	/**
	 * Factory for queue tables.
	 * 
	 * @author delagarza
	 */
	public static class QueueTableFactory extends AbstractTableWithControlsFactory<Queue> {
		private Resource owningResource;

		@Override
		public TableWithControls<Queue> build() {
			return new QueueTable(owningResource, super.title, super.allowEdition, super.withDetails,
					super.allowDuplicates);
		}

		public QueueTableFactory withOwningResource(final Resource owningResource) {
			this.owningResource = owningResource;
			return this;
		}

	}
}
