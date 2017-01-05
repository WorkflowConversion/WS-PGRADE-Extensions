package com.workflowconversion.portlet.ui.table.queue;

import org.apache.commons.lang.StringUtils;
import org.jsoup.helper.Validate;

import com.vaadin.data.Item;
import com.vaadin.ui.TextField;
import com.workflowconversion.portlet.core.resource.Queue;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.ui.table.AbstractAddGenericElementDialog;
import com.workflowconversion.portlet.ui.table.AbstractTableWithControls;
import com.workflowconversion.portlet.ui.table.Dimensions;
import com.workflowconversion.portlet.ui.table.GenericElementCommitedListener;

/**
 * Table on which the queues of a resource are displayed.
 * 
 * @author delagarza
 *
 */
public class QueueTable extends AbstractTableWithControls<Queue> implements GenericElementCommitedListener<Queue> {

	private static final long serialVersionUID = -6243453484525350648L;

	private final Resource resource;

	/**
	 * @param resource
	 *            the resource owning the displayed queues.
	 */
	public QueueTable(final Resource resource, final boolean withEditControls) {
		super("Queues", true, resource.getQueues());
		this.resource = resource;
	}

	@Override
	protected void setUpContainerPropertiesWithEditableFields() {
		super.addContainerProperty(Queue.Field.Name, TextField.class);
	}

	@Override
	protected void setUpContainerPropertiesWithStrings() {
		super.addContainerProperty(Queue.Field.Name, String.class);
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
	protected void fillNewItemProperties(final Queue queue, final Item item) {
		if (super.allowEdition) {
			item.getItemProperty(Queue.Field.Name).setValue(super.newTextFieldWithValue(queue.getName()));
		} else {
			item.getItemProperty(Queue.Field.Name).setValue(queue.getName());
		}
	}

	@Override
	protected void beforeBatchSave() {
		resource.removeAllQueues();
	}

	@Override
	protected Queue convert(final Item item) {
		final Queue queue = new Queue();
		queue.setName((String) item.getItemProperty(Queue.Field.Name).getValue());
		return queue;
	}

	@Override
	protected void save(final Queue queue) {
		resource.addQueue(queue);
	}

	@Override
	protected AbstractAddGenericElementDialog<Queue> createAddElementDialog() {
		return new AddQueueDialog(this);
	}

}
