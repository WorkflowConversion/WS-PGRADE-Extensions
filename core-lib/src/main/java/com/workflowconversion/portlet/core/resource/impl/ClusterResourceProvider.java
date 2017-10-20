package com.workflowconversion.portlet.core.resource.impl;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workflowconversion.portlet.core.SupportedClusters;
import com.workflowconversion.portlet.core.exception.ApplicationException;
import com.workflowconversion.portlet.core.filter.Filter;
import com.workflowconversion.portlet.core.filter.FilterApplicator;
import com.workflowconversion.portlet.core.filter.impl.SimpleFilterFactory;
import com.workflowconversion.portlet.core.middleware.MiddlewareProvider;
import com.workflowconversion.portlet.core.resource.Queue;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.core.resource.ResourceProvider;
import com.workflowconversion.portlet.core.resource.jaxb.JAXBResourceDatabase;
import com.workflowconversion.portlet.core.utils.KeyUtils;

import dci.data.Item;
import dci.data.Middleware;

/**
 * Resource provider for cluster-based providers.
 * 
 * @author delagarza
 *
 */
public class ClusterResourceProvider implements ResourceProvider {

	private static final long serialVersionUID = -3799340787944829350L;
	private final static Logger LOG = LoggerFactory.getLogger(ClusterResourceProvider.class);

	private final Map<String, Resource> resources;

	private final MiddlewareProvider middlewareProvider;
	private final ReadWriteLock readWriteLock;
	private final File jaxbApplicationsXmlFile;
	private volatile boolean hasInitErrors;

	/**
	 * @param middlewareProvider
	 *            the middleware provider.
	 * @param jaxbApplicationsXmlFileLocation
	 *            the location on which the applications will be stored.
	 */
	public ClusterResourceProvider(final MiddlewareProvider middlewareProvider,
			final String jaxbApplicationsXmlFileLocation) {
		this.hasInitErrors = false;
		this.resources = new TreeMap<String, Resource>();
		this.middlewareProvider = middlewareProvider;
		this.jaxbApplicationsXmlFile = new File(jaxbApplicationsXmlFileLocation);
		this.readWriteLock = new ReentrantReadWriteLock(false);
	}

	@Override
	public boolean canAddApplications() {
		return true;
	}

	@Override
	public String getName() {
		return "WS-PGRADE Cluster Middlewares";
	}

	@Override
	public void init() {
		final Lock writeLock = readWriteLock.writeLock();
		writeLock.lock();
		try {
			// FIXME: Also consider resources not present in the xml!
			loadResourcesFromFile_notThreadSafe();
		} catch (final Exception e) {
			hasInitErrors = true;
			LOG.error("The cluster resource provider could not be initialized.", e);
		} finally {
			writeLock.unlock();
		}

	}

	@Override
	public boolean hasInitErrors() {
		return hasInitErrors;
	}

	@Override
	public Collection<Resource> getResources() {
		final Lock readLock = readWriteLock.readLock();
		readLock.lock();
		try {
			return Collections.unmodifiableCollection(resources.values());
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public Resource getResource(final String name, final String type) {
		final Lock readLock = readWriteLock.readLock();
		readLock.lock();
		try {
			return resources.get(KeyUtils.generateResourceKey(name, type));
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public void save() {
		final Lock writeLock = readWriteLock.writeLock();
		writeLock.lock();
		try {
			saveToFile_notThreadSafe();
		} finally {
			writeLock.unlock();
		}
	}

	// ###### LOAD/SAVE METHODS ######
	// poor naming convention, but it has to be clear that the load/save methods are to be externally made thread safe
	private void saveToFile_notThreadSafe() {
		if (LOG.isInfoEnabled()) {
			LOG.info("saving applications to " + jaxbApplicationsXmlFile.getAbsolutePath());
		}
		try {
			ensureParentFolderExists_notThreadSafe();
			final JAXBContext jaxbContext = JAXBContext.newInstance(JAXBResourceDatabase.class);
			final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
			jaxbMarshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true);

			final JAXBResourceDatabase resourceDatabase = new JAXBResourceDatabase();
			resourceDatabase.addResources(this.resources.values());
			jaxbMarshaller.marshal(resourceDatabase, jaxbApplicationsXmlFile);
		} catch (final JAXBException | IOException e) {
			throw new ApplicationException("Could not save resources", e);
		}
	}

	private void ensureParentFolderExists_notThreadSafe() throws IOException {
		if (!jaxbApplicationsXmlFile.exists()) {
			final File parentDirectory = jaxbApplicationsXmlFile.getParentFile();
			if (parentDirectory == null) {
				throw new IOException("Invalid location of applications database file! File location: "
						+ jaxbApplicationsXmlFile.getCanonicalPath());
			}
			FileUtils.forceMkdir(parentDirectory);
		}
	}

	// @SuppressWarnings("unchecked")
	private void loadResourcesFromFile_notThreadSafe() {
		if (LOG.isInfoEnabled()) {
			LOG.info("loading applications from " + jaxbApplicationsXmlFile.getAbsolutePath());
		}
		try {
			resources.clear();

			// if the file does not exist, don't load anything and log this event
			if (jaxbApplicationsXmlFile.exists()) {
				final JAXBContext jaxbContext = JAXBContext.newInstance(JAXBResourceDatabase.class);
				final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

				final JAXBResourceDatabase storedResources = (JAXBResourceDatabase) jaxbUnmarshaller
						.unmarshal(jaxbApplicationsXmlFile);

				// check that the stored resources match the available resources
				final Map<String, Item> enabledClusterItemMap = getEnabledClusterItemMap_notThreadSafe();
				for (final Resource storedResource : storedResources.getResources()) {
					final String key = KeyUtils.generate(storedResource);
					final Item enabledItem = enabledClusterItemMap.get(key);
					if (enabledItem != null) {
						final Resource.Builder resourceBuilder = new Resource.Builder();
						resourceBuilder.withApplications(storedResource.getApplications());
						resourceBuilder.withName(storedResource.getName());
						resourceBuilder.withType(storedResource.getType());
						resourceBuilder
								.withQueues(extractQueuesFromItem_notThreadSafe(storedResource.getType(), enabledItem));
						// allow users to add applications to this resource
						resourceBuilder.canModifyApplications(true);
						resources.put(key, resourceBuilder.newInstance());
					} else {
						LOG.warn("The stored resource [name=" + storedResource.getName() + ", type="
								+ storedResource.getType()
								+ "] is not enabled or does not exist on this WS-PGRADE instance.");
					}
				}
			} else {
				LOG.info(
						"The xml file in which the applications are stored does not exist yet. It will be created once a save operation is performed. File location: "
								+ jaxbApplicationsXmlFile.getAbsolutePath());
			}
		} catch (final JAXBException e) {
			throw new ApplicationException("Could not load resources", e);
		}
	}

	private Map<String, Item> getEnabledClusterItemMap_notThreadSafe() {
		// get the enabled cluster middlewares
		final Filter<Middleware> clusterMiddlewareFilter = new ClusterMiddlewareFilter();
		final Collection<Middleware> enabledClusterMiddlewares = FilterApplicator
				.applyFilter(middlewareProvider.getEnabledMiddlewares(), clusterMiddlewareFilter);
		// select the enabled items from the list of enabled middlewares
		final Map<String, Item> enabledClusterItemMap = new TreeMap<String, Item>();
		final Filter<Item> enabledItemFilter = new SimpleFilterFactory().setEnabled(true).newItemFilter();
		for (final Middleware enabledClusterMiddleware : enabledClusterMiddlewares) {
			for (final Item enabledClusterItem : FilterApplicator.applyFilter(enabledClusterMiddleware.getItem(),
					enabledItemFilter)) {
				final String key = KeyUtils.generateResourceKey(enabledClusterItem.getName(),
						enabledClusterMiddleware.getType());
				if (enabledClusterItemMap.put(key, enabledClusterItem) != null) {
					LOG.warn("There is a duplicate enabled middleware item. Middleware type = "
							+ enabledClusterMiddleware.getType() + ", item name =" + enabledClusterItem.getName());
				}
			}
		}
		return enabledClusterItemMap;
	}

	private Collection<Queue> extractQueuesFromItem_notThreadSafe(final String middlewareType, final Item item) {
		final Collection<String> extractedQueueNames;
		switch (SupportedClusters.valueOf(middlewareType)) {
		case lsf:
			extractedQueueNames = item.getLsf().getQueue();
			break;
		case moab:
			extractedQueueNames = item.getMoab().getQueue();
			break;
		case pbs:
			extractedQueueNames = item.getPbs().getQueue();
			break;
		case sge:
			extractedQueueNames = item.getSge().getQueue();
			break;
		default:
			throw new IllegalArgumentException("Cannot handle items of type " + middlewareType);
		}
		final Collection<Queue> extractedQueues = new LinkedList<Queue>();
		for (final String extractedQueueName : extractedQueueNames) {
			final Queue.Builder queueBuilder = new Queue.Builder();
			queueBuilder.withName(extractedQueueName);
			extractedQueues.add(queueBuilder.newInstance());
		}
		return extractedQueues;
	}

	private final static class ClusterMiddlewareFilter implements Filter<Middleware> {
		@Override
		public boolean passes(final Middleware element) {
			return SupportedClusters.isSupported(element.getType());
		}
	}
}
