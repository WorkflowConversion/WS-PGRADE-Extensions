package com.workflowconversion.portlet.core.jaxb;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.workflowconversion.portlet.core.app.ComputingResource;

/**
 * JAXB adapter for computing resources.
 * 
 * @author delagarza
 *
 */
public class ComputingResourcesXmlAdapter
		extends XmlAdapter<ComputingResourcesXmlAdapter.ComputingResources, Map<String, ComputingResource>> {

	@Override
	public Map<String, ComputingResource> unmarshal(
			final ComputingResourcesXmlAdapter.ComputingResources computingResources) throws Exception {
		final Map<String, ComputingResource> map = new TreeMap<String, ComputingResource>();
		for (final ComputingResource computingResource : computingResources.computingResources) {
			map.put(computingResource.generateKey(), computingResource);
		}
		return map;
	}

	@Override
	public ComputingResourcesXmlAdapter.ComputingResources marshal(final Map<String, ComputingResource> map)
			throws Exception {
		final ComputingResources computingResources = new ComputingResources();
		computingResources.computingResources.addAll(map.values());
		return computingResources;
	}

	/**
	 * Wrapper for computing resources.
	 * 
	 * @author delagarza
	 *
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class ComputingResources {

		@XmlElement(name = "computingResource")
		private List<ComputingResource> computingResources = new LinkedList<ComputingResource>();
	}
}
