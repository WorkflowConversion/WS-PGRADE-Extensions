package com.workflowconversion.portlet.ui.table.resource;

import java.util.Collection;

import org.apache.commons.lang.Validate;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.ui.table.AbstractAddGenericElementDialog;
import com.workflowconversion.portlet.ui.table.GenericElementCommittedListener;

/**
 * Modal dialog to add resources.
 * 
 * @author delagarza
 *
 */
public class AddResourceDialog extends AbstractAddGenericElementDialog<Resource> {

	private static final long serialVersionUID = -8468900974796268721L;

	final Collection<String> middlewareTypes;

	/**
	 * @param middlewareTypes
	 *            a collection with all of the allowed middleware types.
	 * @param listener
	 *            a listener to this dialog's events.
	 */
	public AddResourceDialog(final Collection<String> middlewareTypes,
			final GenericElementCommittedListener<Resource> listener) {
		super("Add Resource", listener);
		Validate.notEmpty(middlewareTypes, "middlewareTypes cannot be null or empty.");
		Validate.notNull(listener, "listener cannot be null.");
		this.middlewareTypes = middlewareTypes;
	}

	@Override
	protected Resource createDefaultElement() {
		final Resource resource = new Resource();
		resource.setType(middlewareTypes.iterator().next());
		return resource;
	}

	@Override
	protected void addAndBindComponents(final FormLayout formLayout, final FieldGroup fieldGroup) {
		final TextField name = createRequiredTextField("Resource name:", "Please enter a name for the resource",
				Resource.Field.Name.getMaxLength());
		final ComboBox resourceType = createComboBox("Resource type:", "Please select a resource type",
				middlewareTypes);

		formLayout.addComponent(name);
		formLayout.addComponent(resourceType);

		fieldGroup.bind(name, Resource.Field.Name.getMemberName());
		fieldGroup.bind(resourceType, Resource.Field.Type.getMemberName());
	}

}
