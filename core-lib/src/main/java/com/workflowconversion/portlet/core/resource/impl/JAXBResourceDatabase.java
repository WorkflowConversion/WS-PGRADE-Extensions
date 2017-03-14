package com.workflowconversion.portlet.core.resource.impl;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workflowconversion.portlet.core.exception.ApplicationException;
import com.workflowconversion.portlet.core.exception.DuplicateResourceException;
import com.workflowconversion.portlet.core.exception.ProviderNotEditableException;
import com.workflowconversion.portlet.core.exception.ResourceNotFoundException;
import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.core.resource.Queue;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.core.resource.ResourceProvider;

/**
 * Represents all of the available computing resources together with their applications.
 * 
 * @author delagarza
 *
 */
@XmlRootElement(name = "resourcesDB")
@XmlAccessorType(XmlAccessType.FIELD)
public class JAXBResourceDatabase implements ResourceProvider {

	private final static long serialVersionUID = -1654143705161576414L;
	private final static Logger LOG = LoggerFactory.getLogger(JAXBResourceDatabase.class);

	@XmlElementWrapper(name = "resources")
	@XmlElement(name = "resource")
	private final Set<Resource> resources;

	// we don't need these members to be serialized/deserialized
	@XmlTransient
	private final ReadWriteLock readWriteLock;
	@XmlTransient
	private final File xmlFile;

	/**
	 * Constructor.
	 * 
	 * @param xmlFileLocation
	 *            location of the xml file containing the resources.
	 */
	public JAXBResourceDatabase(final String xmlFileLocation) {
		Validate.isTrue(StringUtils.isNotBlank(xmlFileLocation),
				"xmlFileLocation cannot be null, empty or contain only whitespaces");
		this.xmlFile = new File(xmlFileLocation);
		this.readWriteLock = new ReentrantReadWriteLock();
		this.resources = new TreeSet<Resource>();
	}

	/**
	 * Default constructor. Quite handy, since JAXB requires a constructor without arguments.
	 */
	public JAXBResourceDatabase() {
		this("resources_default_location.xml");
	}

	@Override
	public boolean isEditable() {
		return true;
	}

	@Override
	public String getName() {
		return "WS-PGRADE Resource Database";
	}

	@Override
	public boolean needsInit() {
		return true;
	}

	@Override
	public Collection<Resource> getResources() {
		final Lock readLock = readWriteLock.readLock();
		readLock.lock();
		try {
			return Collections.unmodifiableCollection(resources);
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public void addResource(final Resource resource) {
		Validate.notNull(resource, "resource cannot be null");
		final Lock writeLock = readWriteLock.writeLock();
		writeLock.lock();
		try {
			if (!resources.contains(resource)) {
				resources.add(resource);
			} else {
				throw new DuplicateResourceException(resource);
			}
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public void removeResource(final Resource resource) {
		Validate.notNull(resource, "resource cannot be null");
		final Lock writeLock = readWriteLock.writeLock();
		writeLock.lock();
		try {
			if (!resources.remove(resource)) {
				throw new ResourceNotFoundException(resource);
			}
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public void removeAllResources() throws ProviderNotEditableException {
		final Lock writeLock = readWriteLock.writeLock();
		writeLock.lock();
		try {
			resources.clear();
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public void saveResource(final Resource resource) throws ProviderNotEditableException {
		Validate.notNull(resource, "resource cannot be null");
		final Lock writeLock = readWriteLock.writeLock();
		writeLock.lock();
		try {
			if (resources.contains(resource)) {
				resources.remove(resource);
				resources.add(resource);
			} else {
				throw new ResourceNotFoundException(resource);
			}
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public void commitChanges() throws ProviderNotEditableException {
		final Lock writeLock = readWriteLock.writeLock();
		writeLock.lock();
		try {
			saveToFile_notThreadSafe();
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public boolean containsResource(final Resource resource) {
		Validate.notNull(resource, "resource cannot be null");
		final Lock readLock = readWriteLock.readLock();
		readLock.lock();
		try {
			return resources.contains(resource);
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public void init() {
		final Lock writeLock = readWriteLock.writeLock();
		writeLock.lock();
		try {
			loadFromFile_notThreadSafe();
		} finally {
			writeLock.unlock();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final Lock readLock = readWriteLock.readLock();
		readLock.lock();
		try {
			return "ResourceDatabase [resources=" + resources + "]";
		} finally {
			readLock.unlock();
		}
	}

	// ###### LOAD/SAVE METHODS ######
	// poor naming convention, but it has to be clear that the load/save methods are to be externally made thread safe
	private void saveToFile_notThreadSafe() {
		try {
			final JAXBContext jaxbContext = JAXBContext.newInstance(JAXBResourceDatabase.class);
			final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			jaxbMarshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true);

			jaxbMarshaller.marshal(this, xmlFile);
		} catch (final JAXBException e) {
			throw new ApplicationException("Could not save resources", e);
		}
	}

	private void loadFromFile_notThreadSafe() {
		try {
			resources.clear();

			// if the file does not exist, don't load anything and log this event
			if (xmlFile.exists()) {
				final JAXBContext jaxbContext = JAXBContext.newInstance(JAXBResourceDatabase.class);
				final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

				final JAXBResourceDatabase resourceDatabase = (JAXBResourceDatabase) jaxbUnmarshaller
						.unmarshal(xmlFile);

				// copy the contents of the unmarshalled class
				resources.addAll(resourceDatabase.resources);
				// go through all applications, queues and set their owning resource
				for (final Resource resource : resources) {
					for (final Application application : resource.getApplications()) {
						application.setResource(resource);
					}
					for (final Queue queue : resource.getQueues()) {
						queue.setResource(resource);
					}
				}
			} else {
				LOG.warn("The xml file in which the resources/applications are stored does not exist. File location: "
						+ xmlFile.getAbsolutePath());
			}
		} catch (final JAXBException e) {
			throw new ApplicationException("Could not load resources", e);
		}
	}
	// ###### END OF LOAD/SAVE METHODS #####
}