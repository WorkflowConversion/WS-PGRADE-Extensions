package com.workflowconversion.portlet.ui.apptable.upload;

import java.io.File;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.workflowconversion.portlet.core.app.Application;

/**
 * Processes an uploaded XML file.
 * 
 * @author delagarza
 *
 */
public class XMLFileProcessor extends AbstractFileProcessor {

	public XMLFileProcessor(final File serverSideFile, final BulkUploadListener listener,
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
		Field currentField = null;
		Application currentApplication;
		final StringBuilder currentFieldValue = new StringBuilder();
		private final static String APPLICATION_NODE_NAME = "application";

		@Override
		public void setDocumentLocator(final Locator locator) {
			this.locator = locator;
		}

		@Override
		public void startElement(final String uri, final String localName, final String qName,
				final Attributes attributes) throws SAXException {
			if (qName.equalsIgnoreCase(APPLICATION_NODE_NAME)) {
				currentField = null;
				currentApplication = new Application();
			}
			// clear whatever has been stored, as we are about to start a new field
			currentFieldValue.setLength(0);
			if (qName.equalsIgnoreCase(Field.Description.name())) {
				currentField = Field.Description;
			}
			if (qName.equalsIgnoreCase(Field.Name.name())) {
				currentField = Field.Name;
			}
			if (qName.equalsIgnoreCase(Field.Path.name())) {
				currentField = Field.Path;
			}
			if (qName.equalsIgnoreCase(Field.Resource.name())) {
				currentField = Field.Resource;
			}
			if (qName.equalsIgnoreCase(Field.ResourceType.name())) {
				currentField = Field.ResourceType;
			}
			if (qName.equalsIgnoreCase(Field.Version.name())) {
				currentField = Field.Version;
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) throws SAXException {
			if (qName.equalsIgnoreCase(APPLICATION_NODE_NAME)) {
				XMLFileProcessor.this.addParsedApplication(currentApplication, locator.getLineNumber());
			}
			if (qName.equalsIgnoreCase(Field.Description.name())) {
				currentApplication.setDescription(getCleanCurrentFieldValue());
			}
			if (qName.equalsIgnoreCase(Field.Name.name())) {
				currentApplication.setName(getCleanCurrentFieldValue());
			}
			if (qName.equalsIgnoreCase(Field.Path.name())) {
				currentApplication.setPath(getCleanCurrentFieldValue());
			}
			if (qName.equalsIgnoreCase(Field.Resource.name())) {
				currentApplication.setResource(getCleanCurrentFieldValue());
			}
			if (qName.equalsIgnoreCase(Field.ResourceType.name())) {
				currentApplication.setResourceType(getCleanCurrentFieldValue());
			}
			if (qName.equalsIgnoreCase(Field.Version.name())) {
				currentApplication.setVersion(getCleanCurrentFieldValue());
			}
		}

		private String getCleanCurrentFieldValue() {
			return StringUtils.trimToEmpty(currentFieldValue.toString());
		}

		@Override
		public void characters(final char[] ch, final int start, final int length) throws SAXException {
			// ignore characters that don't belong to any field so far
			if (currentField != null) {
				switch (currentField) {
				case Description:
				case Name:
				case Path:
				case Resource:
				case ResourceType:
				case Version:
					currentFieldValue.append(new String(ch, start, length));
					break;
				default:
					// nop
					break;
				}
			}
		}
	}

}
