package com.workflowconversion.portlet.core.resource.impl.jaxb;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.workflowconversion.portlet.core.resource.Application;

/**
 * JAXB adaptor for applications.
 * 
 * @author delagarza
 */
public class ApplicationsXmlAdapter extends XmlAdapter<ApplicationsXmlAdapter.Applications, Map<String, Application>> {

	@Override
	public Map<String, Application> unmarshal(final Applications apps) throws Exception {
		final Map<String, Application> map = new TreeMap<String, Application>();
		for (final Application app : apps.applications) {
			map.put(app.getId(), app);
		}
		return map;
	}

	@Override
	public Applications marshal(final Map<String, Application> map) throws Exception {
		final Applications apps = new Applications();
		apps.applications.addAll(map.values());
		return apps;
	}

	/**
	 * Wrapper class for applications.
	 * 
	 * @author delagarza
	 */
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Applications {
		@XmlElement(name = "application")
		private List<Application> applications = new LinkedList<Application>();
	}
}
