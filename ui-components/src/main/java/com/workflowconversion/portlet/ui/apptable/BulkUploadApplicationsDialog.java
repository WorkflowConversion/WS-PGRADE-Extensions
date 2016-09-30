package com.workflowconversion.portlet.ui.apptable;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Layout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.ProgressIndicator;
import com.vaadin.ui.Table;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.FailedEvent;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.StartedEvent;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.workflowconversion.portlet.core.app.Application;
import com.workflowconversion.portlet.core.app.ApplicationField;
import com.workflowconversion.portlet.core.exception.ApplicationException;
import com.workflowconversion.portlet.core.middleware.MiddlewareProvider;
import com.workflowconversion.portlet.ui.HorizontalSeparator;

/**
 * Modal dialog through which a CSV file containing applications can be uploaded.
 * 
 * @author delagarza
 *
 */
class BulkUploadApplicationsDialog extends Window {

	private static final String PROPERTY_NAME_CAPTION = "caption";

	private static final long serialVersionUID = -2587575066352088585L;

	private final static Logger LOG = LoggerFactory.getLogger(BulkUploadApplicationsDialog.class);

	private File serverSideFile;
	private final AtomicLong contentLength;
	private final MiddlewareProvider middlewareProvider;
	private final ApplicationCommittedListener listener;
	private final Collection<String> errors;

	BulkUploadApplicationsDialog(final MiddlewareProvider middlewareProvider,
			final ApplicationCommittedListener listener) {
		Validate.notNull(middlewareProvider, "middlewareProvider cannot be null");
		Validate.notNull(listener, "listener cannot be null");
		this.middlewareProvider = middlewareProvider;
		this.listener = listener;
		this.contentLength = new AtomicLong();
		this.errors = new LinkedList<String>();
		setCaption("Upload CSV File with Applications");
		setModal(true);
		setUpLayout();
	}

	private void setUpLayout() {
		// file options
		final ComboBox fileTypeComboBox = new ComboBox("File type", Arrays.asList(FileType.values()));
		fileTypeComboBox.setNullSelectionAllowed(false);
		fileTypeComboBox.setImmediate(true);
		fileTypeComboBox.setWidth(70, UNITS_PIXELS);
		fileTypeComboBox.select(FileType.CSV);

		// csv-specific options
		final CheckBox firstRowAsHeader = new CheckBox("First row as header", true);
		firstRowAsHeader.setImmediate(true);
		final CheckBox quotedValues = new CheckBox("Values are quoted", false);
		quotedValues.setImmediate(true);
		final IndexedContainer separatorContainer = new IndexedContainer();
		separatorContainer.addContainerProperty(PROPERTY_NAME_CAPTION, String.class, null);
		final ComboBox separatorCombo = new ComboBox("Column delimiter", separatorContainer);
		separatorCombo.setNullSelectionAllowed(false);
		separatorCombo.setImmediate(true);
		separatorCombo.setItemCaptionPropertyId(PROPERTY_NAME_CAPTION);
		separatorCombo.setWidth(95, UNITS_PIXELS);
		int separatorId = 0;
		for (final Delimiter delimiter : Delimiter.values()) {
			final Item newItem = separatorCombo.addItem(separatorId++);
			newItem.getItemProperty(PROPERTY_NAME_CAPTION)
					.setValue(delimiter.name() + ": " + delimiter.getDelimiterAsString());
		}
		final IndexedContainer indexedContainer = getIndexedContainerForHeaders();
		final Table csvHeaders = new Table("Change the column order by drag and dropping");
		csvHeaders.setEditable(false);
		csvHeaders.setMultiSelect(false);
		csvHeaders.setColumnReorderingAllowed(true);
		csvHeaders.setWriteThrough(true);
		csvHeaders.setImmediate(true);
		csvHeaders.setHeight(80, UNITS_PIXELS);
		csvHeaders.setContainerDataSource(indexedContainer);
		// add a sample item
		csvHeaders.getContainerDataSource().addItem(0);

		final Layout csvOptions = new HorizontalLayout();
		csvOptions.addComponent(firstRowAsHeader);
		csvOptions.addComponent(quotedValues);
		csvOptions.addComponent(separatorCombo);
		csvOptions.setWidth(100, UNITS_PERCENTAGE);

		fileTypeComboBox.addListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = -4635999804925764582L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				if (FileType.CSV.equals(event.getProperty().getValue())) {
					csvOptions.setVisible(true);
					csvHeaders.setVisible(true);
				} else {
					csvOptions.setVisible(false);
					csvHeaders.setVisible(false);
				}
			}

		});

		final Layout fileTypeLayout = new FormLayout();
		fileTypeLayout.addComponent(fileTypeComboBox);
		final Panel fileOptionsPanel = new Panel("File options");
		fileOptionsPanel.addComponent(fileTypeLayout);
		fileOptionsPanel.addComponent(csvOptions);
		fileOptionsPanel.addComponent(csvHeaders);

		// upload control
		final Receiver fileReceiver = new FileReceiver();
		final Upload upload = new Upload(null, fileReceiver);
		upload.setImmediate(true);
		upload.setButtonCaption("Upload file...");

		// status/progress panel
		final Label currentStateLabel = new Label();
		currentStateLabel.setCaption("Current state");
		currentStateLabel.setWidth(100, UNITS_PERCENTAGE);
		final Label fileNameLabel = new Label();
		fileNameLabel.setCaption("File name");
		fileNameLabel.setWidth(100, UNITS_PERCENTAGE);
		final ProgressIndicator progressIndicator = new ProgressIndicator();
		progressIndicator.setValue(0f);
		progressIndicator.setWidth(100, UNITS_PERCENTAGE);
		progressIndicator.setCaption("Progress");
		final Label verboseProgressLabel = new Label();
		verboseProgressLabel.setWidth(100, UNITS_PERCENTAGE);

		final Layout statusDisplayLayout = new FormLayout();
		statusDisplayLayout.addComponent(currentStateLabel);
		statusDisplayLayout.addComponent(fileNameLabel);
		statusDisplayLayout.addComponent(progressIndicator);
		statusDisplayLayout.addComponent(verboseProgressLabel);
		statusDisplayLayout.setWidth(100, UNITS_PERCENTAGE);
		final Panel statusDisplayPanel = new Panel("Status", statusDisplayLayout);
		statusDisplayPanel.setWidth(100, UNITS_PERCENTAGE);

		// set the listeners
		upload.addListener(new Upload.StartedListener() {
			private static final long serialVersionUID = -6254312847336221333L;

			@Override
			public void uploadStarted(final StartedEvent event) {
				errors.clear();
				contentLength.set(event.getContentLength());
				progressIndicator.setValue(0f);
				progressIndicator.setPollingInterval(500);
				currentStateLabel.setValue("Uploading file");
				fileNameLabel.setValue(event.getFilename());
				verboseProgressLabel.setValue("Uploaded 0 of ... bytes");
			}
		});

		upload.addListener(new Upload.ProgressListener() {
			private static final long serialVersionUID = 6805421002888765593L;

			@Override
			public void updateProgress(final long readBytes, final long contentLength) {
				progressIndicator.setValue(((float) readBytes) / contentLength);
				verboseProgressLabel.setValue("Uploaded " + readBytes + " of " + contentLength + " bytes");
			}
		});

		upload.addListener(new Upload.SucceededListener() {
			private static final long serialVersionUID = -2722969929750545998L;

			@Override
			public void uploadSucceeded(final SucceededEvent event) {
				currentStateLabel.setValue("Parsing file");
				progressIndicator.setValue(0f);
				verboseProgressLabel.setValue("Parsed 0 lines");
				processFile(serverSideFile, currentStateLabel, verboseProgressLabel, progressIndicator);
			}
		});

		upload.addListener(new Upload.FailedListener() {
			private static final long serialVersionUID = 4889495034113765498L;

			@Override
			public void uploadFailed(final FailedEvent event) {
				currentStateLabel.setValue("Upload failed");
				showNotification("Upload Applications",
						"Could not upload file. Reason: " + event.getReason().getMessage(),
						Notification.TYPE_ERROR_MESSAGE);
			}
		});

		final VerticalLayout layout = (VerticalLayout) getContent();
		layout.addComponent(fileOptionsPanel);
		layout.addComponent(upload);
		layout.addComponent(new HorizontalSeparator());
		layout.addComponent(statusDisplayPanel);
		layout.setMargin(true);
		layout.setWidth(600, UNITS_PIXELS);
		layout.setSpacing(true);
	}

	private IndexedContainer getIndexedContainerForHeaders() {
		final IndexedContainer indexedContainer = new IndexedContainer();
		indexedContainer.addContainerProperty(ApplicationField.Name, String.class, "lib.sample");
		indexedContainer.addContainerProperty(ApplicationField.Version, String.class, "1.0");
		indexedContainer.addContainerProperty(ApplicationField.Description, String.class, "magic!");
		indexedContainer.addContainerProperty(ApplicationField.Path, String.class, "/usr/bin");
		indexedContainer.addContainerProperty(ApplicationField.Resource, String.class, "server.org");
		indexedContainer.addContainerProperty(ApplicationField.ResourceType, String.class, "moab");
		return indexedContainer;
	}

	void processFile(final File file, final Label currentStateLabel, final Label verboseProgressLabel,
			final ProgressIndicator progressIndicator) {
		final Thread processThread = new Thread(new FileProcessor(file, progressIndicator, verboseProgressLabel));
		processThread.start();
	}

	private class FileProcessor implements Runnable {

		private final File serverSideFile;
		private final Collection<Application> parsedApplications;
		private final ProgressIndicator progressIndicator;
		private final Label verboseProgressLabel;
		private final Set<String> validMiddlewareTypes;
		// private final CSVFormat csvFormat;

		FileProcessor(final File serverSideFile, final ProgressIndicator progressIndicator,
				final Label verboseProgressLabel) {
			this.serverSideFile = serverSideFile;
			this.progressIndicator = progressIndicator;
			this.verboseProgressLabel = verboseProgressLabel;
			this.parsedApplications = new LinkedList<Application>();
			this.validMiddlewareTypes = middlewareProvider.getAllMiddlewareTypes();
		}

		@Override
		public void run() {
			parseFile();
			commitApplications();
		}

		private void parseFile() {
			try (final BufferedReader reader = new BufferedReader(new FileReader(serverSideFile))) {
				long processedBytes = 0;
				long lineNumber = 1;
				String line = reader.readLine();
				final StringBuilder parsingErrors = new StringBuilder();
				while (line != null) {
					// highly inefficient...
					processedBytes += line.getBytes().length;

					final Application parsedApplication = parseApplication(line, parsingErrors);
					if (parsedApplication != null) {
						parsedApplications.add(parsedApplication);
					} else {
						// application could not be parsed
						errors.add(
								"Could not parse line number " + lineNumber + ", reason: " + parsingErrors.toString());
						parsingErrors.setLength(0);
					}

					progressIndicator.setValue(((float) processedBytes) / contentLength.get());
					verboseProgressLabel.setValue("Parsed " + lineNumber + " lines");

					line = reader.readLine();
					lineNumber++;
				}

			} catch (IOException e) {
				LOG.error("There was an error while parsing the uploaded file", e);
				throw new ApplicationException("There was an error while parsing the uploaded file");
			}
		}

		private Application parseApplication(final String line, final StringBuilder parsingErrors) {
			return null;
		}

		private void commitApplications() {
			for (final Application parsedApplication : parsedApplications) {
				listener.applicationCommitted(parsedApplication);
			}
		}

	}

	private class FileReceiver implements Receiver {

		private static final long serialVersionUID = 5729540081679884127L;

		@Override
		public OutputStream receiveUpload(final String filename, final String mimeType) {
			// create a temporary file, server-side
			try {
				serverSideFile = File.createTempFile("upload_", null);
				return new BufferedOutputStream(new FileOutputStream(serverSideFile));
			} catch (IOException e) {
				throw new ApplicationException("Could not upload file", e);
			}
		}
	}

	private enum FileType {
		CSV, XML
	}

	private enum Delimiter {
		Comma(","), Tab("\\t"), Pipe("|");

		private final String delimiterStr;

		private Delimiter(final String delimiterStr) {
			this.delimiterStr = delimiterStr;
		}

		public String getDelimiterAsString() {
			return delimiterStr;
		}

	}
}
