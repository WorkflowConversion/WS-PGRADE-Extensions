package com.workflowconversion.portlet.core.exception;

/**
 * Exception thrown when an element, whose type cannot be known at compile time, is duplicated in a table.
 * 
 * @author delagarza
 *
 */
public class DuplicateElementException extends ApplicationException {
	private static final long serialVersionUID = -3121248684349706822L;

	/**
	 * @param duplicateKey
	 *            the duplicate key.
	 */
	public DuplicateElementException(final String duplicateKey) {
		super("The key '" + duplicateKey + "' already exists in this table.");
	}
}
