package com.workflowconversion.portlet.core.app;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.workflowconversion.portlet.core.jaxb.ApplicationsXmlAdapter;

/**
 * Class representing a computing resource, such as a computing cluster.
 * 
 * {@link ComputingResource} classes contain a list of {@link Application} and a list of queues.
 * 
 * @author delagarza
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ComputingResource implements Serializable {

	private static final long serialVersionUID = -2174466858733103521L;

	// i.e. unicore, moab, lsf, etc.
	@XmlAttribute
	private String type;
	@XmlAttribute
	private String name;
	@XmlJavaTypeAdapter(ApplicationsXmlAdapter.class)
	private Map<String, Application> applications = new TreeMap<String, Application>();
	@XmlElementWrapper(name = "queues")
	@XmlElement(name = "queue")
	private Set<String> queues = new TreeSet<String>();

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type
	 *            the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the applications
	 */
	public Collection<Application> getApplications() {
		return applications.values();
	}

	/**
	 * @param application
	 *            application to add.
	 */
	public void addApplication(final Application application) {
		this.applications.put(application.generateKey(), application);
	}

	/**
	 * @param application
	 *            the application to remove.
	 */
	public void removeApplication(final Application application) {
		this.applications.remove(application.generateKey());
	}

	/**
	 * @return the queues
	 */
	public Set<String> getQueues() {
		return new TreeSet<String>(queues);
	}

	/**
	 * @param queue
	 *            the queue to add.
	 */
	public void addQueue(final String queue) {
		queues.add(queue);
	}

	/**
	 * @param queue
	 *            the queue to remove.
	 */
	public void removeQueue(final String queue) {
		queues.remove(queue);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ComputingResource [type=" + type + ", name=" + name + ", applications=" + applications + ", queues="
				+ queues + "]";
	}

	/**
	 * Generates a key using the relevant fields.
	 * 
	 * @return
	 */
	public String generateKey() {
		return "type=" + type + "_name=" + name;
	}
}
