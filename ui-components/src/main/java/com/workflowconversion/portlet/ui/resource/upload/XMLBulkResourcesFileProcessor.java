package com.workflowconversion.portlet.ui.resource.upload;

import java.io.File;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.core.resource.FormField;
import com.workflowconversion.portlet.core.resource.Queue;
import com.workflowconversion.portlet.core.resource.Resource;

/**
 * Processes an uploaded XML file.
 * 
 * @author delagarza
 *
 */
public class XMLBulkResourcesFileProcessor extends AbstractFileProcessor {

	public XMLBulkResourcesFileProcessor(final File serverSideFile, final BulkUploadListener listener,
			final Set<String> validMiddlewareTypes) {
		super(serverSideFile, listener, validMiddlewareTypes);
	}

	@Override
	void parseFile(final File serverSideFile) throws Exception {
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(true);
		final SAXParser saxParser = factory.newSAXParser();
		saxParser.parse(serverSideFile, new SAXHandler());
	}

	private class SAXHandler extends DefaultHandler {
		Locator locator;
		Resource currentResource;
		Application currentApplication;
		Queue currentQueue;
		FormField currentField;
		// the name of the queue comes as a text node and not as an attribute value
		final StringBuilder currentFieldValue = new StringBuilder();
		String currentTopElement;
		private final static String RESOURCE_NODE_NAME = "resource";
		private final static String APPLICATION_NODE_NAME = "application";
		private final static String QUEUE_NODE_NAME = "queue";

		@Override
		public void setDocumentLocator(final Locator locator) {
			this.locator = locator;
		}

		@Override
		public void startElement(final String uri, final String localName, final String qName,
				final Attributes attributes) throws SAXException {
			// clear whatever has been stored, as we are about to start a new field
			currentFieldValue.setLength(0);

			// check for top-level nodes first (resource, application, queue)
			if (qName.equalsIgnoreCase(RESOURCE_NODE_NAME)) {
				currentResource = new Resource();
				currentTopElement = RESOURCE_NODE_NAME;
			} else if (qName.equalsIgnoreCase(APPLICATION_NODE_NAME)) {
				currentApplication = new Application();
				currentTopElement = APPLICATION_NODE_NAME;
			} else if (qName.equalsIgnoreCase(QUEUE_NODE_NAME)) {
				currentQueue = new Queue();
				currentTopElement = QUEUE_NODE_NAME;
			} else {
				// check for field names
				if (currentTopElement == RESOURCE_NODE_NAME) {
					if (qName.equalsIgnoreCase(Resource.Field.Name.name())) {
						currentField = Resource.Field.Name;
					} else if (qName.equalsIgnoreCase(Resource.Field.Type.name())) {
						currentField = Resource.Field.Type;
					}
				} else if (currentTopElement == APPLICATION_NODE_NAME) {
					if (qName.equalsIgnoreCase(Application.Field.Name.name())) {
						currentField = Application.Field.Name;
					} else if (qName.equalsIgnoreCase(Application.Field.Description.name())) {
						currentField = Application.Field.Description;
					} else if (qName.equalsIgnoreCase(Application.Field.Path.name())) {
						currentField = Application.Field.Path;
					} else if (qName.equalsIgnoreCase(Application.Field.Version.name())) {
						currentField = Application.Field.Version;
					}
				} else if (currentTopElement == QUEUE_NODE_NAME) {
					if (qName.equalsIgnoreCase(Queue.Field.Name.name())) {
						currentField = Queue.Field.Name;
					}
				}
			}
		}

		@Override
		public void endElement(final String uri, final String localName, final String qName) throws SAXException {
			// check for top-level nodes first (resource, application, queue)
			if (qName.equalsIgnoreCase(RESOURCE_NODE_NAME)) {
				XMLBulkResourcesFileProcessor.this.addParsedResource(currentResource, locator.getLineNumber());
			} else if (qName.equalsIgnoreCase(APPLICATION_NODE_NAME)) {
				XMLBulkResourcesFileProcessor.this.addParsedApplication(currentResource, currentApplication,
						locator.getLineNumber());
			} else if (qName.equalsIgnoreCase(QUEUE_NODE_NAME)) {
				// add a queue to the current resource
				XMLBulkResourcesFileProcessor.this.addParsedQueue(currentResource, currentQueue,
						locator.getLineNumber());
			} else {
				// check for field names
				if (currentTopElement == RESOURCE_NODE_NAME) {
					if (qName.equalsIgnoreCase(Resource.Field.Name.name())) {
						currentResource.setName(getCleanCurrentFieldValue());
					} else if (qName.equalsIgnoreCase(Resource.Field.Type.name())) {
						currentResource.setType(getCleanCurrentFieldValue());
					}
				} else if (currentTopElement == APPLICATION_NODE_NAME) {
					if (qName.equalsIgnoreCase(Application.Field.Name.name())) {
						currentApplication.setName(getCleanCurrentFieldValue());
					} else if (qName.equalsIgnoreCase(Application.Field.Description.name())) {
						currentApplication.setDescription(getCleanCurrentFieldValue());
					} else if (qName.equalsIgnoreCase(Application.Field.Path.name())) {
						currentApplication.setPath(getCleanCurrentFieldValue());
					} else if (qName.equalsIgnoreCase(Application.Field.Version.name())) {
						currentApplication.setVersion(getCleanCurrentFieldValue());
					}
				} else if (currentTopElement == QUEUE_NODE_NAME) {
					if (qName.equalsIgnoreCase(Queue.Field.Name.name())) {
						currentQueue.setName(getCleanCurrentFieldValue());
					}
				}
			}
		}

		private String getCleanCurrentFieldValue() {
			return StringUtils.trimToEmpty(currentFieldValue.toString());
		}

		@Override
		public void characters(final char[] ch, final int start, final int length) throws SAXException {
			if (currentField != null && (Resource.Field.class.isAssignableFrom(currentField.getClass())
					|| Application.Field.class.isAssignableFrom(currentField.getClass())
					|| Queue.Field.class.isAssignableFrom(currentField.getClass()))) {
				currentFieldValue.append(ch, start, length);
			}
		}
	}

}
