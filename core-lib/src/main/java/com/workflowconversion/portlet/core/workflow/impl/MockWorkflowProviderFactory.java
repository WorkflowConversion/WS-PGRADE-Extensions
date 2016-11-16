package com.workflowconversion.portlet.core.workflow.impl;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.workflowconversion.portlet.core.user.PortletUser;
import com.workflowconversion.portlet.core.workflow.Workflow;
import com.workflowconversion.portlet.core.workflow.WorkflowProvider;
import com.workflowconversion.portlet.core.workflow.WorkflowProviderFactory;

/**
 * Mock factory. Useful for testing purposes.
 * 
 * @author delagarza
 *
 */
public class MockWorkflowProviderFactory implements WorkflowProviderFactory {

	@Override
	public WorkflowProviderFactory withPortletUser(PortletUser portletUser) {
		return this;
	}

	@Override
	public WorkflowProvider newWorkflowProvider() {
		return new MockWorkflowProvider();
	}

	private static class MockWorkflowProvider implements WorkflowProvider {
		final Map<String, Workflow> workflows;
		private static int CURRENT_WF_ID = 0;

		private MockWorkflowProvider() {
			workflows = new TreeMap<String, Workflow>();
			// create some workflows
			for (int i = 0; i < 5; i++) {
				final Workflow newWorkflow = addNewWorkflow();
				// TODO: add some jobs to the new workflow
			}
		}

		@Override
		public Workflow importToStagingArea(final File serverSideWorkflowLocation) {
			final Workflow newWorkflow = addNewWorkflow();
			newWorkflow.setLocation(serverSideWorkflowLocation);
			return newWorkflow;
		}

		private Workflow addNewWorkflow() {
			final Workflow newWorkflow = new Workflow(Integer.toString(CURRENT_WF_ID));
			newWorkflow.setName("MockWorkflow_" + CURRENT_WF_ID++);
			workflows.put(newWorkflow.getId(), newWorkflow);
			return newWorkflow;
		}

		@Override
		public void deleteWorkflow(final Workflow workflow) {
			workflows.remove(workflow.getId());
		}

		@Override
		public void saveWorkflow(final Workflow workflow) {
			workflows.put(workflow.getId(), workflow);
		}

		@Override
		public Collection<Workflow> getStagedWorkflows() {
			return workflows.values();
		}
	}

}
