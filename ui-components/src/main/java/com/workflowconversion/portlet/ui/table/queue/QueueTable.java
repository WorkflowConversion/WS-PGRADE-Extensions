package com.workflowconversion.portlet.ui.table.queue;

import com.vaadin.data.Item;
import com.vaadin.ui.Label;
import com.workflowconversion.portlet.core.resource.Queue;
import com.workflowconversion.portlet.core.utils.KeyUtils;
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
public class QueuesTable extends AbstractTableWithControls<Queue> {

	private final static long serialVersionUID = -6243453484525350648L;
	private final static String PROPERTY_QUEUE = "QueueTable_property_queue";

	private QueuesTable(final String title) {
		super(title, false, false);
	}

	@Override
	protected void setUpContainerProperties() {
		super.addContainerProperty(Queue.Field.Name, Label.class);
		// no need to add type, this is a hidden property
		super.addContainerProperty(PROPERTY_QUEUE);
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
	protected String getKeyForItem(final Item item) {
		final String name = ((Label) (item.getItemProperty(Queue.Field.Name).getValue())).getValue();
		return KeyUtils.generateQueueKey(name);
	}

	@Override
	protected Object[] getVisibleColumns() {
		return new Object[] { Queue.Field.Name };
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void fillItemProperties(final Queue queue, final Item item) {
		item.getItemProperty(Queue.Field.Name).setValue(super.newLabelWithValue(queue.getName()));
		item.getItemProperty(PROPERTY_QUEUE).setValue(queue);
	}

	@Override
	protected Queue convertFromItem(final Item item) {
		return (Queue) item.getItemProperty(PROPERTY_QUEUE).getValue();
	}

	/**
	 * Factory for queue tables.
	 * 
	 * @author delagarza
	 */
	public static class QueueTableFactory extends AbstractTableWithControlsFactory<Queue> {

		@Override
		public TableWithControls<Queue> newInstance() {
			return new QueuesTable(super.title);
		}

	}
}
