package com.workflowconversion.portlet.ui.table;

import java.util.Collection;

import com.vaadin.data.Container;
import com.vaadin.ui.Component;

/**
 * Table to display generic elements. Contains methods to add items of a specific type, batch save changes and determine
 * if there are changes to be saved.
 * 
 * @author delagarza
 *
 */
public interface TableWithControls<T>
		extends Component, GenericElementCommittedListener<T>, GenericElementDetailsSavedListener<T> {
	/**
	 * {@link Container} contains {@link #addItem(Object))}, but we need to enforce that implementations accept only
	 * certain type of elements (i.e., of type {@code T}).
	 */
	void insertItem(final T newElement);

	/**
	 * Initializes the table with the given elements.
	 * 
	 * @param initialItems
	 *            a collection of the initial items to display.
	 */
	void setInitialItems(final Collection<T> initialItems);

	/**
	 * Allow users to check this table's dimensions.
	 * 
	 * @return the dimensions of this table.
	 */
	Size getSize();

	/**
	 * Clears any item selection.
	 */
	void clearSelection();

	/**
	 * Propagates changes done in the container to any data structure or storage system that holds these items.
	 */
	void saveAllChanges();

	/**
	 * @return whether the instance has unsaved changes.
	 */
	boolean isDirty();
}
