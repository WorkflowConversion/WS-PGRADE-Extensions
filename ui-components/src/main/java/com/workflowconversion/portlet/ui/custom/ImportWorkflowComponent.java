package com.workflowconversion.portlet.ui.custom;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * Custom component to import workflows.
 * 
 * @author delagarza
 *
 */
public class ImportWorkflowComponent extends CustomComponent {
	private static final long serialVersionUID = -7495441921144579244L;

	/**
	 * Constructor
	 */
	public ImportWorkflowComponent() {
		initUI();
	}

	private void initUI() {
		final VerticalLayout layout = new VerticalLayout();
		final Label label = new Label("TODO: write the import workflow");
		layout.addComponent(label);
		setCompositionRoot(layout);
	}

}
