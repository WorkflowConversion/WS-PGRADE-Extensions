package com.workflowconversion.portlet.ui.resource.upload;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
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
import com.workflowconversion.portlet.core.utils.KeyUtils;

/**
 * Processes an uploaded XML file.
 * 
 * @author delagarza
 *
 */
public class XMLBulkResourcesFileProcessor extends AbstractFileProcessor<Resource> {

	private final static Logger LOG = LoggerFactory.getLogger(XMLBulkResourcesFileProcessor.class);

	public XMLBulkResourcesFileProcessor(final File serverSideFile, final BulkUploadListener<Resource> listener,
			final ResourceProvider resourceProvider) {
		super(serverSideFile, listener, resourceProvider);
	}

	@Override
	Collection<Resource> parseFile(final File serverSideFile) throws Exception {
		Validate.notNull(serverSideFile,
				"serverSideFile cannot be null; this seems to be a coding problem and should be reported.");
		if (LOG.isInfoEnabled()) {
			LOG.info("Parsing applications file from " + serverSideFile.getAbsolutePath());
		}
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(true);
		final SAXParser saxParser = factory.newSAXParser();
		final Map<String, Resource> parsedResources = new TreeMap<String, Resource>();
		final SAXHandler saxHandler = new SAXHandler(parsedResources);
		saxParser.parse(serverSideFile, saxHandler);
		return parsedResources.values();
	}

	private class SAXHandler extends DefaultHandler {
		private Locator locator;
		private Resource currentResource;
		private final Map<String, String> attributeMap = new TreeMap<String, String>();
		private final Map<String, Resource> parsedResources;
		private final static String RESOURCE_NODE_NAME = "resource";
		private final static String APPLICATION_NODE_NAME = "application";

		private SAXHandler(final Map<String, Resource> parsedResources) {
			this.parsedResources = parsedResources;
		}

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
			loadAttributesInMap(attributes);

			if (qName.equalsIgnoreCase(RESOURCE_NODE_NAME)) {
				// find the resource using the resource provider (this method has error handling, so no
				// need to do it again)
				currentResource = findResource(extractFromAttributesMap(Resource.Field.Name),
						extractFromAttributesMap(Resource.Field.Type), locator.getLineNumber());
				if (currentResource != null) {
					final String currentResourceKey = KeyUtils.generate(currentResource);
					// check if we've already seen this resource
					final Resource storedResource = parsedResources.put(currentResourceKey, currentResource);
					if (storedResource != null) {
						// we've seen this resource before, do not use the one from the provider, use the
						// one we've got in the map because that one contains the apps we've parsed,
						// so we need to rever the 'put' operation
						parsedResources.put(currentResourceKey, storedResource);
						currentResource = storedResource;
					}
				}
			} else if (qName.equalsIgnoreCase(APPLICATION_NODE_NAME)) {
				addParsedApplication(currentResource, extractFromAttributesMap(Application.Field.Name),
						extractFromAttributesMap(Application.Field.Version),
						extractFromAttributesMap(Application.Field.Path),
						extractFromAttributesMap(Application.Field.Description), locator.getLineNumber());
			} else {
				// jesus, too many dots
				XMLBulkResourcesFileProcessor.this.listener
						.parsingWarning("Ignoring unrecognized tag name '" + qName + '\'', locator.getLineNumber());
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

		private String extractFromAttributesMap(final FormField field) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Extracting " + field.toString());
			}
			return attributeMap.get(field.name().toLowerCase());
		}
	}
}
