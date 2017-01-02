package com.workflowconversion.portlet.ui.apptable;

import java.util.Arrays;

import com.workflowconversion.portlet.core.app.Application;
import com.workflowconversion.portlet.core.app.FormField;
import com.workflowconversion.portlet.core.app.ResourceProvider;
import com.workflowconversion.portlet.core.middleware.MiddlewareProvider;

/**
 * Builder for {@link ApplicationsTable}.
 * 
 * @author delagarza
 *
 */
public class ApplicationsTableBuilder {

	private boolean displayEditControls;
	private ResourceProvider applicationProvider;
	private MiddlewareProvider middlewareProvider;
	private FormField[] visibleColumns;

	/**
	 * Sets whether the table will display edit controls.
	 * 
	 * @param displayEditControls
	 *            whether the table will have edit controls.
	 * 
	 * @return {@code this} {@link ApplicationsTableBuilder}.
	 */
	public ApplicationsTableBuilder withDisplayEditControls(final boolean displayEditControls) {
		this.displayEditControls = displayEditControls;
		return this;
	}

	/**
	 * Sets the application provider.
	 * 
	 * @param applicationProvider
	 *            the application provider.
	 * @return {@code this} {@link ApplicationsTableBuilder}.
	 */
	public ApplicationsTableBuilder withApplicationProvider(final ResourceProvider applicationProvider) {
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
	public ApplicationsTableBuilder withMiddlewareProvider(final MiddlewareProvider middlewareProvider) {
		this.middlewareProvider = middlewareProvider;
		return this;
	}

	/**
	 * Sets the visible columns.
	 * 
	 * @param visibleColumns
	 *            a variable sequence of visible columns.
	 * @return {@code this} {@link ApplicationsTableBuilder}.
	 */
	public ApplicationsTableBuilder withVisibleColumns(final Application.Field... visibleColumns) {
		this.visibleColumns = Arrays.copyOf(visibleColumns, visibleColumns.length);
		return this;
	}

	/**
	 * Builds a new applications table.
	 * 
	 * @return a new applications table.
	 */
	public ApplicationsTable newApplicationsTable() {
		return new ApplicationsTable(this.applicationProvider, this.middlewareProvider, this.displayEditControls,
				this.visibleColumns);
	}

}
