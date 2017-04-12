package com.workflowconversion.portlet.core.workflow.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import com.vaadin.server.FileDownloader;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.ui.Button;
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

	ArchiveDownloadWorkflowExporter() {
		// constructor added to make it "package" accessible only
	}

	@Override
	public void export(final Workflow workflow) throws Exception {
		final Button dummyButton = new Button();
		dummyButton.setImmediate(true);

		final FileDownloader fileDownloader = new FileDownloader(createWorkflowStreamResource(workflow));
		fileDownloader.extend(dummyButton);

		dummyButton.click();
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
