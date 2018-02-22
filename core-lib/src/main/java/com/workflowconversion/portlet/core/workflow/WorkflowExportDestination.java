package com.workflowconversion.portlet.core.workflow;

/**
 * Enum with possible workflow export destinations.
 * 
 * @author delagarza
 *
 */
public enum WorkflowExportDestination {

	/**
	 * WS-PGRADE local repository.
	 */
	// TODO: this is not supported, yet...
	// given the current ws-pgrade state, it doesn't seem possible to do this,
	// because the repository/storage services seem to use hardcoded paths
	LocalRepository("Local WS-PGRADE repository"),
	/**
	 * Downloadable archive.
	 */
	Archive("Download to your computer");

	private final String longCaption;

	private WorkflowExportDestination(final String longCaption) {
		this.longCaption = longCaption;
	}

	/**
	 * @return the long caption.
	 */
	public String getLongCaption() {
		return longCaption;
	}
}
