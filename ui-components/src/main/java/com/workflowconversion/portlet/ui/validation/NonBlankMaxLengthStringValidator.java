package com.workflowconversion.portlet.ui.validation;

import org.apache.commons.lang.StringUtils;

import com.vaadin.data.validator.AbstractValidator;

/**
 * String validator. Only non-blank strings with a maximum length will be valid.
 * 
 * @author delagarza
 *
 */
public class NonBlankMaxLengthStringValidator extends AbstractValidator<String> {

	private static final long serialVersionUID = 8476005512295626122L;
	private final int maxLength;

	/**
	 * @param maxLength
	 *            the maximum length to allow.
	 */
	public NonBlankMaxLengthStringValidator(final int maxLength) {
		this("The field cannot be empty, blank, contain only whitespaces or contain more than " + maxLength
				+ " characters.", maxLength);
	}

	/**
	 * @param errorMessage
	 *            a custom error message to be displayed if a string is not valid
	 * @param maxLength
	 *            the maximum length to allow.
	 */
	public NonBlankMaxLengthStringValidator(final String errorMessage, final int maxLength) {
		super(errorMessage);
		this.maxLength = maxLength;
	}

	@Override
	protected boolean isValidValue(final String value) {
		if (value == null) {
			return false;
		}
		return StringUtils.isNotBlank(value.toString()) && value.toString().length() <= maxLength;
	}

	@Override
	public Class<String> getType() {
		return String.class;
	}
}