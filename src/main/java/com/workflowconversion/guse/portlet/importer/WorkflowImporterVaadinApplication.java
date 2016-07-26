package com.workflowconversion.guse.portlet.importer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.Application;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.Notification;

public class WorkflowImporterVaadinApplication extends Application {

	private static final long serialVersionUID = -1691439006632825854L;
	protected final Logger logger = LoggerFactory
			.getLogger(WorkflowImporterVaadinApplication.class);

	public WorkflowImporterVaadinApplication() {
		logger.info("constructor");
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub
		logger.info("init()");
		final Window mainWindow = new Window("V6tm1 Application");
		Label label = new Label("Hello Vaadin!");
		mainWindow.addComponent(label);
		Button button = new Button("click me!");
		button.addListener(new ClickListener() {

			public void buttonClick(ClickEvent event) {
				logger.info("buttonClick()");
				mainWindow.showNotification(new Notification("Notification",
						"description", Notification.TYPE_HUMANIZED_MESSAGE));
			}
		});

		mainWindow.addComponent(button);

		setMainWindow(mainWindow);
	}
}
