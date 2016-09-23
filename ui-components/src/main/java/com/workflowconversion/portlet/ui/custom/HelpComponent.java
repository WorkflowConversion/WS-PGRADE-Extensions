package com.workflowconversion.portlet.ui.custom;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Displays help for this portlet.
 * 
 * @author delagarza
 *
 */
public class HelpComponent extends CustomComponent {
	private static final long serialVersionUID = 6121395631165183604L;

	public HelpComponent() {
		initUI();
	}

	private void initUI() {
		final VerticalLayout layout = new VerticalLayout();
		final Label label = new Label("TODO: write the help");
		layout.addComponent(label);
		setCompositionRoot(layout);
	}

}
