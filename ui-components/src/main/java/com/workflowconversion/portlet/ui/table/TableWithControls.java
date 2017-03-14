package com.workflowconversion.portlet.ui.table;

import java.util.Collection;

import com.vaadin.data.Container;
import com.vaadin.ui.Component;
import com.workflowconversion.portlet.core.resource.HasKey;

/**
 * Table to display generic elements. Contains methods to add items of a specific type, batch save changes and determine
 * if there are changes to be saved.
 * 
 * @author delagarza
 *
 */
public interface TableWithControls<T extends HasKey>
		extends Component, GenericElementCommittedListener<T>, GenericElementDetailsCommittedListener<T> {
	/**
	 * {@link Container} contains {@link #addItem(Object))}, but we need to enforce that implementations accept only
	 * certain type of elements (i.e., of type {@code T}).
	 */
	void insertItem(final T newElement);

	/**
	 * Initializes the table with the given elements.
	 * 
	 * @param initialElements
	 *            a collection of the initial elements to display.
	 */
	void init(final Collection<T> initialElements);

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