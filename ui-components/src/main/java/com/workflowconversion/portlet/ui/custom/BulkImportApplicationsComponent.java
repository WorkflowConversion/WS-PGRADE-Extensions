package com.workflowconversion.portlet.ui.custom;

import com.vaadin.ui.CustomComponent;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * View to import applications using a CSV file.
 * 
 * @author delagarza
 *
 */
public class BulkImportApplicationsComponent extends CustomComponent {
	private static final long serialVersionUID = 1730032422547496883L;

	public BulkImportApplicationsComponent() {
		initUI();
	}

	private void initUI() {
		final VerticalLayout layout = new VerticalLayout();
		final Label label = new Label("TODO: write the bulk import");
		layout.addComponent(label);
		setCompositionRoot(layout);
	}

}
