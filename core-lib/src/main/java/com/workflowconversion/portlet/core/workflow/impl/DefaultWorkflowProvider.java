package com.workflowconversion.portlet.core.workflow.impl;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.google.common.io.Files;
import com.workflowconversion.portlet.core.app.Application;
import com.workflowconversion.portlet.core.exception.InvalidWorkflowException;
import com.workflowconversion.portlet.core.workflow.Job;
import com.workflowconversion.portlet.core.workflow.Job.ConfigurationState;
import com.workflowconversion.portlet.core.workflow.Workflow;
import com.workflowconversion.portlet.core.workflow.WorkflowProvider;

/**
 * Provides the default implementation to access workflows in the staging area.
 * 
 * Archives to be imported must contain the following elements under each of their "concrete" jobs (i.e., children of
 * the {@code real} element):
 * 
 * <ul>
 * <li>{@code <description key="knime2guse.appVersion" value="..."/>}
 * <li>{@code <description key="knime2guse.appName" value="..."/>}
 * </ul>
 * 
 * @author delagarza
 *
 */
public class DefaultWorkflowProvider implements WorkflowProvider {

	private static final String PROPERTY_PREFIX = "knime2guse.";
	private static final String PROPERTY_APP_NAME = PROPERTY_PREFIX + "appName";
	private static final String PROPERTY_APP_VERSION = PROPERTY_PREFIX + "appVersion";
	private static final String PROPERTY_JOB_STATUS = PROPERTY_PREFIX + "jobStatus";
	private final static String WORKFLOW_ID_FORMAT = "yyyy_MM_dd_hh_mm_ss_SSS";

	private final static Logger LOG = LoggerFactory.getLogger(DefaultWorkflowProvider.class);

	private final File stagingArea;

	DefaultWorkflowProvider(final File stagingArea) {
		this.stagingArea = stagingArea;
	}

	@Override
	public Workflow importToStagingArea(final File serverSideWorkflowLocation) throws IOException {
		final Workflow parsedWorkflow = loadFromFile(serverSideWorkflowLocation);
		moveToStagingArea(parsedWorkflow);
		return parsedWorkflow;
	}

	private Workflow loadFromFile(final File serverSideWorkflowLocation) throws IOException {
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
			} catch (XPathExpressionException e) {
				throw new InvalidWorkflowException("Error while evaluation XPath expression '/workflow/@name'",
						serverSideWorkflowLocation, e);
			}

			final String workflowId = new SimpleDateFormat(WORKFLOW_ID_FORMAT).format(new Date());
			final Workflow parsedWorkflow = new Workflow(workflowId);
			parsedWorkflow.setName(workflowName);
			parsedWorkflow.setLocation(serverSideWorkflowLocation);

			final NodeList jobNodeList;
			try {
				jobNodeList = (NodeList) xPath.evaluate("/workflow/real/job", inputSource, XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				throw new InvalidWorkflowException("Error while evaluation XPath expression '/workflow/real/job'",
						serverSideWorkflowLocation, e);
			}
			for (int i = 0; i < jobNodeList.getLength(); i++) {
				final Node node = jobNodeList.item(i);
				final String jobName = extractAttribute(node, "name");
				final Job parsedJob = new Job(jobName);
				final Map<String, String> jobProperties = extractJobProperties(node);
				setConfigurationState(parsedJob, jobProperties);
				parsedJob.setApplication(extractApplication(node, serverSideWorkflowLocation, jobProperties));
				parsedJob.setName(jobName);
				parsedWorkflow.addJob(parsedJob);
			}

			zippedWorkflow.close();

			return parsedWorkflow;
		}
	}

	private void setConfigurationState(final Job job, final Map<String, String> jobProperties) {
		final String jobStatus = jobProperties.get(PROPERTY_JOB_STATUS);
		Job.ConfigurationState configurationState = ConfigurationState.Incomplete;
		if (!StringUtils.isBlank(jobStatus)) {
			try {
				configurationState = ConfigurationState.valueOf(jobStatus.trim());
			} catch (IllegalArgumentException e) {
				// just log the exception, state stays as incomplete
				LOG.error("Invalid configuration state for job " + job.getName(), e);
			}
		}
		job.setConfigurationState(configurationState);
	}

	private String extractAttribute(final Node node, final String attributeName) {
		return node.getAttributes().getNamedItem(attributeName).getNodeValue();
	}

	private Application extractApplication(final Node node, final File serverSideWorkflowLocation,
			final Map<String, String> jobProperties) {
		// try as hard as we can to get the version and application name
		final String version = jobProperties.get(PROPERTY_APP_VERSION);
		final String name = jobProperties.get(PROPERTY_APP_NAME);
		final Application parsedApplication = new Application();
		if (StringUtils.isNotBlank(version)) {
			parsedApplication.setVersion(version);
		}
		if (StringUtils.isNotBlank(name)) {
			parsedApplication.setName(name);
		}
		// at the time of import, we can only know version and name of the application
		return parsedApplication;
	}

	// returns the attribute values for all <description> elements with a 'key' attribute starting with 'knime2guse.'
	private Map<String, String> extractJobProperties(final Node node) {
		final Map<String, String> propertyMap = new TreeMap<String, String>();
		final NodeList children = node.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {
			final Node child = children.item(i);
			if (child.getNodeType() == Node.ELEMENT_NODE && "description".equals(child.getNodeName())) {
				// it is a description element, now, extract all attributes whose name starts with 'knime2guse.'
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

	private void moveToStagingArea(final Workflow workflow) throws IOException {
		final File newLocation = new File(stagingArea, workflow.getId() + ".zip");
		Files.move(workflow.getLocation(), newLocation);
		workflow.setLocation(newLocation);
	}

	@Override
	public void deleteWorkflow(final Workflow workflow) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveWorkflow(final Workflow workflow) {
		// we will make a copy of the workflow archive and modify workflow.xml to include
		// the information contained in each of the workflow jobs

	}

	@Override
	public Collection<Workflow> getStagedWorkflows() {
		// TODO Auto-generated method stub
		return null;
	}

}
