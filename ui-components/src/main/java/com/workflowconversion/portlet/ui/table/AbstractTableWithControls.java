package com.workflowconversion.portlet.ui.table;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.workflowconversion.portlet.core.exception.ApplicationException;
import com.workflowconversion.portlet.ui.HorizontalSeparator;
import com.workflowconversion.portlet.ui.NotificationUtils;

/**
 * Abstract class to facilitate development of different tables displaying elements (such as resources, applications,
 * queues).
 * 
 * @author delagarza
 *
 * @param <T>
 */
public abstract class AbstractTableWithControls<T> extends VerticalLayout
		implements GenericElementCommitedListener<T>, TableWithControls<T> {

	private static final long serialVersionUID = -8313705037279187002L;

	// id of the property to use to store any error occurred when saving.
	private static final String PROPERTY_ERROR = "AbstractTableWithControls_Property_Error";

	private final String title;
	private final Button addButton;
	private final Button deleteButton;
	private final Table table;
	private boolean dirty;
	// no danger in making this one protected since it's final anyway
	protected final boolean allowEdition;

	/**
	 * @param title
	 *            title to display above the table with elements.
	 * @param addElementDialog
	 *            the dialog to display when a new element is to be added.
	 */
	protected AbstractTableWithControls(final String title, final boolean allowEdition,
			final Collection<T> initialItems) {
		Validate.isTrue(StringUtils.isNotBlank(title),
				"title cannot be null, empty or contain only whitespace characters.");
		Validate.notNull(initialItems, "initialItems cannot be null.");

		this.dirty = false;
		this.title = title;
		this.allowEdition = allowEdition;
		this.addButton = createButton("Add new item", FontAwesome.PLUS_CIRCLE);
		this.deleteButton = createButton("Delete selected item(s)", FontAwesome.MINUS_CIRCLE);
		this.table = new Table();

		setUpEditComponents();
		setUpLayout();
		setUpProperties();
		setUpTable();

		setInitialItems(initialItems);
	}

	private void setUpEditComponents() {
		if (allowEdition) {
			addButton.setImmediate(true);
			deleteButton.setImmediate(true);

			addButton.addClickListener(new Button.ClickListener() {
				private static final long serialVersionUID = 2596096312063338614L;

				@Override
				public void buttonClick(final ClickEvent event) {
					try {
						addButtonClicked();
					} finally {
						addButton.setEnabled(true);
					}
				}
			});

			deleteButton.addClickListener(new Button.ClickListener() {
				private static final long serialVersionUID = -954031148232687091L;

				@Override
				public void buttonClick(final ClickEvent event) {
					try {
						deleteButtonClicked();
					} finally {
						deleteButton.setEnabled(true);
					}
				}
			});
		}
	}

	private void addButtonClicked() {
		// display a window with the application fields
		final AbstractAddGenericElementDialog<T> addElementDialog = createAddElementDialog();
		if (addElementDialog.getParent() != null) {
			NotificationUtils.displayWarning(
					"The 'Add Dialog' is already open. Please complete the form and then click on the 'Add' button to add a new item.");
		} else {
			UI.getCurrent().addWindow(addElementDialog);
		}
	}

	private void deleteButtonClicked() {
		// since multiselect is enabled, we get a set of the selected values
		final Set<?> selectedRowIds = (Set<?>) table.getValue();
		if (CollectionUtils.isNotEmpty(selectedRowIds)) {
			for (final Object selectedRowId : selectedRowIds) {
				table.removeItem(selectedRowId);
			}
			dirty = true;
		} else {
			NotificationUtils.displayMessage("Please select at least one item to delete.");
		}
	}

	private void setUpLayout() {
		final Label titleLabel = new Label();
		titleLabel.setValue(title);

		final HorizontalLayout titleBarLayout = new HorizontalLayout();
		titleBarLayout.setSpacing(true);
		titleBarLayout.setMargin(true);

		titleBarLayout.addComponent(titleLabel);
		if (allowEdition) {
			titleBarLayout.addComponent(addButton);
			titleBarLayout.addComponent(deleteButton);
		}

		addComponent(titleBarLayout);
		addComponent(new HorizontalSeparator());
		addComponent(table);
	}

	private void setUpProperties() {
		table.addContainerProperty(PROPERTY_ERROR, Exception.class, null);
		if (allowEdition) {
			setUpContainerPropertiesWithEditableFields();
		} else {
			setUpContainerPropertiesWithStrings();
		}
	}

	/**
	 * Sets container properties bound to editable fields.
	 */
	protected abstract void setUpContainerPropertiesWithEditableFields();

	/**
	 * Sets container properties bound to editable fields.
	 */
	protected abstract void setUpContainerPropertiesWithStrings();

	/**
	 * Adds a property to the table.
	 * 
	 * @param propertyId
	 *            the id of the property.
	 * @param type
	 *            the type.
	 */
	protected final void addContainerProperty(final Object propertyId, final Class<?> type) {
		table.addContainerProperty(propertyId, type, null);
	}

	private void setUpTable() {
		final Dimensions tableDimensions = getTableDimensions();
		table.setWidth(tableDimensions.width, tableDimensions.widthUnit);
		table.setHeight(tableDimensions.height, tableDimensions.heightUnit);

		table.addStyleName("tableWithControls");
		table.setSelectable(true);
		table.setMultiSelect(true);
		table.setBuffered(false);
		table.setEditable(false);
		table.setSortEnabled(false);
		table.setImmediate(true);

		table.setVisibleColumns(getVisibleColumns());

		table.setCellStyleGenerator(new Table.CellStyleGenerator() {
			private static final long serialVersionUID = 1L;

			@Override
			public String getStyle(final Table source, final Object itemId, final Object propertyId) {
				// apply only at the row level
				if (propertyId == null) {
					final Exception error = (Exception) source.getItem(itemId).getItemProperty(PROPERTY_ERROR)
							.getValue();
					if (error != null) {
						return "withError";
					}
				}
				return null;
			}
		});

		table.setItemDescriptionGenerator(new Table.ItemDescriptionGenerator() {
			private static final long serialVersionUID = 1L;

			@Override
			public String generateDescription(final Component source, final Object itemId, final Object propertyId) {
				// apply only at the row level
				if (propertyId == null) {
					final Exception error = (Exception) table.getItem(itemId).getItemProperty(PROPERTY_ERROR)
							.getValue();
					if (error != null) {
						return error.getMessage();
					}
				}

				return null;
			}
		});

	}

	/**
	 * Allow implementations to provide custom dimensions for the table.
	 */
	protected abstract Dimensions getTableDimensions();

	/**
	 * @return the visible columns of the table.
	 */
	protected abstract Object[] getVisibleColumns();

	@Override
	public final Item insertItem(final T element) {
		try {
			// do some basic validation
			Validate.notNull(element, "cannot add a null element");
			validate(element);
		} catch (final Exception e) {
			NotificationUtils.displayError("Could not add item", e);
		}
		final Item item = insertItem_internal(element);
		dirty = true;
		return item;
	}

	/**
	 * Allow implementations to validate items so only proper elements are to be inserted/saved. If the element is not
	 * valid, implementations should throw an exception.
	 * 
	 * @param element
	 *            the item that will be inserted.
	 */
	protected abstract void validate(final T element);

	private void setInitialItems(final Collection<T> initialItems) {
		for (final T element : initialItems) {
			insertItem_internal(element);
		}
	}

	private Item insertItem_internal(final T element) {
		final Object id = table.addItem();
		final Item newItem = table.getItem(id);
		fillNewItemProperties(element, newItem);
		if (allowEdition) {
			propagateReadOnly(newItem);
			setValueChangeListener(newItem);
		}
		return newItem;
	}

	/**
	 * Sets the properties of the item using the given element.
	 * 
	 * @param element
	 *            the added element.
	 * @param item
	 *            the item whose properties are to be set.
	 */
	protected abstract void fillNewItemProperties(final T element, final Item item);

	private void propagateReadOnly(final Item item) {
		for (final Object propertyId : item.getItemPropertyIds()) {
			final Object propertyValue = item.getItemProperty(propertyId).getValue();
			if (propertyValue instanceof Component) {
				((Component) propertyValue).setReadOnly(isReadOnly());
				((Component) propertyValue).setEnabled(!isReadOnly());
			}
		}
	}

	private void setValueChangeListener(final Item item) {
		for (final Object propertyId : item.getItemPropertyIds()) {
			final Object propertyValue = item.getItemProperty(propertyId).getValue();
			if (propertyValue instanceof Field) {
				((Field<?>) propertyValue).addValueChangeListener(new Property.ValueChangeListener() {
					private static final long serialVersionUID = 2055195168270807750L;

					@Override
					public void valueChange(final ValueChangeEvent event) {
						dirty = true;
					}
				});
				;
			}
		}
	}

	@Override
	public final boolean isDirty() {
		return dirty;
	}

	@Override
	public final void batchSave() {
		if (isDirty()) {
			beforeBatchSave();
			boolean keepDirty = false;
			for (final Object id : table.getItemIds()) {
				// clear any previous error
				final Item item = table.getItem(id);
				clearError(item);
				try {
					final T element = convert(item);
					validate(element);
					save(element);
				} catch (final Exception e) {
					markWithError(item, e);
					// if there were errors, keep the table dirty
					keepDirty = true;
				}
			}
			dirty = keepDirty;
			// mark table as dirty to update style/tooltips
			if (dirty) {
				table.markAsDirtyRecursive();
			}
		}
	}

	/**
	 * Allows implementations to perform an action before saving starts.
	 */
	protected abstract void beforeBatchSave();

	/**
	 * Saves a single item.
	 * 
	 * @param item
	 *            the item to save.
	 */
	protected abstract void save(final T item);

	/**
	 * Converts a row item to an item of type {@code T}.
	 * 
	 * @param item
	 *            the row item.
	 * @return an item of type {@code T}.
	 */
	protected abstract T convert(final Item item);

	@SuppressWarnings("unchecked")
	private void clearError(final Item item) {
		// clear the cell containing the error
		item.getItemProperty(PROPERTY_ERROR).setValue(null);
		// remove the style from the components
		forEachComponent(item, new Action() {
			@Override
			public void performAction(final AbstractComponent component) {
				component.setDescription("");
				component.removeStyleName("withError");
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void markWithError(final Item item, final Exception error) {
		// set the error in the cell
		item.getItemProperty(PROPERTY_ERROR).setValue(error);
		// set the style in the components
		forEachComponent(item, new Action() {
			@Override
			public void performAction(final AbstractComponent component) {
				component.setDescription(error.getMessage());
				component.addStyleName("withError");
			}
		});
	}

	@Override
	public final void elementCommitted(final T element) {
		insertItem(element);
	}

	/**
	 * Implementations provide a dialog to add elements to the table.
	 * 
	 * @return the dialog to add elements.
	 */
	protected abstract AbstractAddGenericElementDialog<T> createAddElementDialog();

	@Override
	public void setReadOnly(final boolean readOnly) {
		if (!allowEdition) {
			if (!readOnly) {
				throw new ApplicationException(
						"This is a read-only component. This is quite likely a problem in the code and should be reported.");
			}
		} else {
			if (isReadOnly() != readOnly) {
				super.setReadOnly(readOnly);
				addButton.setReadOnly(readOnly);
				deleteButton.setReadOnly(readOnly);
				table.setReadOnly(readOnly);
				for (final Object itemId : table.getItemIds()) {
					final Item item = table.getItem(itemId);
					propagateReadOnlyToTableContents(item);
				}
			}
		}
	}

	private void propagateReadOnlyToTableContents(final Item item) {
		forEachComponent(item, new Action() {
			@Override
			public void performAction(final AbstractComponent component) {
				component.setReadOnly(isReadOnly());
				component.setEnabled(!isReadOnly());

			}
		});
	}

	// half-ass attempt of a lambda operator, but hey, it's java 1.7 compatible
	private void forEachComponent(final Item item, final Action action) {
		for (final Object propertyId : item.getItemPropertyIds()) {
			final Object propertyValue = item.getItemProperty(propertyId).getValue();
			if (propertyValue instanceof AbstractComponent) {
				action.performAction((AbstractComponent) propertyValue);
			}
		}
	}

	private interface Action {
		void performAction(final AbstractComponent component);
	}

	private Button createButton(final String description, final Resource icon) {
		final Button button = new Button();
		button.setIcon(icon);
		button.setDescription(description);
		button.setEnabled(false);
		button.setDisableOnClick(true);
		button.setImmediate(true);
		return button;
	}

	protected final TextField newTextFieldWithValue(final String value) {
		final TextField textField = new TextField();
		textField.setValue(value);
		textField.setImmediate(true);
		textField.setBuffered(false);
		return textField;
	}

	protected final TextArea newTextAreaWithValue(final String value) {
		final TextArea textArea = new TextArea();
		textArea.setValue(value);
		textArea.setImmediate(true);
		textArea.setBuffered(false);
		return textArea;
	}

	protected final ComboBox newComboBox(final String initialSelection, final Collection<String> options) {
		final ComboBox comboBox = new ComboBox();
		for (final String option : options) {
			comboBox.addItem(option);
		}
		comboBox.setNullSelectionAllowed(false);
		comboBox.setImmediate(true);
		comboBox.setBuffered(false);
		comboBox.select(initialSelection);

		return comboBox;
	}
}
