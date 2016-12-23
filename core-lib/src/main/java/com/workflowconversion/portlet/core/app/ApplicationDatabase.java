package com.workflowconversion.portlet.core.app;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.workflowconversion.portlet.core.jaxb.ComputingResourcesXmlAdapter;

/**
 * Represents all of the available computing resources together with their applications.
 * 
 * @author delagarza
 *
 */
@XmlRootElement(name = "appDB")
@XmlAccessorType(XmlAccessType.FIELD)
public class ApplicationDatabase implements Serializable {

	private static final long serialVersionUID = -1654143705161576414L;

	@XmlJavaTypeAdapter(ComputingResourcesXmlAdapter.class)
	private Map<String, ComputingResource> computingResources = new TreeMap<String, ComputingResource>();

	/**
	 * @return the computingResources
	 */
	public Collection<ComputingResource> getComputingResources() {
		return computingResources.values();
	}

	public void addComputingResource(final ComputingResource computingResource) {
		computingResources.put(computingResource.generateKey(), computingResource);
	}

	public void removeComputingResource(final ComputingResource computingResource) {
		computingResources.remove(computingResource.generateKey());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ApplicationDatabase [computingResources=" + computingResources + "]";
	}

}
