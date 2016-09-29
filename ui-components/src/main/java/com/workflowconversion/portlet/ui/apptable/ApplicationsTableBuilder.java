package com.workflowconversion.portlet.ui.apptable;

import com.workflowconversion.portlet.core.app.ApplicationProvider;
import com.workflowconversion.portlet.core.middleware.MiddlewareProvider;

/**
 * Builder for {@link ApplicationsTable}.
 * 
 * @author delagarza
 *
 */
public class ApplicationsTableBuilder {

	private boolean withEditControls;
	private ApplicationProvider applicationProvider;
	private MiddlewareProvider middlewareProvider;

	/**
	 * Sets whether the table to be built will have edit controls.
	 * 
	 * @param withEditControls
	 *            whether edit controls are desired.
	 * @return {@code this} {@link ApplicationsTableBuilder}.
	 */
	public ApplicationsTableBuilder setWithEditControls(final boolean withEditControls) {
		this.withEditControls = withEditControls;
		return this;
	}

	/**
	 * Sets the application provider.
	 * 
	 * @param applicationProvider
	 *            the application provider.
	 * @return {@code this} {@link ApplicationsTableBuilder}.
	 */
	public ApplicationsTableBuilder setApplicationProvider(final ApplicationProvider applicationProvider) {
		this.applicationProvider = applicationProvider;
		return this;
	}

	/**
	 * Sets the middleware provider.
	 * 
	 * @param middlewareProvider
	 *            the middleware provider.
	 * @return {@code this} {@link ApplicationsTableBuilder}.
	 */
	public ApplicationsTableBuilder setMiddlewareProvider(final MiddlewareProvider middlewareProvider) {
		this.middlewareProvider = middlewareProvider;
		return this;
	}

	/**
	 * Builds a new applications table.
	 * 
	 * @return a new applications table.
	 */
	public ApplicationsTable newApplicationsTable() {
		return new ApplicationsTable(this.applicationProvider, this.middlewareProvider, this.withEditControls);
	}

}
