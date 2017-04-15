package com.workflowconversion.portlet.core.utils;

import org.apache.commons.lang.Validate;

import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.core.resource.Queue;
import com.workflowconversion.portlet.core.resource.Resource;

/**
 * Convenience methods to generate system-wide keys for applications, queues.
 * 
 * @author delagarza
 *
 */
public class SystemWideKeyUtils {

	/**
	 * Generates a system wide key using the given resource and application.
	 * 
	 * @param resource
	 *            the resource.
	 * @param application
	 *            the application.
	 * @return A system wide key that represents the given application.
	 */
	public static String generate(final Resource resource, final Application application) {
		Validate.notNull(resource, "resource cannot be null; this is a coding problem and should be reported.");
		Validate.notNull(application, "application cannot be null; this is a coding problem and should be reported.");
		return resource.generateKey() + '_' + application.generateKey();
	}

	/**
	 * Generates a system wide key using the given resource and queue.
	 * 
	 * @param resource
	 *            the resource.
	 * @param queue
	 *            the queue.
	 * @return A system wide key that represents the given queue.
	 */
	public static String generate(final Resource resource, final Queue queue) {
		Validate.notNull(resource, "resource cannot be null; this is a coding problem and should be reported.");
		Validate.notNull(queue, "queue cannot be null; this is a coding problem and should be reported.");
		return resource.generateKey() + '_' + queue.generateKey();
	}
}
