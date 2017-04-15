package com.workflowconversion.portlet.core.workflow.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import com.workflowconversion.portlet.core.exception.ApplicationException;
import com.workflowconversion.portlet.core.workflow.Workflow;
import com.workflowconversion.portlet.core.workflow.WorkflowExporter;

/**
 * Exports workflows as zip archives that can be downloaded to the user's computer.
 * 
 * @author delagarza
 *
 */
public class ArchiveDownloadWorkflowExporter implements WorkflowExporter {

	private final static Logger LOG = LoggerFactory.getLogger(ArchiveDownloadWorkflowExporter.class);

	final Button dummyButton;
	final Window dialog;

	ArchiveDownloadWorkflowExporter() {
		dummyButton = new Button();
		dialog = new Window();
		initUI();
	}

	private void initUI() {
		dialog.setModal(true);
		dummyButton.setImmediate(true);

		final HorizontalLayout layout = new HorizontalLayout();
		layout.addComponent(dummyButton);

		dialog.setContent(layout);
	}

	@Override
	public void export(final Workflow workflow) throws Exception {
		if (LOG.isInfoEnabled()) {
			LOG.info("Downloading workflow " + workflow);
		}
		final FileDownloader fileDownloader = new FileDownloader(createWorkflowStreamResource(workflow));
		fileDownloader.extend(dummyButton);

		UI.getCurrent().addWindow(dialog);
		dialog.bringToFront();

		dummyButton.click();
		UI.getCurrent().removeWindow(dialog);
	}

	private StreamResource createWorkflowStreamResource(final Workflow workflow) {
		return new StreamResource(new StreamSource() {
			private static final long serialVersionUID = -482825331518184714L;

			@Override
			public InputStream getStream() {

				try {
					return new BufferedInputStream(Files.newInputStream(workflow.getArchivePath()));
				} catch (final IOException e) {
					throw new ApplicationException("Could not download workflow.", e);
				}

			}
		}, "workflow.zip");
	}

}
