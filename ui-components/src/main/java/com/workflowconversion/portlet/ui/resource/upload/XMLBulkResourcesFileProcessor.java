package com.workflowconversion.portlet.ui.resource.upload;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;

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
import com.workflowconversion.portlet.core.resource.ResourceProvider;

/**
 * Processes an uploaded XML file.
 * 
 * @author delagarza
 *
 */
public class XMLBulkResourcesFileProcessor extends AbstractFileProcessor {

	public XMLBulkResourcesFileProcessor(final File serverSideFile, final BulkUploadListener listener,
			final ResourceProvider resourceProvider) {
		super(serverSideFile, listener, resourceProvider);
	}

	@Override
	void parseFile(final File serverSideFile) throws Exception {
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(true);
		final SAXParser saxParser = factory.newSAXParser();
		saxParser.parse(serverSideFile, new SAXHandler());
	}

	private class SAXHandler extends DefaultHandler {
		private Locator locator;
		private final Map<FormField, String> parsedValues = new TreeMap<FormField, String>();
		private FormField currentField;
		// the name of the queue comes as a text node and not as an attribute value
		final StringBuilder currentFieldValue = new StringBuilder();
		private Resource currentResource;
		private String currentTopElement;
		private final static String RESOURCE_NODE_NAME = "resource";
		private final static String APPLICATION_NODE_NAME = "application";

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
				currentTopElement = RESOURCE_NODE_NAME;
			} else if (qName.equalsIgnoreCase(APPLICATION_NODE_NAME)) {
				currentTopElement = APPLICATION_NODE_NAME;
			} else {
				final FormField[] fields = getCurrentFields();
				for (final FormField field : fields) {
					if (qName.equalsIgnoreCase(field.name())) {
						currentField = field;
						break;
					}
				}

			}
		}

		@Override
		public void endElement(final String uri, final String localName, final String qName) throws SAXException {
			// check for top-level nodes first (resource, application, queue)
			if (qName.equalsIgnoreCase(RESOURCE_NODE_NAME)) {
				currentResource = XMLBulkResourcesFileProcessor.this.findResource(parsedValues.get(Resource.Field.Name),
						parsedValues.get(Resource.Field.Type), locator.getLineNumber());
			} else if (qName.equalsIgnoreCase(APPLICATION_NODE_NAME)) {
				XMLBulkResourcesFileProcessor.this.addParsedApplication(currentResource,
						parsedValues.get(Application.Field.Name), parsedValues.get(Application.Field.Version),
						parsedValues.get(Application.Field.Path), parsedValues.get(Application.Field.Description),
						locator.getLineNumber());
			} else {
				final FormField[] fields = getCurrentFields();
				for (final FormField field : fields) {
					if (qName.equalsIgnoreCase(field.name())) {
						parsedValues.put(field, getCleanCurrentFieldValue());
						break;
					}
				}

			}
		}

		private FormField[] getCurrentFields() {
			final FormField[] fields;
			if (currentTopElement == RESOURCE_NODE_NAME) {
				fields = Resource.Field.values();
			} else if (currentTopElement == APPLICATION_NODE_NAME) {
				fields = Application.Field.values();
			} else {
				fields = new FormField[0];
			}
			return fields;
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
