package com.workflowconversion.portlet.ui.workflow;

import java.util.Map;

import org.apache.commons.lang.Validate;

import com.vaadin.ui.Accordion;
import com.vaadin.ui.TabSheet.Tab;
import com.vaadin.ui.VerticalLayout;
import com.workflowconversion.portlet.core.resource.Application;
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
	private final Accordion workflowDetailsAccordion;

	/**
	 * @param workflow
	 *            the workflow to display.
	 * @param applicationMap
	 *            the map of all available applications.
	 */
	public WorkflowView(final Workflow workflow, final Map<String, Application> applicationMap) {
		Validate.notNull(workflow,
				"workflow cannot be null. This seems to be a coding problem and should be reported.");
		Validate.notNull(applicationMap,
				"applications cannot be null. This seems to be a coding problem and should be reported.");
		this.workflowId = workflow.getId();
		this.workflowName = workflow.getName();
		this.workflowDetailsAccordion = new Accordion();
		initUI(workflow, applicationMap);
	}

	private void initUI(final Workflow workflow, final Map<String, Application> applicationMap) {
		for (final Job job : workflow.getJobs()) {
			workflowDetailsAccordion.addTab(new JobView(job, applicationMap));
		}

		addComponent(workflowDetailsAccordion);
	}

	/**
	 * @return the workflow, as configured in this view.
	 */
	public Workflow getWorkflow() {
		final Workflow workflow = new Workflow(workflowId);
		workflow.setName(workflowName);
		for (int i = 0; i < workflowDetailsAccordion.getComponentCount(); i++) {
			final Tab tab = workflowDetailsAccordion.getTab(i);
			final JobView jobView = (JobView) tab.getComponent();
			workflow.addJob(jobView.getJob());
		}
		return workflow;
	}
}
