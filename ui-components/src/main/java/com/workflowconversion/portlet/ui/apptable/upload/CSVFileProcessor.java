package com.workflowconversion.portlet.ui.apptable.upload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.Validate;

import com.workflowconversion.portlet.core.app.Application;
import com.workflowconversion.portlet.core.app.ApplicationField;

/**
 * Processes an uploaded CSV file.
 * 
 * @author delagarza
 *
 */
public class CSVFileProcessor extends AbstractFileProcessor {

	private final CSVFormat csvFormat;

	public CSVFileProcessor(final File serverSideFile, final BulkUploadListener listener, final CSVFormat csvFormat,
			final Set<String> validMiddlewareTypes) {
		super(serverSideFile, listener, validMiddlewareTypes);
		Validate.notNull(csvFormat, "csvFormat cannot be null");
		this.csvFormat = csvFormat;
	}

	@Override
	void parseFile(final File serverSideFile) throws Exception {
		final BufferedReader reader = new BufferedReader(new FileReader(serverSideFile));
		final CSVParser parser = csvFormat.parse(reader);
		for (final CSVRecord record : parser) {
			try {
				final Application parsedApplication = new Application();
				parsedApplication.setDescription(record.get(ApplicationField.Description));
				parsedApplication.setName(record.get(ApplicationField.Name));
				parsedApplication.setPath(record.get(ApplicationField.Path));
				parsedApplication.setResource(record.get(ApplicationField.Resource));
				parsedApplication.setResourceType(record.get(ApplicationField.ResourceType));
				parsedApplication.setVersion(record.get(ApplicationField.Version));
				addParsedApplication(parsedApplication, parser.getCurrentLineNumber());
			} catch (Exception e) {
				this.listener.parsingError(e.getMessage(), parser.getCurrentLineNumber());
			}
		}
	}
}
