package com.workflowconversion.portlet.ui.validation;

/**
 * String validator. Only non-blank strings will be valid.
 * 
 * @author delagarza
 *
 */
public class NonBlankStringValidator extends NonBlankMaxLengthStringValidator {

	private static final long serialVersionUID = 1584625763507132085L;

	/**
	 * Default constructor.
	 */
	public NonBlankStringValidator() {
		super("The field cannot be empty, blank or contain only whitespaces.", Integer.MAX_VALUE);
	}

	/**
	 * @param errorMessage
	 *            a custom error message to be displayed if a string is not valid
	 */
	public NonBlankStringValidator(final String errorMessage) {
		super(errorMessage, Integer.MAX_VALUE);
	}
}