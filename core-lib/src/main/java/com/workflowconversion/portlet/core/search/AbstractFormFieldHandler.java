package com.workflowconversion.portlet.core.search;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workflowconversion.portlet.core.exception.ApplicationException;
import com.workflowconversion.portlet.core.exception.InvalidFieldException;
import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.core.resource.FormField;
import com.workflowconversion.portlet.core.resource.Queue;
import com.workflowconversion.portlet.core.resource.Resource;

abstract class AbstractFormFieldHandler implements FormFieldHandler {

	private final static Logger LOG = LoggerFactory.getLogger(AbstractFormFieldHandler.class);

	private final Map<FormField, String> fields;
	private final Class<? extends FormField> fieldType;
	private final Set<FormField> handledFields;

	protected AbstractFormFieldHandler(final Class<? extends FormField> fieldType,
			final Map<FormField, String> fields) {
		this.fields = fields;
		this.fieldType = fieldType;
		this.handledFields = new TreeSet<FormField>();
		fillHandledFields();
	}

	private void fillHandledFields() {
		if (Resource.Field.class.isAssignableFrom(fieldType)) {
			handledFields.addAll(Arrays.asList(Resource.Field.Name, Resource.Field.Type));
		} else if (Application.Field.class.isAssignableFrom(fieldType)) {
			handledFields
					.addAll(Arrays.asList(Application.Field.Name, Application.Field.Version, Application.Field.Path));
		} else if (Queue.Field.class.isAssignableFrom(fieldType)) {
			handledFields.addAll(Arrays.asList(Queue.Field.Name));
		} else {
			throw new ApplicationException("Field type " + fieldType
					+ " cannot handled. This seems to be a coding problem and should be reported.");
		}
		if (LOG.isDebugEnabled()) {
			LOG.debug("Loaded handled fields for fieldType=" + fieldType);
			LOG.debug("handledFields=" + handledFields);
		}
	}

	@Override
	public boolean canHandle(final FormField field) {
		return fieldType.isAssignableFrom(field.getClass());
	}

	@Override
	public void handle(final FormField field, final String value) {
		if (handledFields.contains(field)) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Handling field=" + field.getClass() + '.' + field + ", value=" + value);
			}
			fields.put(field, value);
		} else {
			throw new InvalidFieldException(field);
		}
	}

}
