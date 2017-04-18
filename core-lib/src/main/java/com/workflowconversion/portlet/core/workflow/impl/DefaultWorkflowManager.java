package com.workflowconversion.portlet.core.workflow.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.workflowconversion.portlet.core.exception.ApplicationException;
import com.workflowconversion.portlet.core.exception.InvalidWorkflowException;
import com.workflowconversion.portlet.core.exception.JobNotFoundException;
import com.workflowconversion.portlet.core.exception.WorkflowNotFoundException;
import com.workflowconversion.portlet.core.execution.JobExecutionPropertiesHandler;
import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.core.resource.Queue;
import com.workflowconversion.portlet.core.resource.Resource;
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
@XmlRootElement(name = "userWorkflows")
@XmlAccessorType(XmlAccessType.FIELD)
public class DefaultWorkflowManager implements WorkflowManager {

	private static final String ATTRIBUTE_VALUE = "value";
	private static final String ATTRIBUTE_KEY = "key";
	private static final String NODE_JOB_DESCRIPTION = "description";
	private static final String NODE_JOB_EXECUTE = "execute";
	private static final String ATTRIBUTE_JOB_NAME = "name";
	private static final String XPATH_ALL_JOBS_SELECTOR = "/workflow/real/job";
	private static final String XPATH_SINGLE_JOB_SELECTOR = "/workflow/real/job[@name=''{0}'']";
	private static final String XPATH_WORKFLOW_NAME = "/workflow/@name";
	private static final String PATH_WORKFLOW_XML = "/workflow.xml";
	private static final String USER_WORKFLOWS_ARCHIVE_SUFFIX = ".zip";
	private static final String USER_WORKFLOWS_XML_FILE_LOCATION = "user_workflows.xml";
	private static final String PROPERTY_CURRENT_PARAMETERS = "params";
	private static final String PROPERTY_PREFIX = "workflowconversion.";
	private static final String PROPERTY_RESOURCE_NAME = PROPERTY_PREFIX + "resourceName";
	private static final String PROPERTY_RESOURCE_TYPE = PROPERTY_PREFIX + "resourceType";
	private static final String PROPERTY_APP_NAME = PROPERTY_PREFIX + "appName";
	private static final String PROPERTY_APP_VERSION = PROPERTY_PREFIX + "appVersion";
	private static final String PROPERTY_APP_PATH = PROPERTY_PREFIX + "appPath";
	private static final String PROPERTY_QUEUE_NAME = PROPERTY_PREFIX + "queueName";
	private static final String PROPERTY_ORIGINAL_PARAMETERS = PROPERTY_PREFIX + PROPERTY_CURRENT_PARAMETERS;
	private final static String WORKFLOW_ID_FORMAT = "yyyy_MM_dd_hh_mm_ss_SSS";

	private final static Logger LOG = LoggerFactory.getLogger(DefaultWorkflowManager.class);

	@XmlJavaTypeAdapter(WorkflowsXmlAdapter.class)
	private final Map<String, Workflow> workflows;

	@XmlTransient
	private final ReadWriteLock readWriteLock;
	@XmlTransient
	private final Path userStagingArea;
	@XmlTransient
	private final Path userWorkflowsXmlPath;
	@XmlTransient
	private final AssetFinder assetFinder;
	@XmlTransient
	private final JobExecutionPropertiesHandler jobHandler;
	@XmlTransient
	private final Set<String> workflowsMarkedForDeletion;
	@XmlTransient
	private final Set<String> unstagedWorkflows;

	DefaultWorkflowManager(final Path userStagingArea, final AssetFinder assetFinder,
			final JobExecutionPropertiesHandler jobHandler) {
		Validate.isTrue(Files.exists(userStagingArea),
				"The user's staging area does not exist, this is probably a coding problem and should be reported.");
		this.userStagingArea = userStagingArea;
		this.assetFinder = assetFinder;
		this.readWriteLock = new ReentrantReadWriteLock(false);
		this.workflows = new TreeMap<String, Workflow>();
		this.userWorkflowsXmlPath = Paths.get(userStagingArea.toString(), USER_WORKFLOWS_XML_FILE_LOCATION);
		this.jobHandler = jobHandler;
		this.workflowsMarkedForDeletion = new TreeSet<String>();
		this.unstagedWorkflows = new TreeSet<String>();
	}

	/**
	 * JAXB serialization requires a default constructor.
	 */
	public DefaultWorkflowManager() {
		this(Paths.get("."), null, null);
	}

	@Override
	public void init() {
		final Lock writeLock = readWriteLock.writeLock();
		writeLock.lock();
		try {
			loadStagedWorkflowsFromFile_notThreadSafe();
			cleanStagingArea_notThreadSafe();
		} catch (final Exception e) {
			throw new ApplicationException("Could not init DefaultWorkflowManager. Cause: " + e.getMessage(), e);
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public Workflow importWorkflow(final File serverSideWorkflowLocation) {
		LOG.info("Importing workflow from " + serverSideWorkflowLocation.getAbsolutePath());
		final Lock writeLock = readWriteLock.writeLock();
		writeLock.lock();
		try {
			final String workflowId = new SimpleDateFormat(WORKFLOW_ID_FORMAT).format(new Date());
			final Workflow parsedWorkflow = loadWorkflowFromArchive_notThreadSafe(serverSideWorkflowLocation.toPath(),
					workflowId);
			// this workflow is unstaged
			unstagedWorkflows.add(parsedWorkflow.getId());
			workflows.put(parsedWorkflow.getId(), parsedWorkflow);
			return parsedWorkflow;
		} catch (final Exception e) {
			throw new ApplicationException(e.getMessage(), e);
		} finally {
			writeLock.unlock();
		}
	}

	private String extractAttribute(final Node node, final String attributeName) {
		return node.getAttributes().getNamedItem(attributeName).getNodeValue();
	}

	private String extractJobParameters(final Node node, final Map<String, String> jobProperties) {
		String parameters = jobProperties.get(PROPERTY_ORIGINAL_PARAMETERS);
		// if the params are missing, use the ones from the execution nodes
		if (parameters == null) {
			final Map<String, String> executionProperties = extractJobProperties(node, NODE_JOB_EXECUTE, "");
			parameters = executionProperties.get(PROPERTY_CURRENT_PARAMETERS);
		}
		return StringUtils.trimToEmpty(parameters);
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

	private Map<String, String> extractJobProperties(final Node parentNode, final String childrenNodeName,
			final String propertyPrefix) {
		final Map<String, String> propertyMap = new TreeMap<String, String>();
		final NodeList children = parentNode.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			final Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE && childrenNodeName.equals(child.getNodeName())) {
				// it is an element with the desired tagname, extract all attributes whose name starts with the prefix
				final Node keyNode = child.getAttributes().getNamedItem(ATTRIBUTE_KEY);
				if (keyNode != null) {
					final String key = keyNode.getNodeValue();
					if (StringUtils.isBlank(propertyPrefix) || key.startsWith(propertyPrefix)) {
						final Node valueNode = child.getAttributes().getNamedItem(ATTRIBUTE_VALUE);
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
			final Workflow currentWorkflow = workflows.get(workflow.getId());
			if (currentWorkflow == null) {
				throw new WorkflowNotFoundException(workflow);
			}
			workflowsMarkedForDeletion.add(currentWorkflow.getId());
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public void saveWorkflow(final Workflow workflow) {
		// we will make a copy of the workflow archive and modify workflow.xml to include
		// the information contained in each of the workflow jobs
		Validate.notNull(workflow,
				"workflow cannot be null; this seems to be a coding problem and should be reported.");
		if (LOG.isInfoEnabled()) {
			LOG.info("Saving workflow with name " + workflow.getName());
		}
		final Lock writeLock = readWriteLock.writeLock();
		writeLock.lock();
		try {
			if (workflows.containsKey(workflow.getId())) {
				saveWorkflowToArchive_notThreadSafe(workflow);
				workflows.put(workflow.getId(), workflow);
			} else {
				throw new WorkflowNotFoundException(workflow);
			}
		} catch (final XPathExpressionException | TransformerException | SAXException | ParserConfigurationException
				| IOException e) {
			throw new ApplicationException("Could not save workflow. This is a coding problem and should be reported.",
					e);
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public void commitChanges() {
		final Lock writeLock = readWriteLock.writeLock();
		writeLock.lock();
		try {
			// handle workflows marked for deletion and workflows that haven't been staged yet
			deleteWorkflowsMarkedForDeletion_notThreadSafe();
			stageUnstagedWorkflows_notThreadSafe();
			saveWorkflowsToXmlFile_notThreadSafe();
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public Collection<Job> getUnsupportedJobs(final Workflow workflow) {
		final Collection<Job> unsupportedJobs = new LinkedList<Job>();
		for (final Job job : workflow.getJobs()) {
			if (!jobHandler.canHandle(job)) {
				unsupportedJobs.add(job);
			}
		}
		return unsupportedJobs;
	}

	@Override
	public Collection<Workflow> getImportedWorkflows() {
		final Collection<Workflow> importedWorkflows = new LinkedList<Workflow>();
		final Lock readLock = readWriteLock.readLock();
		readLock.lock();
		try {
			for (final Workflow workflow : workflows.values()) {
				if (!workflowsMarkedForDeletion.contains(workflow.getId())) {
					importedWorkflows.add(workflow);
				}
			}
			return Collections.unmodifiableCollection(importedWorkflows);
		} finally {
			readLock.unlock();
		}
	}

	//////////////////////////////////
	////// NOT THREAD SAFE METHODS
	//////////////////////////////////
	private void saveWorkflowToArchive_notThreadSafe(final Workflow workflow) throws IOException, SAXException,
			ParserConfigurationException, TransformerException, XPathExpressionException {
		try (final FileSystem fileSystem = FileSystems.newFileSystem(workflow.getArchivePath(), null)) {
			// we're just interested in workflow.xml
			final Path workflowXmlPath = fileSystem.getPath(PATH_WORKFLOW_XML);
			// get the modified DOM document that reflects the changes in execution properties
			final Document modifiedWorkflowDocument = getModifiedDocument_notThreadSafe(workflow,
					Files.newInputStream(workflowXmlPath));
			// transform the modified DOM to an OutputStream
			saveDomInWorkflowArchive_notThreadSafe(workflowXmlPath, modifiedWorkflowDocument);
		}
	}

	private void saveDomInWorkflowArchive_notThreadSafe(final Path workflowXmlPath,
			final Document modifiedWorkflowDocument) throws TransformerFactoryConfigurationError,
			TransformerConfigurationException, TransformerException, IOException {
		final TransformerFactory tFactory = TransformerFactory.newInstance();
		final Transformer transformer = tFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		final DOMSource source = new DOMSource(modifiedWorkflowDocument);
		final ByteArrayOutputStream modifiedWorkflowXmlOutputStream = new ByteArrayOutputStream();
		final StreamResult result = new StreamResult(modifiedWorkflowXmlOutputStream);
		transformer.transform(source, result);
		// modify the zipfile directly
		final InputStream modifiedWorkflowXmlInputStream = new ByteArrayInputStream(
				modifiedWorkflowXmlOutputStream.toByteArray());
		Files.copy(modifiedWorkflowXmlInputStream, workflowXmlPath, StandardCopyOption.REPLACE_EXISTING);
	}

	private Document getModifiedDocument_notThreadSafe(final Workflow workflow,
			final InputStream originalWorkflowXmlInputStream)
			throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		// get the workflow.xml descriptor from the current workflow archive and load it on a DOM
		final Document currentWorkflowDocument = getDocumentFromInputStream_notThreadSafe(
				originalWorkflowXmlInputStream);
		// go through each job and modify the DOM
		final XPath xPath = XPathFactory.newInstance().newXPath();
		final MessageFormat singleJobSelectorFormat = new MessageFormat(XPATH_SINGLE_JOB_SELECTOR);
		for (final Job job : workflow.getJobs()) {
			final String xPathExpression = singleJobSelectorFormat.format(new Object[] { job.getName() });
			final NodeList jobList = (NodeList) xPath.evaluate(xPathExpression,
					currentWorkflowDocument.getDocumentElement(), XPathConstants.NODESET);
			if (jobList.getLength() == 0) {
				throw new JobNotFoundException(job.getName());
			} else if (jobList.getLength() > 1) {
				throw new InvalidWorkflowException("More than one job with the name '" + job.getName() + "' was found.",
						workflow.getArchivePath());
			}
			// we know we have only one job, extract the execution properties
			final Node jobNode = jobList.item(0);
			final Map<String, String> jobExecutionProperties = extractJobProperties(jobNode, NODE_JOB_EXECUTE, "");
			// transform the properties accordingly
			if (jobHandler.canHandle(job)) {
				jobHandler.handle(job, jobExecutionProperties);
			} else {
				// don't throw an exception, save the workflow as-is
				LOG.warn("Job " + job + " can't be handled.");
				continue;
			}
			// write the modified properties to the dom
			writeExecutionProperties_notThreadSafe(jobNode, jobExecutionProperties, xPath);
			// write job information in the description nodes
			writeJobProperties_notThreadSafe(jobNode, job, xPath);
		}
		return currentWorkflowDocument;
	}

	private Document getDocumentFromInputStream_notThreadSafe(final InputStream inputStream)
			throws ParserConfigurationException, SAXException, IOException {
		final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		final Document currentWorkflowDocument = documentBuilder.parse(inputStream);
		return currentWorkflowDocument;
	}

	private void writeExecutionProperties_notThreadSafe(final Node jobNode,
			final Map<String, String> executionProperties, final XPath xPath) throws XPathExpressionException {

		final NodeList childrenExecuteNodes = (NodeList) xPath.evaluate(NODE_JOB_EXECUTE, jobNode,
				XPathConstants.NODESET);
		final Collection<Node> nodesToRemove = new LinkedList<Node>();
		final Collection<Element> nodesToInsert = new LinkedList<Element>();
		final Set<String> processedProperties = new TreeSet<String>();
		for (int i = 0; i < childrenExecuteNodes.getLength(); i++) {
			final Node childExecuteNode = childrenExecuteNodes.item(i);
			// if the property is not in the map, it means that this node is to be removed
			final String propertyKey = childExecuteNode.getAttributes().getNamedItem(ATTRIBUTE_KEY).getNodeValue();
			final String propertyValue = executionProperties.get(propertyKey);
			if (propertyValue == null) {
				// not found on the properties, it means that this node has to be removed
				nodesToRemove.add(childExecuteNode);
			} else {
				// found on the properties, update the 'value' attribute
				childExecuteNode.getAttributes().getNamedItem(ATTRIBUTE_VALUE).setNodeValue(propertyValue);
			}
			processedProperties.add(propertyKey);

		}
		// find out which properties were not processed yet
		for (final Map.Entry<String, String> entry : executionProperties.entrySet()) {
			if (!processedProperties.contains(entry.getKey())) {
				// if a property is present on the properties but has not been processed,
				// it means that we need to add a new node
				nodesToInsert.add(createNewNode_notThreadSafe(jobNode, NODE_JOB_EXECUTE, "desc", "null", "inh", "null",
						"label", "null", ATTRIBUTE_KEY, entry.getKey(), ATTRIBUTE_VALUE, entry.getValue()));
			}
		}
		// add/remove nodes accordingly
		for (final Node nodeToRemove : nodesToRemove) {
			jobNode.removeChild(nodeToRemove);
		}
		for (final Element nodeToInsert : nodesToInsert) {
			jobNode.appendChild(nodeToInsert);
		}
	}

	private void writeJobProperties_notThreadSafe(final Node jobNode, final Job job, final XPath xPath)
			throws XPathExpressionException {
		// remove all the description nodes that match
		final NodeList childrenDescriptionNodes = (NodeList) xPath.evaluate(NODE_JOB_DESCRIPTION, jobNode,
				XPathConstants.NODESET);
		final Collection<Node> nodesToRemove = new LinkedList<Node>();
		for (int i = 0; i < childrenDescriptionNodes.getLength(); i++) {
			final Node childDescriptionNode = childrenDescriptionNodes.item(i);
			final String propertyKey = childDescriptionNode.getAttributes().getNamedItem(ATTRIBUTE_KEY).getNodeValue();
			if (StringUtils.isNotBlank(propertyKey) && propertyKey.startsWith(PROPERTY_PREFIX)) {
				nodesToRemove.add(childDescriptionNode);
			}
		}
		for (final Node nodeToRemove : nodesToRemove) {
			jobNode.removeChild(nodeToRemove);
		}
		// add a node for each of the job properties
		jobNode.appendChild(createNewNode_notThreadSafe(jobNode, NODE_JOB_DESCRIPTION, ATTRIBUTE_KEY,
				PROPERTY_RESOURCE_NAME, ATTRIBUTE_VALUE, job.getResourceName()));
		jobNode.appendChild(createNewNode_notThreadSafe(jobNode, NODE_JOB_DESCRIPTION, ATTRIBUTE_KEY,
				PROPERTY_RESOURCE_TYPE, ATTRIBUTE_VALUE, job.getResourceType()));
		jobNode.appendChild(createNewNode_notThreadSafe(jobNode, NODE_JOB_DESCRIPTION, ATTRIBUTE_KEY, PROPERTY_APP_NAME,
				ATTRIBUTE_VALUE, job.getApplication().getName()));
		jobNode.appendChild(createNewNode_notThreadSafe(jobNode, NODE_JOB_DESCRIPTION, ATTRIBUTE_KEY,
				PROPERTY_APP_VERSION, ATTRIBUTE_VALUE, job.getApplication().getVersion()));
		jobNode.appendChild(createNewNode_notThreadSafe(jobNode, NODE_JOB_DESCRIPTION, ATTRIBUTE_KEY, PROPERTY_APP_PATH,
				ATTRIBUTE_VALUE, job.getApplication().getPath()));
		jobNode.appendChild(createNewNode_notThreadSafe(jobNode, NODE_JOB_DESCRIPTION, ATTRIBUTE_KEY,
				PROPERTY_ORIGINAL_PARAMETERS, ATTRIBUTE_VALUE, job.getParameters()));
		final Queue queue = job.getQueue();
		if (queue != null) {
			jobNode.appendChild(createNewNode_notThreadSafe(jobNode, NODE_JOB_DESCRIPTION, ATTRIBUTE_KEY,
					PROPERTY_QUEUE_NAME, ATTRIBUTE_VALUE, queue.getName()));
		}
	}

	private Element createNewNode_notThreadSafe(final Node parentNode, final String name, final String... attributes) {
		if (attributes == null || attributes.length % 2 != 0) {
			throw new IllegalArgumentException(
					"Attributes are provided as varargs: name1, value1, name2, value2, ... nameN, valueN. Provided values: "
							+ Arrays.toString(attributes) + ". This is a coding problem and should be reported.");
		}
		final Element newNode = parentNode.getOwnerDocument().createElement(name);
		for (int i = 0; i < attributes.length; i += 2) {
			newNode.setAttribute(attributes[i], attributes[i + 1]);
		}
		return newNode;

	}

	private Workflow loadWorkflowFromArchive_notThreadSafe(final Path workflowArchiveLocation, final String workflowId)
			throws IOException, ParserConfigurationException, SAXException {
		try (final FileSystem fileSystem = FileSystems.newFileSystem(workflowArchiveLocation, null)) {
			final Path workflowXmlPath = fileSystem.getPath(PATH_WORKFLOW_XML);
			if (!Files.exists(workflowXmlPath)) {
				throw new InvalidWorkflowException("The file doesn't contain a 'workflow.xml' entry.",
						workflowArchiveLocation);
			}

			try (final InputStream workflowXmlInputStream = Files.newInputStream(workflowXmlPath)) {
				// read workflow.xml into a document
				final Document document = getDocumentFromInputStream_notThreadSafe(workflowXmlInputStream);

				final XPath xPath = XPathFactory.newInstance().newXPath();
				final String workflowName;
				try {
					workflowName = (String) xPath.evaluate(XPATH_WORKFLOW_NAME, document, XPathConstants.STRING);
				} catch (final XPathExpressionException e) {
					throw new InvalidWorkflowException("Error while evaluation XPath expression '/workflow/@name'",
							workflowArchiveLocation, e);
				}

				final Workflow parsedWorkflow = new Workflow();
				parsedWorkflow.setId(workflowId);
				parsedWorkflow.setName(workflowName);
				parsedWorkflow.setArchivePath(workflowArchiveLocation.toAbsolutePath());

				final NodeList jobNodeList;
				try {
					jobNodeList = (NodeList) xPath.evaluate(XPATH_ALL_JOBS_SELECTOR, document, XPathConstants.NODESET);
				} catch (final XPathExpressionException e) {
					throw new InvalidWorkflowException("Error while evaluation XPath expression '/workflow/real/job'",
							workflowArchiveLocation, e);
				}

				for (int i = 0; i < jobNodeList.getLength(); i++) {
					final Node node = jobNodeList.item(i);
					final String jobName = extractAttribute(node, ATTRIBUTE_JOB_NAME);
					final Job parsedJob = new Job(jobName);
					final Map<String, String> jobProperties = extractJobProperties(node, NODE_JOB_DESCRIPTION,
							PROPERTY_PREFIX);
					parsedJob.setParameters(extractJobParameters(node, jobProperties));
					assetFinder.clearAllFields();
					final Application application = extractApplication(node, jobProperties);
					if (application != null) {
						parsedJob.setApplication(application);
					}
					final Queue queue = extractQueue(node, jobProperties);
					if (queue != null) {
						parsedJob.setQueue(queue);
					}
					parsedWorkflow.addJob(parsedJob);
				}

				return parsedWorkflow;
			}
		}
	}

	private void loadStagedWorkflowsFromFile_notThreadSafe()
			throws IOException, ParserConfigurationException, SAXException {
		try {
			workflows.clear();
			unstagedWorkflows.clear();
			workflowsMarkedForDeletion.clear();

			// if the file does not exist, don't load anything and log this event
			if (Files.exists(userWorkflowsXmlPath)) {
				final JAXBContext jaxbContext = JAXBContext.newInstance(DefaultWorkflowManager.class);
				final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

				// this manager has "empty" workflows, only name, id and archivePath are contained
				// in the xml file, so we need to load the workflows from the archive
				final DefaultWorkflowManager workflowManager = (DefaultWorkflowManager) jaxbUnmarshaller
						.unmarshal(userWorkflowsXmlPath.toFile());

				for (final Workflow workflow : workflowManager.workflows.values()) {
					workflows.put(workflow.getId(),
							loadWorkflowFromArchive_notThreadSafe(workflow.getArchivePath(), workflow.getId()));
				}
			} else {
				LOG.info(
						"The xml file in which the user workflows are stored does not exist yet. It will be created once a save operation is performed. File location: "
								+ userWorkflowsXmlPath.toAbsolutePath().toString());
			}
		} catch (final JAXBException e) {
			throw new ApplicationException(e.getMessage(), e);
		}
	}

	private void cleanStagingArea_notThreadSafe() {
		// remove archives on the staging area that don't belong to any workflow
		LOG.info("Cleaning staging area");
		final Set<Path> orphanArchives = new TreeSet<Path>();
		try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(userStagingArea)) {
			for (final Path path : directoryStream) {
				if (path.toString().endsWith(USER_WORKFLOWS_ARCHIVE_SUFFIX)) {
					orphanArchives.add(path.toAbsolutePath());
				}
			}
		} catch (final IOException e) {
			LOG.error("Could not list entries while clearing staging area of old archives. Cause: " + e.getMessage(),
					e);
		}
		for (final Workflow workflow : workflows.values()) {
			orphanArchives.remove(workflow.getArchivePath().toAbsolutePath());
		}
		// any path remaining should be removed
		for (final Path orphanArchive : orphanArchives) {
			try {
				LOG.info("Deleting orphan archive: " + orphanArchive.toString());
				Files.deleteIfExists(orphanArchive);
			} catch (final IOException e) {
				LOG.error("Could not delete orphan archive " + orphanArchive.toString());
			}
		}
	}

	private void deleteWorkflowsMarkedForDeletion_notThreadSafe() {
		final Collection<String> removedKeys = new LinkedList<String>();
		for (final String keyToRemove : workflowsMarkedForDeletion) {
			// remove it from the map
			final Workflow workflow = workflows.remove(keyToRemove);
			removedKeys.add(keyToRemove);
			// and remove the archive
			try {
				Files.deleteIfExists(workflow.getArchivePath());
			} catch (final IOException e) {
				LOG.error("Could not delete archive located on " + workflow.getArchivePath().toString()
						+ " of workflow ID " + workflow.getId(), e);
			}
		}
		workflowsMarkedForDeletion.removeAll(removedKeys);
	}

	private void stageUnstagedWorkflows_notThreadSafe() {
		for (final Map.Entry<String, Workflow> entry : workflows.entrySet()) {
			final Workflow workflow = entry.getValue();
			if (unstagedWorkflows.contains(entry.getKey()) && !workflowsMarkedForDeletion.contains(entry.getKey())) {
				// move the archive to the staging area
				try {
					moveWorkflowToStagingArea_notThreadSafe(workflow);
					unstagedWorkflows.remove(entry.getKey());
				} catch (final IOException e) {
					throw new ApplicationException("Could not move archive to staging area. Cause: " + e.getMessage(),
							e);
				}
			}
		}
	}

	private void saveWorkflowsToXmlFile_notThreadSafe() {
		try {
			final JAXBContext jaxbContext = JAXBContext.newInstance(DefaultWorkflowManager.class);
			final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			jaxbMarshaller.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, true);

			jaxbMarshaller.marshal(this, userWorkflowsXmlPath.toFile());
		} catch (final JAXBException e) {
			throw new ApplicationException(e.getMessage(), e);
		}
	}

	private void moveWorkflowToStagingArea_notThreadSafe(final Workflow workflow) throws IOException {
		final Path newArchivePath = generateWorkflowPath_notThreadSafe();
		Files.move(workflow.getArchivePath(), newArchivePath, StandardCopyOption.REPLACE_EXISTING);
		workflow.setArchivePath(newArchivePath);
	}

	private Path generateWorkflowPath_notThreadSafe() throws IOException {
		Path newWorkflowPath;
		do {
			final String fileName = UUID.randomUUID().toString() + USER_WORKFLOWS_ARCHIVE_SUFFIX;
			newWorkflowPath = Paths.get(userStagingArea.toString(), fileName);
		} while (Files.exists(newWorkflowPath));
		return newWorkflowPath;
	}
}
