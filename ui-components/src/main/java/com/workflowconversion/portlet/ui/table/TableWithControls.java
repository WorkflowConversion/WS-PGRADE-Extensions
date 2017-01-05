package com.workflowconversion.portlet.ui.table;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.ui.Component;

/**
 * Table to display generic elements. Contains methods to add items of a specific type, batch save changes and determine
 * if there are changes to be saved.
 * 
 * @author delagarza
 *
 */
public interface TableWithControls<T> extends Component {
	/**
	 * {@link Container} contains {@link #addItem(Object))}, but we need to enforce that implementations accept only
	 * certain type of elements (i.e., of type {@code T}).
	 * 
	 * @param item
	 *            the item to insert.
	 */
	Item insertItem(final T newElement);

	/**
	 * Propagates changes done in the container to any data structure or storage system that holds these items.
	 */
	void batchSave();

	/**
	 * @return whether the instance has unsaved changes.
	 */
	boolean isDirty();
}
