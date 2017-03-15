package com.workflowconversion.portlet.core.resource.impl.jaxb;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.workflowconversion.portlet.core.resource.Resource;

/**
 * JAXB adapter for computing resources.
 * 
 * @author delagarza
 *
 */
public class ResourcesXmlAdapter extends XmlAdapter<ResourcesXmlAdapter.Resources, Map<String, Resource>> {

	@Override
	public Map<String, Resource> unmarshal(final ResourcesXmlAdapter.Resources resources) throws Exception {
		final Map<String, Resource> map = new TreeMap<String, Resource>();
		for (final Resource resource : resources.resources) {
			map.put(resource.generateKey(), resource);
		}
		return map;
	}

	@Override
	public ResourcesXmlAdapter.Resources marshal(final Map<String, Resource> map) throws Exception {
		final Resources computingResources = new Resources();
		computingResources.resources.addAll(map.values());
		return computingResources;
	}

	/**
	 * Wrapper for computing resources.
	 * 
	 * @author delagarza
	 *
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Resources {

		@XmlElementWrapper(name = "resources")
		@XmlElement(name = "resource")
		private final List<Resource> resources = new LinkedList<Resource>();
	}
}
