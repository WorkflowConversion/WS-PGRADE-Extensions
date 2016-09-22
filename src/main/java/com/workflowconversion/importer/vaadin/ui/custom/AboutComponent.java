package com.workflowconversion.importer.vaadin.ui.custom;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Displays the <i>about</i> information for this portlet.
 * 
 * @author delagarza
 *
 */
public class AboutComponent extends CustomComponent {

	private static final long serialVersionUID = 5404418113711845777L;

	public AboutComponent() {
		initUI();
	}

	private void initUI() {
		final VerticalLayout layout = new VerticalLayout();
		final Label label = new Label("TODO: write the about");
		layout.addComponent(label);
		setCompositionRoot(layout);
	}

}
