package com.workflowconversion.portlet.core.workflow;

import java.io.File;
import java.util.Collection;

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
	 * Saves changes.
	 * 
	 * @param workflow
	 *            the workflow save.
	 */
	public void saveWorkflow(final Workflow workflow);

	/**
	 * Obtains a collection of all workflows in the staging area for the requesting user.
	 * 
	 * @return a collection with all imported workflows.
	 */
	public Collection<Workflow> getStagedWorkflows();
}
