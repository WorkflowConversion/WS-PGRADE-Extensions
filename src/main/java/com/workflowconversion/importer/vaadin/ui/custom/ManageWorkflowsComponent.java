package com.workflowconversion.importer.vaadin.ui.custom;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * View to manage imported workflows.
 * 
 * @author delagarza
 */
public class ManageWorkflowsComponent extends CustomComponent {
	private static final long serialVersionUID = 3352772855627143557L;

	/**
	 * Constructor.
	 */
	public ManageWorkflowsComponent() {
		initUI();
	}

	private void initUI() {
		final VerticalLayout layout = new VerticalLayout();
		final Label label = new Label("TODO: write the manage workflow");
		layout.addComponent(label);
		setCompositionRoot(layout);
	}

}
