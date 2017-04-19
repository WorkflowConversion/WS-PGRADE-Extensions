package com.workflowconversion.portlet.ui.resource.upload;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.jsoup.helper.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.core.resource.FormField;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.core.resource.ResourceProvider;

/**
 * Processes an uploaded XML file.
 * 
 * @author delagarza
 *
 */
public class XMLBulkResourcesFileProcessor extends AbstractFileProcessor {

	private final static Logger LOG = LoggerFactory.getLogger(XMLBulkResourcesFileProcessor.class);

	public XMLBulkResourcesFileProcessor(final File serverSideFile, final BulkUploadListener listener,
			final ResourceProvider resourceProvider) {
		super(serverSideFile, listener, resourceProvider);
	}

	@Override
	void parseFile(final File serverSideFile) throws Exception {
		Validate.notNull(serverSideFile,
				"serverSideFile cannot be null; this seems to be a coding problem and should be reported.");
		if (LOG.isInfoEnabled()) {
			LOG.info("Parsing applications file from " + serverSideFile.getAbsolutePath());
		}
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(true);
		final SAXParser saxParser = factory.newSAXParser();
		saxParser.parse(serverSideFile, new SAXHandler());
	}

	private class SAXHandler extends DefaultHandler {
		private Locator locator;
		private Resource currentResource;
		private String currentTopElement;
		private final Map<String, String> attributeMap = new TreeMap<String, String>();
		private final static String RESOURCE_NODE_NAME = "resource";
		private final static String APPLICATION_NODE_NAME = "application";

		@Override
		public void setDocumentLocator(final Locator locator) {
			this.locator = locator;
		}

		@Override
		public void startElement(final String uri, final String localName, final String qName,
				final Attributes attributes) throws SAXException {
			// check for top-level nodes first (resource, application)
			if (LOG.isDebugEnabled()) {
				LOG.debug("Processing tag <" + qName + '>');
			}
			if (qName.equalsIgnoreCase(RESOURCE_NODE_NAME)) {
				currentTopElement = RESOURCE_NODE_NAME;
			} else if (qName.equalsIgnoreCase(APPLICATION_NODE_NAME)) {
				currentTopElement = APPLICATION_NODE_NAME;
			} else {
				if (LOG.isDebugEnabled()) {
					LOG.debug("Ignoring tag <" + qName + '>');
				}
			}
			loadAttributesInMap(attributes);
			if (currentTopElement == RESOURCE_NODE_NAME) {
				currentResource = resourceProvider.getResource(extract(Resource.Field.Name),
						extract(Resource.Field.Type));
			} else if (currentTopElement == APPLICATION_NODE_NAME) {
				addParsedApplication(currentResource, extract(Application.Field.Name),
						extract(Application.Field.Version), extract(Application.Field.Path),
						extract(Application.Field.Description), locator.getLineNumber());
			}
		}

		private void loadAttributesInMap(final Attributes attributes) {
			attributeMap.clear();
			for (int i = 0; i < attributes.getLength(); i++) {
				final String attributeName = attributes.getQName(i);
				final String attributeValue = attributes.getValue(i);
				if (LOG.isDebugEnabled()) {
					LOG.debug("Loading attribute [" + attributeName + '=' + attributeValue + ']');
				}
				attributeMap.put(attributeName.toLowerCase(), StringUtils.trimToEmpty(attributeValue));
			}
		}

		private String extract(final FormField field) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Extracting " + field.toString());
			}
			return attributeMap.get(field.name().toLowerCase());
		}

		@Override
		public void endElement(final String uri, final String localName, final String qName) throws SAXException {
			// check for top-level nodes first (resource, application, queue)
			if (qName.equalsIgnoreCase(RESOURCE_NODE_NAME)) {
				currentResource = null;
			}
		}
	}

}
