package com.workflowconversion.portlet.ui.table.queue;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;
import com.workflowconversion.portlet.core.resource.Queue;
import com.workflowconversion.portlet.ui.table.AbstractAddGenericElementDialog;
import com.workflowconversion.portlet.ui.table.GenericElementCommittedListener;

public class AddQueueDialog extends AbstractAddGenericElementDialog<Queue> {
	private static final long serialVersionUID = -5777361522747805042L;

	protected AddQueueDialog(final GenericElementCommittedListener<Queue> listener) {
		super("Add queue", listener);
	}

	@Override
	protected Queue createDefaultElement() {
		return new Queue();
	}

	@Override
	protected void addAndBindComponents(final FormLayout formLayout, final FieldGroup fieldGroup) {
		final TextField queueName = createRequiredTextField("Queue name:", "Please enter a name for the queue",
				Queue.Field.Name.getMaxLength());

		formLayout.addComponent(queueName);

		fieldGroup.bind(queueName, Queue.Field.Name.getMemberName());
	}

}
