package com.workflowconversion.portlet.core.search;

import com.workflowconversion.portlet.core.resource.FormField;

/**
 * Handles search criteria.
 * 
 * @author delagarza
 *
 */
interface FormFieldHandler {

	/**
	 * Whether the field can be handled.
	 * 
	 * @param field
	 *            the field.
	 * @return whether the field can be handled.
	 */
	boolean canHandle(final FormField field);

	/**
	 * Handles the search criteria.
	 * 
	 * @param field
	 *            the field.
	 * @param value
	 *            the value.
	 */
	void handle(final FormField field, final String value);
}
