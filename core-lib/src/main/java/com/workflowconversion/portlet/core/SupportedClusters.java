package com.workflowconversion.portlet.core;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

/**
 * Supported clusters enumeration.
 * 
 * @author delagarza
 *
 */
public enum SupportedClusters {
	pbs, lsf, sge, moab;

	public static boolean isSupported(final String resourceType) {
		Validate.isTrue(StringUtils.isNotBlank(resourceType),
				"resourceType cannot be null, empty or contain only whitespaces; this seems to be a coding problem and should be reported.");
		try {
			valueOf(resourceType);
			return true;
		} catch (final IllegalArgumentException e) {

		}
		return false;
	}
}
