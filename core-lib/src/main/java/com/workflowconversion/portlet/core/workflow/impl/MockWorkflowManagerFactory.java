package com.workflowconversion.portlet.core.workflow.impl;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.workflowconversion.portlet.core.resource.ResourceProvider;
import com.workflowconversion.portlet.core.user.PortletUser;
import com.workflowconversion.portlet.core.workflow.Job;
import com.workflowconversion.portlet.core.workflow.Workflow;
import com.workflowconversion.portlet.core.workflow.WorkflowManager;
import com.workflowconversion.portlet.core.workflow.WorkflowManagerFactory;

/**
 * Mock factory. Useful for testing purposes.
 * 
 * @author delagarza
 *
 */
public class MockWorkflowManagerFactory implements WorkflowManagerFactory {

	@Override
	public WorkflowManagerFactory withPortletUser(final PortletUser portletUser) {
		return this;
	}

	@Override
	public WorkflowManagerFactory withResourceProviders(final Collection<ResourceProvider> resourceProviders) {
		return this;
	}

	@Override
	public WorkflowManager newInstance() {
		return new MockWorkflowProvider();
	}

	private static class MockWorkflowProvider implements WorkflowManager {
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
		public Workflow importWorkflow(final File serverSideWorkflowLocation) {
			final Workflow newWorkflow = addNewWorkflow();
			newWorkflow.setArchivePath(serverSideWorkflowLocation.toPath());
			return newWorkflow;
		}

		private Workflow addNewWorkflow() {
			final Workflow newWorkflow = new Workflow();
			newWorkflow.setId(Integer.toString(CURRENT_WF_ID));
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
		public Collection<Workflow> getImportedWorkflows() {
			return workflows.values();
		}

		@Override
		public Collection<Job> getUnsupportedJobs(final Workflow workflow) {
			return Collections.<Job>emptyList();
		}

		@Override
		public void commitChanges() {
			// nop
		}

		@Override
		public void init() {
			// nop
		}
	}

}
