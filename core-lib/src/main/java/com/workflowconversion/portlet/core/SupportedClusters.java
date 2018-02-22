package com.workflowconversion.portlet.core;

import org.apache.commons.lang3.StringUtils;

/**
 * Supported clusters enumeration.
 * 
 * @author delagarza
 *
 */
public enum SupportedClusters {
	pbs, lsf, sge, moab;

	public static boolean isSupported(final String resourceType) {
		if (StringUtils.isNotBlank(resourceType)) {
			try {
				valueOf(StringUtils.trimToEmpty(resourceType.toLowerCase()));
				return true;
			} catch (final IllegalArgumentException e) {

			}
		}
		return false;
	}
}
