package com.workflowconversion.portlet.ui.table;

/**
 * 
 * @author delagarza
 *
 * @param <T>
 */
public interface Wrapper<T> {

	/**
	 * Gets the wrapped element.
	 * 
	 * @return the warpped element.
	 */
	public T get();
}
