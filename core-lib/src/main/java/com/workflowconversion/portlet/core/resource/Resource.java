package com.workflowconversion.portlet.core.resource;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.workflowconversion.portlet.core.exception.ApplicationNotFoundException;
import com.workflowconversion.portlet.core.exception.DuplicateApplicationException;
import com.workflowconversion.portlet.core.exception.DuplicateQueueException;
import com.workflowconversion.portlet.core.exception.QueueNotFoundException;
import com.workflowconversion.portlet.core.resource.impl.jaxb.ApplicationsXmlAdapter;

/**
 * Class representing a computing resource, such as a computing cluster.
 * 
 * {@link Resource} classes contain a list of {@link Application} and a list of queues.
 * 
 * @author delagarza
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Resource implements Serializable, HasKey {

	private static final long serialVersionUID = -2174466858733103521L;

	// i.e. unicore, moab, lsf, etc.
	@XmlAttribute
	private String type;
	@XmlAttribute
	private String name = "";
	@XmlJavaTypeAdapter(ApplicationsXmlAdapter.class)
	private final Map<String, Application> applications = new TreeMap<String, Application>();
	@XmlElementWrapper(name = "queues")
	@XmlElement(name = "queue")
	private final Set<Queue> queues = new TreeSet<Queue>();

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
	public void setType(final String type) {
		Validate.isTrue(StringUtils.isNotBlank(type),
				"type cannot be null, empty or contain only whitespace characters.");
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
	public void setName(final String name) {
		Validate.isTrue(StringUtils.isNotBlank(name),
				"name cannot be null, empty or contain only whitespace characters.");
		this.name = name;
	}

	/**
	 * @return the applications
	 */
	public Collection<Application> getApplications() {
		return new LinkedList<Application>(applications.values());
	}

	/**
	 * @param application
	 *            application to add.
	 * @throws DuplicateApplicationException
	 *             if an application with the same name, version, path already exists in this resource.
	 */
	public void addApplication(final Application application) throws DuplicateApplicationException {
		Validate.notNull(application, "application cannot be null");
		final String key = application.generateKey();
		if (!applications.containsKey(key)) {
			application.setResource(this);
			applications.put(key, application);
		} else {
			throw new DuplicateApplicationException(application);
		}
	}

	/**
	 * @param application
	 *            the application to remove.
	 * @throws ApplicationNotFoundException
	 *             if the application does not already exist.
	 */
	public void removeApplication(final Application application) {
		Validate.notNull(application, "application cannot be null");
		final String key = application.generateKey();
		if (applications.remove(key) != null) {
			application.setResource(null);
		} else {
			// did not exist!
			throw new ApplicationNotFoundException(application);
		}
	}

	/**
	 * Clears this resource of its applications.
	 */
	public void removeAllApplications() {
		for (final Application application : applications.values()) {
			application.setResource(null);
		}
		applications.clear();
	}

	/**
	 * Saves changes done to the application.
	 * 
	 * @param application
	 *            the edited application to be saved.
	 * @throws ApplicationNotFoundException
	 *             if the application does not already exist.
	 */
	public void saveApplication(final Application application) throws ApplicationNotFoundException {
		Validate.notNull(application, "application cannot be null");
		final String key = application.generateKey();
		if (applications.containsKey(key)) {
			application.setResource(this);
			applications.put(key, application);
		} else {
			throw new ApplicationNotFoundException(application);
		}
	}

	/**
	 * Returns whether this resource contains the given application.
	 * 
	 * @param application
	 *            the application.
	 * @return {@code true} if this resource contains the passed application.
	 */
	public boolean containsApplication(final Application application) {
		Validate.notNull(application, "application cannot be null");
		return applications.containsKey(application.generateKey());
	}

	/**
	 * @return the queues
	 */
	public Collection<Queue> getQueues() {
		return new LinkedList<Queue>(queues);
	}

	/**
	 * Adds a queue.
	 * 
	 * @param queue
	 *            the queue to add.
	 * @throws DuplicateQueueException
	 *             if the queue already exists in this resource.
	 */
	public void addQueue(final Queue queue) throws DuplicateQueueException {
		Validate.notNull(queue, "queue cannot be null.");
		final String name = StringUtils.trimToNull(queue.getName());
		Validate.notNull(name, "queue name cannot be null, empty, or contain only whitespace characters.");
		if (queues.contains(queue)) {
			throw new DuplicateQueueException(queue);
		}
		queue.setResource(this);
		queues.add(queue);
	}

	/**
	 * @param queue
	 *            the queue to search for.
	 * @return whether this resource contains the given queue.
	 */
	public boolean containsQueue(final Queue queue) {
		return queues.contains(queue);
	}

	/**
	 * @param queue
	 *            the queue to remove.
	 */
	public void removeQueue(final Queue queue) {
		if (queues.remove(queue)) {
			queue.setResource(null);
		} else {
			throw new QueueNotFoundException(queue);
		}
	}

	/**
	 * Removes all queues from this resource.
	 */
	public void removeAllQueues() {
		for (final Queue queue : queues) {
			queue.setResource(null);
		}
		queues.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Resource [type=" + type + ", name=" + name + ", applications=" + applications + ", queues=" + queues
				+ "]";
	}

	@Override
	public String generateKey() {
		return "type=" + type + "_name=" + name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Resource other = (Resource) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	/**
	 * Enum listing the fields of computing resources.
	 * 
	 * @author delagarza
	 *
	 */
	public static enum Field implements FormField {

		OriginalResource(1, "originalResource", "Original Resource"), Applications(1, "applications",
				"Applications"), Queues(1, "queues", "Queues"), Name(256, "name", "Name"), Type(64, "type", "Type");

		private final int maxLength;
		private final String displayName;
		private final String memberName;

		private Field(final int maxLength, final String memberName, final String displayName) {
			this.maxLength = maxLength;
			this.memberName = memberName;
			this.displayName = displayName;
		}

		@Override
		public int getMaxLength() {
			return maxLength;
		}

		@Override
		public String getMemberName() {
			return memberName;
		}

		@Override
		public String getDisplayName() {
			return displayName;
		}
	}
}
