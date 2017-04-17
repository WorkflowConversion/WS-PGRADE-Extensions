package com.workflowconversion.portlet.core.resource.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.workflowconversion.portlet.core.resource.Application;

/**
 * JAXB xml adapter for applications.
 * 
 * @author delagarza
 *
 */
public class ApplicationXmlAdapter extends XmlAdapter<ApplicationXmlAdapter.AdaptedApplication, Application> {

	@Override
	public Application unmarshal(final AdaptedApplication adaptedApplication) throws Exception {
		final Application.Builder applicationBuilder = new Application.Builder();
		applicationBuilder.withName(adaptedApplication.name);
		applicationBuilder.withVersion(adaptedApplication.version);
		applicationBuilder.withPath(adaptedApplication.path);
		applicationBuilder.withDescription(adaptedApplication.description);
		return applicationBuilder.newInstance();
	}

	@Override
	public AdaptedApplication marshal(final Application application) throws Exception {
		final AdaptedApplication adaptedApplication = new AdaptedApplication();
		adaptedApplication.name = application.getName();
		adaptedApplication.version = application.getVersion();
		adaptedApplication.path = application.getPath();
		adaptedApplication.description = application.getDescription();
		return adaptedApplication;
	}

	/**
	 * Wrapper class for applications.
	 */
	@XmlRootElement(name = "application")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class AdaptedApplication {
		@XmlAttribute
		private String name;
		@XmlAttribute
		private String version;
		@XmlAttribute
		private String path;
		@XmlAttribute
		private String description;
	}

}
