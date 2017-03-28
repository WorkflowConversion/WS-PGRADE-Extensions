package com.workflowconversion.portlet.core.workflow.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.workflowconversion.portlet.core.exception.ApplicationException;
import com.workflowconversion.portlet.core.exception.InvalidWorkflowException;
import com.workflowconversion.portlet.core.exception.WorkflowNotFoundException;
import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.core.resource.Queue;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.core.resource.impl.JAXBResourceDatabase;
import com.workflowconversion.portlet.core.search.AssetFinder;
import com.workflowconversion.portlet.core.workflow.Job;
import com.workflowconversion.portlet.core.workflow.Workflow;
import com.workflowconversion.portlet.core.workflow.WorkflowManager;

/**
 * Provides the default implementation to access workflows in the staging area.
 * 
 * Archives to be imported must contain the following elements under each of their "concrete" jobs (i.e., children of
 * the {@code real} element):
 * 
 * <ul>
 * <li>{@code <description key="workflowconversion.appVersion" value="..."/>}
 * <li>{@code <description key="workflowconversion.appName" value="..."/>}
 * </ul>
 * 
 * @author delagarza
 *
 */
@XmlRootElement(name = "workflows")
@XmlAccessorType(XmlAccessType.FIELD)
public class DefaultWorkflowManager implements WorkflowManager {

	private static final String USER_WORKFLOWS_XML_FILE_LOCATION = "user_workflows.xml";
	private static final String PROPERTY_PREFIX = "workflowconversion.";
	private static final String PROPERTY_RESOURCE_NAME = PROPERTY_PREFIX + "resourceName";
	private static final String PROPERTY_RESOURCE_TYPE = PROPERTY_PREFIX + "resourceType";
	private static final String PROPERTY_APP_NAME = PROPERTY_PREFIX + "appName";
	private static final String PROPERTY_APP_VERSION = PROPERTY_PREFIX + "appVersion";
	private static final String PROPERTY_APP_PATH = PROPERTY_PREFIX + "appPath";
	private static final String PROPERTY_QUEUE_NAME = PROPERTY_PREFIX + "queueName";
	private final static String WORKFLOW_ID_FORMAT = "yyyy_MM_dd_hh_mm_ss_SSS";

	private final static Logger LOG = LoggerFactory.getLogger(DefaultWorkflowManager.class);

	@XmlJavaTypeAdapter(WorkflowsXmlAdapter.class)
	private final Map<String, Workflow> workflows;

	@XmlTransient
	private final ReadWriteLock readWriteLock;
	@XmlTransient
	private final File userStagingArea;
	@XmlTransient
	private final File userWorkflowsXmlFile;
	@XmlTransient
	private final AssetFinder assetFinder;

	DefaultWorkflowManager(final File userStagingArea, final AssetFinder assetFinder) {
		Validate.isTrue(userStagingArea.exists(),
				"The user's staging area does not exist, this is probably a coding problem and should be reported.");
		this.userStagingArea = userStagingArea;
		this.assetFinder = assetFinder;
		this.readWriteLock = new ReentrantReadWriteLock(false);
		this.workflows = new TreeMap<String, Workflow>();
		this.userWorkflowsXmlFile = new File(userStagingArea, USER_WORKFLOWS_XML_FILE_LOCATION);
	}

	/**
	 * JAXB serialization requires a default constructor.
	 */
	public DefaultWorkflowManager() {
		this(new File(""), null);
	}

	@Override
	public void init() {
		final Lock writeLock = readWriteLock.writeLock();
		writeLock.lock();
		try {
			loadWorkflowsFromFile_notThreadSafe();
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public Workflow importWorkflow(final File serverSideWorkflowLocation) {
		final Lock writeLock = readWriteLock.writeLock();
		writeLock.lock();
		try {
			final Workflow parsedWorkflow = loadWorkflowFromFile_notThreadSafe(serverSideWorkflowLocation);
			moveWorkflowToStagingArea_notThreadSafe(parsedWorkflow);
			workflows.put(parsedWorkflow.getId(), parsedWorkflow);
			return parsedWorkflow;
		} catch (final IOException e) {
			throw new ApplicationException("Could not import workflow.", e);
		} finally {
			writeLock.unlock();
		}
	}

	private String extractAttribute(final Node node, final String attributeName) {
		return node.getAttributes().getNamedItem(attributeName).getNodeValue();
	}

	private Resource extractResource(final Node node, final Map<String, String> jobProperties) {
		assetFinder.addSearchCriterion(Resource.Field.Name, getProperty(PROPERTY_RESOURCE_NAME, jobProperties));
		assetFinder.addSearchCriterion(Resource.Field.Type, getProperty(PROPERTY_RESOURCE_TYPE, jobProperties));
		return assetFinder.findResource();
	}

	private Application extractApplication(final Node node, final Map<String, String> jobProperties) {
		final Resource resource = extractResource(node, jobProperties);
		assetFinder.addSearchCriterion(Application.Field.Version, getProperty(PROPERTY_APP_VERSION, jobProperties));
		assetFinder.addSearchCriterion(Application.Field.Name, getProperty(PROPERTY_APP_NAME, jobProperties));
		assetFinder.addSearchCriterion(Application.Field.Path, getProperty(PROPERTY_APP_PATH, jobProperties));
		return assetFinder.findApplication(resource);
	}

	private Queue extractQueue(final Node node, final Map<String, String> jobProperties) {
		final Resource resource = extractResource(node, jobProperties);
		assetFinder.addSearchCriterion(Queue.Field.Name, getProperty(PROPERTY_QUEUE_NAME, jobProperties));
		return assetFinder.findQueue(resource);
	}

	private String getProperty(final String propertyName, final Map<String, String> jobProperties) {
		return StringUtils.trimToNull(jobProperties.get(propertyName));
	}

	// returns the attribute values for all <description> elements with a 'key' attribute starting with PROPERTY_PREFIX
	private Map<String, String> extractJobProperties(final Node node) {
		final Map<String, String> propertyMap = new TreeMap<String, String>();
		final NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			final Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE && "description".equals(child.getNodeName())) {
				// it is a description element, now, extract all attributes whose name starts with our prefix
				final Node keyNode = child.getAttributes().getNamedItem("key");
				if (keyNode != null) {
					final String key = keyNode.getNodeValue();
					if (key.startsWith(PROPERTY_PREFIX)) {
						final Node valueNode = child.getAttributes().getNamedItem("value");
						if (valueNode != null) {
							propertyMap.put(key, valueNode.getNodeValue());
						}
					}
				}
			}
		}
		return propertyMap;
	}

	@Override
	public void deleteWorkflow(final Workflow workflow) {
		final Lock writeLock = readWriteLock.writeLock();
		writeLock.lock();
		try {
			final Workflow currentWorkflow = workflows.remove(workflow.getId());
			if (currentWorkflow == null) {
				throw new WorkflowNotFoundException(workflow);
			}
			// in case the passed workflow contains a different location than the
			// current one, we need to delete both locations
			for (final File fileLocation : new File[] { workflow.getLocation(), currentWorkflow.getLocation() }) {
				if (fileLocation.exists()) {
					FileUtils.forceDelete(fileLocation);
				}
			}
		} catch (final IOException e) {
			throw new ApplicationException("Could not delete workflow", e);
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public void saveWorkflow(final Workflow workflow) {
		// we will make a copy of the workflow archive and modify workflow.xml to include
		// the information contained in each of the workflow jobs
		final Lock writeLock = readWriteLock.writeLock();
		try {
			if (workflows.containsKey(workflow.getId())) {
				final Workflow previousWorkflow = workflows.put(workflow.getId(), workflow);
				if (previousWorkflow.getLocation().exists()) {
					FileUtils.forceDelete(previousWorkflow.getLocation());
				}
				saveWorkflowToFile_notThreadSafe(workflow);
			} else {
				throw new WorkflowNotFoundException(workflow);
			}
		} catch (final IOException e) {
			throw new ApplicationException("Could not save workflow", e);
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public void commitChanges() {
		final Lock writeLock = readWriteLock.writeLock();
		writeLock.lock();
		try {
			saveWorkflowsToFile_notThreadSafe();
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public Collection<Workflow> getStagedWorkflows() {
		final Lock readLock = readWriteLock.readLock();
		readLock.lock();
		try {
			return Collections.unmodifiableCollection(this.workflows.values());
		} finally {
			readLock.unlock();
		}
	}

	//////////////////////////////////
	////// NOT THREAD SAFE METHODS
	//////////////////////////////////
	private void saveWorkflowToFile_notThreadSafe(final Workflow workflow) {

	}

	private Workflow loadWorkflowFromFile_notThreadSafe(final File serverSideWorkflowLocation) throws IOException {
		try (final ZipFile zippedWorkflow = new ZipFile(serverSideWorkflowLocation)) {
			// we're just interested in workflow.xml
			final ZipEntry workflowXmlEntry = zippedWorkflow.getEntry("workflow.xml");
			if (workflowXmlEntry == null) {
				throw new InvalidWorkflowException("The file doesn't contain a 'workflow.xml' entry.",
						serverSideWorkflowLocation);
			}
			final XPath xPath = XPathFactory.newInstance().newXPath();
			final InputSource inputSource = new InputSource(zippedWorkflow.getInputStream(workflowXmlEntry));
			final String workflowName;
			try {
				workflowName = (String) xPath.evaluate("/workflow/@name", inputSource, XPathConstants.STRING);
			} catch (final XPathExpressionException e) {
				throw new InvalidWorkflowException("Error while evaluation XPath expression '/workflow/@name'",
						serverSideWorkflowLocation, e);
			}

			final String workflowId = new SimpleDateFormat(WORKFLOW_ID_FORMAT).format(new Date());
			final Workflow parsedWorkflow = new Workflow();
			parsedWorkflow.setId(workflowId);
			parsedWorkflow.setName(workflowName);
			parsedWorkflow.setLocation(serverSideWorkflowLocation);

			final NodeList jobNodeList;
			try {
				jobNodeList = (NodeList) xPath.evaluate("/workflow/real/job", inputSource, XPathConstants.NODESET);
			} catch (final XPathExpressionException e) {
				throw new InvalidWorkflowException("Error while evaluation XPath expression '/workflow/real/job'",
						serverSideWorkflowLocation, e);
			}

			for (int i = 0; i < jobNodeList.getLength(); i++) {
				final Node node = jobNodeList.item(i);
				final String jobName = extractAttribute(node, "name");
				final Job parsedJob = new Job(jobName);
				final Map<String, String> jobProperties = extractJobProperties(node);
				assetFinder.clearAllFields();
				parsedJob.setApplication(extractApplication(node, jobProperties));
				parsedJob.setQueue(extractQueue(node, jobProperties));
				parsedJob.setName(jobName);
				parsedWorkflow.addJob(parsedJob);
			}

			zippedWorkflow.close();

			return parsedWorkflow;
		}
	}

	private void loadWorkflowsFromFile_notThreadSafe() {
		try {
			workflows.clear();

			// if the file does not exist, don't load anything and log this event
			if (userWorkflowsXmlFile.exists()) {
				final JAXBContext jaxbContext = JAXBContext.newInstance(JAXBResourceDatabase.class);
				final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

				final DefaultWorkflowManager workflowManager = (DefaultWorkflowManager) jaxbUnmarshaller
						.unmarshal(userWorkflowsXmlFile);

				// copy the contents of the unmarshalled class
				workflows.putAll(workflowManager.workflows);
			} else {
				LOG.info(
						"The xml file in which the user workflows are stored does not exist yet. It will be created once a save operation is performed. File location: "
								+ userWorkflowsXmlFile.getAbsolutePath());
			}
		} catch (final JAXBException e) {
			throw new ApplicationException("Could not load user workflows", e);
		}
	}

	private void saveWorkflowsToFile_notThreadSafe() {
		try {
			final JAXBContext jaxbContext = JAXBContext.newInstance(DefaultWorkflowManager.class);
			final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			jaxbMarshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true);

			jaxbMarshaller.marshal(this, userWorkflowsXmlFile);
		} catch (final JAXBException e) {
			throw new ApplicationException("Could not save user workflows", e);
		}
	}

	private void moveWorkflowToStagingArea_notThreadSafe(final Workflow workflow) throws IOException {
		final File newLocation = new File(userStagingArea, workflow.getId() + ".zip");
		Files.move(workflow.getLocation().toPath(), newLocation.toPath(), StandardCopyOption.REPLACE_EXISTING);
		workflow.setLocation(newLocation);
	}
}
