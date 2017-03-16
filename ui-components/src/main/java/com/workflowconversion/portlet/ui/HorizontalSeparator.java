package com.workflowconversion.portlet.ui;

import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;

/**
 * Vaadin implementation of a {@code 
 * 
<hr>
 * } html element.
 * 
 * @author delagarza
 *
 */
public class HorizontalSeparator extends Label {

	private static final long serialVersionUID = -3409918267820323183L;

	/**
	 * Constructor.
	 */
	public HorizontalSeparator() {
		super("<hr width='100%'/>", ContentMode.HTML);
		this.setHeight("10px");
	}
}
