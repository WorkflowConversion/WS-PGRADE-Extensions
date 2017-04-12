package com.workflowconversion.portlet.core.workflow;

import java.io.File;
import java.util.Collection;

import com.workflowconversion.portlet.core.exception.JobExecutionPropertiesHandlerNotFoundException;

/**
 * Interface containing methods to perform operations with/on workflows.
 * 
 * @author delagarza
 *
 */
public interface WorkflowManager {

	/**
	 * Initializes the workflow manager.
	 */
	public void init();

	/**
	 * Commits changes.
	 */
	public void commitChanges();

	/**
	 * Imports a workflow into the <i>staging</i> area, which is a place on which workflow can be edited before being
	 * fully exported to gUSE.
	 * 
	 * @param serverSideWorkflowLocation
	 *            the location of the file containing the workflow to import.
	 * 
	 * @return the imported workflow.
	 */
	public Workflow importWorkflow(final File serverSideWorkflowLocation);

	/**
	 * Deletes a workflow from the staging area.
	 * 
	 * @param workflow
	 *            the workflow to delete.
	 */
	public void deleteWorkflow(final Workflow workflow);

	/**
	 * Given a workflow, this method returns the collection of jobs whose resource type is not handled.
	 * 
	 * @param workflowthe
	 *            workflow.
	 * @return a collection of jobs from the passed workflow that won't be handled.
	 */
	public Collection<Job> getUnsupportedJobs(final Workflow workflow);

	/**
	 * Saves changes.
	 * 
	 * @param workflow
	 *            the workflow save.
	 * @throws JobExecutionPropertiesHandlerNotFoundException
	 *             if the workflow contains jobs that cannot be handled.
	 */
	public void saveWorkflow(final Workflow workflow) throws JobExecutionPropertiesHandlerNotFoundException;

	/**
	 * Obtains a collection of all workflows in the staging area for the requesting user.
	 * 
	 * @return a collection with all imported workflows.
	 */
	public Collection<Workflow> getStagedWorkflows();
}
