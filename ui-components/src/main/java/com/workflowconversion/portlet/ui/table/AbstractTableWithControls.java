package com.workflowconversion.portlet.ui.table;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.Resource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Alignment;
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
import com.workflowconversion.portlet.core.exception.TableIsReadOnlyException;
import com.workflowconversion.portlet.core.resource.HasKey;
import com.workflowconversion.portlet.ui.NotificationUtils;

/**
 * Abstract class to facilitate development of different tables displaying elements (such as resources, applications,
 * queues).
 * 
 * @author delagarza
 *
 * @param <T>
 */
public abstract class AbstractTableWithControls<T extends HasKey> extends VerticalLayout
		implements TableWithControls<T> {

	private static final long serialVersionUID = -8313705037279187002L;

	// css style name for rows with error
	private static final String STYLE_WITH_ERROR = "withError";
	// id of the property to use to store any error occurred when saving.
	private static final String PROPERTY_ERROR = "AbstractTableWithControls_Property_Error";

	private final String title;
	private final Button addButton;
	private final Button deleteButton;
	private final Button detailsButton;
	private final boolean withDetails;
	private final boolean allowDuplicates;
	private final Table table;
	private final Collection<ContainerProperty> containerProperties;

	private volatile boolean dirty;

	protected final boolean allowEdition;

	/**
	 * @param title
	 *            title to display above the table with elements.
	 * @param allowEdition
	 *            whether edit controls will be displayed and this table allows edition.
	 * @param withDetails
	 *            whether a "display details" button will be displayed.
	 * @param allowDuplicates
	 *            whether duplicates in the table (based on the keys produced by the elements) are allowed.
	 */
	protected AbstractTableWithControls(final String title, final boolean allowEdition, final boolean withDetails,
			final boolean allowDuplicates) {
		Validate.isTrue(StringUtils.isNotBlank(title),
				"title cannot be null, empty or contain only whitespace characters.");
		this.dirty = false;
		this.title = title;
		this.allowEdition = allowEdition;
		this.withDetails = withDetails;
		this.allowDuplicates = allowDuplicates;
		this.addButton = createButton("Add new item", FontAwesome.PLUS_CIRCLE);
		this.deleteButton = createButton("Delete selected item", FontAwesome.MINUS_CIRCLE);
		this.detailsButton = createButton("Show details for selected item", FontAwesome.EYE);
		this.containerProperties = new LinkedList<ContainerProperty>();
		this.table = new Table();
	}

	@Override
	public void init(final Collection<T> initialElements) {
		setUpContainerProperties();
		setUpEditComponents();
		setUpLayout();
		setUpProperties();
		setUpTable();
		// make sure to start the control in readOnly mode
		setReadOnly(true);
		setInitialItems(initialElements);
	}

	private void setUpEditComponents() {
		if (allowEdition) {
			addButton.setImmediate(true);
			addButton.setDisableOnClick(true);
			deleteButton.setImmediate(true);
			deleteButton.setDisableOnClick(true);

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

		if (withDetails) {
			detailsButton.setEnabled(true);
			detailsButton.setImmediate(true);
			detailsButton.setDisableOnClick(true);
			detailsButton.addClickListener(new Button.ClickListener() {
				private static final long serialVersionUID = -2169451476142160932L;

				@Override
				public void buttonClick(final ClickEvent event) {
					try {
						detailsButtonClicked();
					} finally {
						detailsButton.setEnabled(true);
					}
				}
			});
		}
	}

	private void addButtonClicked() {
		validateEditionAllowed();
		// display a window with the application fields
		final AbstractAddGenericElementDialog<T> addElementDialog = createAddElementDialog();
		addElementDialog.init();
		if (addElementDialog.getParent() != null) {
			NotificationUtils.displayWarning(
					"The 'Add Dialog' is already open. Please complete the form and then click on the 'Add' button to add a new item.");
		} else {
			UI.getCurrent().addWindow(addElementDialog);
		}
	}

	private void deleteButtonClicked() {
		validateEditionAllowed();
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

	private void detailsButtonClicked() {
		if (!withDetails) {
			throw new ApplicationException(
					"This table doesn't support the details button. This seems to be a coding problem and should be reported.");
		}
		final Set<?> selectedRowIds = (Set<?>) table.getValue();
		if (selectedRowIds.size() == 1) {
			final Object itemId = selectedRowIds.iterator().next();
			final Item selectedItem = table.getItem(itemId);
			final AbstractGenericElementDetailDialog<T> dialog = createElementDetailDialog(itemId,
					convertFromItem(selectedItem));
			if (dialog == null) {
				throw new ApplicationException(
						"Missing details dialog. This seems to be a coding problem and should be reported.");
			}
			UI.getCurrent().addWindow(dialog);
		} else {
			NotificationUtils.displayMessage("Please select one item to display its details.");
		}

	}

	private void setUpLayout() {
		setSizeUndefined();
		setSpacing(false);
		setMargin(false);

		final Label titleLabel = new Label();
		titleLabel.setContentMode(ContentMode.HTML);
		titleLabel.setValue("<h2>" + title + "</h2>");

		final HorizontalLayout titleBarLayout = new HorizontalLayout();
		titleBarLayout.setSpacing(true);
		titleBarLayout.setMargin(false);
		final Size tableDimensions = getSize();
		titleBarLayout.setWidth(tableDimensions.width, tableDimensions.widthUnit);

		titleBarLayout.addComponent(titleLabel);
		titleBarLayout.setComponentAlignment(titleLabel, Alignment.BOTTOM_LEFT);

		if (allowEdition || withDetails) {
			final HorizontalLayout buttonLayout = new HorizontalLayout();
			buttonLayout.setSpacing(true);
			buttonLayout.setMargin(false);

			if (allowEdition) {
				buttonLayout.addComponent(addButton);
				buttonLayout.addComponent(deleteButton);
				buttonLayout.setComponentAlignment(addButton, Alignment.MIDDLE_LEFT);
				buttonLayout.setComponentAlignment(deleteButton, Alignment.MIDDLE_RIGHT);
			}

			if (withDetails) {
				buttonLayout.addComponent(detailsButton);
				buttonLayout.setComponentAlignment(detailsButton, Alignment.MIDDLE_RIGHT);
			}

			titleBarLayout.addComponent(buttonLayout);
			titleBarLayout.setComponentAlignment(buttonLayout, Alignment.MIDDLE_RIGHT);

		}

		addComponent(titleBarLayout);
		setComponentAlignment(titleBarLayout, Alignment.BOTTOM_LEFT);

		addComponent(table);
	}

	private void setUpProperties() {
		Validate.notEmpty(containerProperties,
				"there are no properties set, use the addContainerProperty method in AbstractTableWithControls");
		table.addContainerProperty(PROPERTY_ERROR, String.class, null);
		for (final ContainerProperty containerProperty : containerProperties) {
			final Class<? extends Field<?>> fieldType = containerProperty.fieldType;
			table.addContainerProperty(containerProperty.id, fieldType == null ? Object.class : fieldType, null);
		}
	}

	/**
	 * Adds a property to the table.
	 * 
	 * @param id
	 *            the id of the property.
	 * @param fieldType
	 *            the type of the field that could edit this property.
	 */
	protected final void addContainerProperty(final Object id, final Class<? extends Field<?>> fieldType) {
		Validate.notNull(id, "id cannot be null");
		Validate.notNull(fieldType, "type cannot be null");
		this.containerProperties.add(new ContainerProperty(id, fieldType));
	}

	/**
	 * Adds an invisible property to the table.
	 * 
	 * @param id
	 *            the id of the property.
	 */
	protected final void addContainerProperty(final Object id) {
		Validate.notNull(id, "id cannot be null");
		this.containerProperties.add(new ContainerProperty(id, null));
	}

	/**
	 * Sets container properties.
	 */
	protected abstract void setUpContainerProperties();

	private void setUpTable() {
		final Size tableDimensions = getSize();
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
					final String error = (String) source.getItem(itemId).getItemProperty(PROPERTY_ERROR).getValue();
					if (StringUtils.isNotBlank(error)) {
						return STYLE_WITH_ERROR;
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
					final String error = (String) table.getItem(itemId).getItemProperty(PROPERTY_ERROR).getValue();
					return StringUtils.trimToNull(error);
				}

				return null;
			}
		});

	}

	/**
	 * @return the visible columns of the table.
	 */
	protected abstract Object[] getVisibleColumns();

	@Override
	public final void insertItem(final T element) {
		validateEditionAllowed();
		try {
			// do some basic validation
			Validate.notNull(element, "cannot add a null element");
			validate(element);
			insertItem_internal(element);
			dirty = true;
		} catch (final Exception e) {
			NotificationUtils.displayError("Could not add item", e);
		}
	}

	private void validateEditionAllowed() {
		if (!allowEdition) {
			throw new TableIsReadOnlyException("This table does not allow editions.");
		}
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
		Validate.notNull(initialItems, "initialItems cannot be null.");
		for (final T element : initialItems) {
			insertItem_internal(element);
		}
	}

	private Item insertItem_internal(final T element) {
		final Object id = table.addItem();
		final Item newItem = table.getItem(id);
		fillItemProperties(element, newItem);
		propagateReadOnly(newItem);
		if (allowEdition) {
			setItemFieldsValueChangeListeners(newItem);
		}
		return newItem;
	}

	/**
	 * Sets the properties of an item that can be edited.
	 * 
	 * @param element
	 *            the added element.
	 * @param item
	 *            the item whose properties are to be set.
	 */
	protected abstract void fillItemProperties(final T element, final Item item);

	private void propagateReadOnly(final Item item) {
		for (final Object propertyId : item.getItemPropertyIds()) {
			final Object propertyValue = item.getItemProperty(propertyId).getValue();
			if (propertyValue instanceof Component) {
				((Component) propertyValue).setReadOnly(!allowEdition || isReadOnly());
				((Component) propertyValue).setEnabled(allowEdition && !isReadOnly());
			}
		}
	}

	private void setItemFieldsValueChangeListeners(final Item item) {
		for (final Object propertyId : item.getItemPropertyIds()) {
			final Object propertyValue = item.getItemProperty(propertyId).getValue();
			if (propertyValue instanceof Field) {
				final Field<?> field = (Field<?>) propertyValue;
				field.addValueChangeListener(new Property.ValueChangeListener() {
					private static final long serialVersionUID = 2055195168270807750L;

					@Override
					public void valueChange(final ValueChangeEvent event) {
						dirty = true;
					}
				});

			}
		}
	}

	@Override
	public Collection<T> getAllElements() {
		final Collection<T> elements = new LinkedList<T>();
		for (final Object itemId : table.getItemIds()) {
			elements.add(convertFromItem(table.getItem(itemId)));
		}
		return elements;
	}

	@Override
	public final boolean isDirty() {
		return dirty;
	}

	@Override
	public final void saveAllChanges() {
		validateEditionAllowed();
		if (isDirty()) {
			if (markDuplicates()) {
				NotificationUtils.displayWarning("Some elements are duplicated. This table does not allow duplicates.");
			} else {
				saveAllChanges_internal();
			}
		}
	}

	private void saveAllChanges_internal() {
		beforeSaveAllChanges();
		boolean keepDirty = false;
		for (final Object id : table.getItemIds()) {
			// clear any previous error
			final Item item = table.getItem(id);
			clearError(item);
			try {
				// we assume the items are editable
				final T element = convertFromItem(item);
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

	// marks duplicate rows; returns true if there were duplicate rows
	// to decide whether there are duplicates, the key of the elements is used (duplicate keys assumes duplicate
	// elements)
	private boolean markDuplicates() {
		boolean hasDuplicates = false;
		boolean refresh = false;

		if (!allowDuplicates) {
			final Set<String> currentKeys = new TreeSet<String>();
			for (final Object itemId : table.getItemIds()) {
				final Item item = table.getItem(itemId);
				final T element = convertFromItem(item);
				if (!currentKeys.add(element.generateKey())) {
					markWithError(item, "This element is duplicated.");
					hasDuplicates = true;
					refresh = true;
				} else {
					if (hasError(item)) {
						clearError(item);
						refresh = true;
					}
				}
			}
		}

		if (refresh) {
			table.markAsDirtyRecursive();
		}

		return hasDuplicates;
	}

	private boolean hasError(final Item item) {
		return (item.getItemProperty(PROPERTY_ERROR).getValue() != null);
	}

	@Override
	public void clearSelection() {
		table.setValue(table.getNullSelectionItemId());
	}

	/**
	 * Allows implementations to perform an action before saving starts.
	 */
	protected abstract void beforeSaveAllChanges();

	/**
	 * Saves a single item.
	 * 
	 * @param item
	 *            the item to save.
	 */
	protected abstract void save(final T item);

	/**
	 * Converts an editable row item to an item of type {@code T}.
	 * 
	 * @param item
	 *            the row item.
	 * @return an item of type {@code T}.
	 */
	protected abstract T convertFromItem(final Item item);

	@SuppressWarnings("unchecked")
	private void clearError(final Item item) {
		// clear the cell containing the error
		item.getItemProperty(PROPERTY_ERROR).setValue(null);
		// remove the style from the components
		forEachComponent(item, new Action() {
			@Override
			public void performAction(final AbstractComponent component) {
				component.setDescription("");
				component.removeStyleName(STYLE_WITH_ERROR);
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void markWithError(final Item item, final String errorMessage) {
		// set the error in the cell
		item.getItemProperty(PROPERTY_ERROR).setValue(errorMessage);
		// set the style in the components
		forEachComponent(item, new Action() {
			@Override
			public void performAction(final AbstractComponent component) {
				component.setDescription(errorMessage);
				component.addStyleName(STYLE_WITH_ERROR);
			}
		});
	}

	private void markWithError(final Item item, final Exception error) {
		markWithError(item, error.getMessage());
	}

	@Override
	public final void elementCommitted(final T element) {
		insertItem(element);
	}

	@Override
	public void elementDetailsCommitted(final Object itemId, final T element) {
		if (!withDetails) {
			throw new ApplicationException(
					"This table does not support displaying/edition of details. This seems to be a coding error and should be reported.");
		}
		final Item item = table.getItem(itemId);
		fillItemProperties(element, item);
		dirty = true;
	}

	/**
	 * Implementations provide a dialog to add elements to the table.
	 * 
	 * @return the dialog to add elements.
	 */
	protected abstract AbstractAddGenericElementDialog<T> createAddElementDialog();

	/**
	 * Implementations provide a dialog to edit elements.
	 * 
	 * @param element
	 *            the element.
	 * @return the dialog to edit elements.
	 */
	protected AbstractGenericElementDetailDialog<T> createElementDetailDialog(final Object itemId, final T element) {
		if (!withDetails) {
			throw new ApplicationException(
					"This table does not support displaying/edition of details. This seems to be a coding error and should be reported.");
		}
		return null;
	}

	@Override
	public void setReadOnly(final boolean readOnly) {
		if (!allowEdition && !readOnly) {
			throw new ApplicationException(
					"This is a read-only component. This is quite likely a problem in the code and should be reported.");
		}
		propagateReadOnly(readOnly);
	}

	private void propagateReadOnly(final boolean readOnly) {
		super.setReadOnly(readOnly);
		addButton.setEnabled(!readOnly);
		deleteButton.setEnabled(!readOnly);
		// do not set the table as read only, rather, set the controls, if any, as readOnly
		for (final Object itemId : table.getItemIds()) {
			final Item item = table.getItem(itemId);
			forEachComponent(item, new Action() {
				@Override
				public void performAction(final AbstractComponent component) {
					component.setReadOnly(readOnly);
					component.setEnabled(!readOnly);
				}
			});
		}
		markAsDirtyRecursive();
		addButton.markAsDirtyRecursive();
		deleteButton.markAsDirtyRecursive();
		table.markAsDirtyRecursive();
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

	private static class ContainerProperty {
		final Object id;
		final Class<? extends Field<?>> fieldType;

		ContainerProperty(final Object id, final Class<? extends Field<?>> fieldType) {
			this.id = id;
			this.fieldType = fieldType;
		}
	}
}
