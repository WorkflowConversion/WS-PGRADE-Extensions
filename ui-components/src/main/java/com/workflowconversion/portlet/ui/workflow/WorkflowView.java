package com.workflowconversion.portlet.ui.workflow;

import java.nio.file.Path;
import java.util.Collection;

import org.apache.commons.lang3.Validate;

import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.workflowconversion.portlet.core.resource.ResourceProvider;
import com.workflowconversion.portlet.core.workflow.Job;
import com.workflowconversion.portlet.core.workflow.Workflow;

/**
 * Class that displays a single workflow.
 * 
 * @author delagarza
 *
 */
public class WorkflowView extends VerticalLayout {

	private static final long serialVersionUID = 3843347539780676302L;

	private final String workflowId;
	private final String workflowName;
	private final Path archivePath;

	/**
	 * @param workflow
	 *            the workflow to display.
	 * @param applicationMap
	 *            the map of all available applications.
	 */
	public WorkflowView(final Workflow workflow, final Collection<ResourceProvider> resourceProviders) {
		Validate.notNull(workflow,
				"workflow cannot be null. This seems to be a coding problem and should be reported.");
		Validate.notNull(resourceProviders,
				"resourceProviders cannot be null. This seems to be a coding problem and should be reported.");
		this.workflowId = workflow.getId();
		this.workflowName = workflow.getName();
		this.archivePath = workflow.getArchivePath();
		initUI(workflow, resourceProviders);
	}

	private void initUI(final Workflow workflow, final Collection<ResourceProvider> resourceProviders) {
		for (final Job job : workflow.getJobs()) {
			final Panel jobPanel = new Panel(job.getName(), new JobView(job, resourceProviders));
			addComponent(jobPanel);
		}
	}

	/**
	 * @return the workflow, as configured in this view.
	 */
	public Workflow getWorkflow() {
		final Workflow workflow = new Workflow();
		workflow.setId(workflowId);
		workflow.setName(workflowName);
		workflow.setArchivePath(archivePath);
		for (int i = 0; i < getComponentCount(); i++) {
			final Panel jobViewPanel = (Panel) getComponent(i);
			final JobView jobView = (JobView) jobViewPanel.getContent();
			workflow.addJob(jobView.getJob());
		}
		return workflow;
	}
}
