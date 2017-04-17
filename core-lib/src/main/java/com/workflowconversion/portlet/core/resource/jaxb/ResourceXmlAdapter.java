package com.workflowconversion.portlet.core.resource.jaxb;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.core.resource.Resource;

/**
 * XML adapter for {@link Resource}.
 * 
 * @author delagarza
 *
 */
public class ResourceXmlAdapter extends XmlAdapter<ResourceXmlAdapter.AdaptedResource, Resource> {

	@Override
	public Resource unmarshal(final AdaptedResource adaptedResource) throws Exception {
		final Resource.Builder resourceBuilder = new Resource.Builder();
		resourceBuilder.withName(adaptedResource.name);
		resourceBuilder.withType(adaptedResource.type);
		resourceBuilder.withApplications(adaptedResource.applications);
		// no need to set queues, this is only for applications...
		// we assume that this resource will be able to add applications, since we're using JAXB
		// to store applications that were added by the user.
		resourceBuilder.canModifyApplications(true);
		return resourceBuilder.newInstance();
	}

	@Override
	public AdaptedResource marshal(final Resource resource) throws Exception {
		final AdaptedResource adaptedResource = new AdaptedResource();
		adaptedResource.name = resource.getName();
		adaptedResource.type = resource.getType();
		for (final Application application : resource.getApplications()) {
			adaptedResource.applications.add(application);
		}
		return adaptedResource;
	}

	/**
	 * Class to assist JAXB marshall/unmarshalling of resources.
	 * 
	 * @author delagarza
	 *
	 */
	@XmlRootElement(name = "resource")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class AdaptedResource {
		@XmlAttribute
		private String type;
		@XmlAttribute
		private String name;

		@XmlElement(name = "application")
		private final List<Application> applications = new LinkedList<Application>();
	}
}
