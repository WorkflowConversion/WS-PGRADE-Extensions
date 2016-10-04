package com.workflowconversion.portlet.ui.apptable;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.QuoteMode;
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

	private final static String FILE_TYPE_TOOLTIP = "<i>TODO: add documentation for file type</i>";
	private final static String FIRST_ROW_AS_HEADER_TOOLTIP = "<i>TODO: add documentation for header</i>";
	private final static String QUOTED_VALUES_TOOLTIP = "<i>TODO: add documentation for quoted values</i>";
	private final static String COLUMN_DELIMITER_TOOLTIP = "<i>TODO: add documentation for column delimiter</i>";

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
		fileTypeComboBox.setDescription(FILE_TYPE_TOOLTIP);

		// csv-specific options
		final CheckBox firstRowAsHeader = new CheckBox("First row as header", true);
		firstRowAsHeader.setImmediate(true);
		firstRowAsHeader.setDescription(FIRST_ROW_AS_HEADER_TOOLTIP);

		final CheckBox quotedValues = new CheckBox("Values are quoted", false);
		quotedValues.setImmediate(true);
		quotedValues.setDescription(QUOTED_VALUES_TOOLTIP);

		final IndexedContainer columnDelimiterContainer = new IndexedContainer();
		columnDelimiterContainer.addContainerProperty(PROPERTY_NAME_CAPTION, String.class, null);
		final ComboBox columnDelimiterCombo = new ComboBox("Column delimiter", columnDelimiterContainer);
		columnDelimiterCombo.setNullSelectionAllowed(false);
		columnDelimiterCombo.setImmediate(true);
		columnDelimiterCombo.setItemCaptionPropertyId(PROPERTY_NAME_CAPTION);
		columnDelimiterCombo.setWidth(95, UNITS_PIXELS);
		columnDelimiterCombo.setDescription(COLUMN_DELIMITER_TOOLTIP);
		for (final Delimiter delimiter : Delimiter.values()) {
			final Item newItem = columnDelimiterCombo.addItem(delimiter);
			newItem.getItemProperty(PROPERTY_NAME_CAPTION)
					.setValue(delimiter.name() + ": " + delimiter.getDelimiterCharacter());
		}
		columnDelimiterCombo.select(Delimiter.Comma);

		final IndexedContainer indexedContainer = getIndexedContainerForHeaders();
		final Table csvHeaders = new Table("Change the desired column order by drag and dropping");
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
		csvOptions.addComponent(columnDelimiterCombo);
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
		final Label fileNameLabel = new Label();
		fileNameLabel.setCaption("File name");
		final ProgressIndicator progressIndicator = new ProgressIndicator();
		progressIndicator.setValue(0f);
		progressIndicator.setIndeterminate(false);
		progressIndicator.setEnabled(false);
		progressIndicator.setCaption("Progress");
		final Label verboseProgressLabel = new Label();

		final Layout statusDisplayLayout = new FormLayout();
		statusDisplayLayout.addComponent(currentStateLabel);
		statusDisplayLayout.addComponent(fileNameLabel);
		statusDisplayLayout.addComponent(progressIndicator);
		statusDisplayLayout.addComponent(verboseProgressLabel);
		final Panel statusDisplayPanel = new Panel("Status");
		statusDisplayPanel.addComponent(statusDisplayLayout);

		// set the listeners
		upload.addListener(new Upload.StartedListener() {
			private static final long serialVersionUID = -6254312847336221333L;

			@Override
			public void uploadStarted(final StartedEvent event) {
				errors.clear();
				contentLength.set(event.getContentLength());
				progressIndicator.setEnabled(true);
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
				progressIndicator.setEnabled(false);
				progressIndicator.setValue(0f);
				verboseProgressLabel.setValue("Parsed 0 lines");
				processFile(serverSideFile, currentStateLabel, verboseProgressLabel, progressIndicator,
						(FileType) fileTypeComboBox.getValue(), firstRowAsHeader.booleanValue(),
						quotedValues.booleanValue(), csvHeaders.getVisibleColumns(),
						(Delimiter) columnDelimiterCombo.getValue());
			}
		});

		upload.addListener(new Upload.FailedListener() {
			private static final long serialVersionUID = 4889495034113765498L;

			@Override
			public void uploadFailed(final FailedEvent event) {
				progressIndicator.setEnabled(false);
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
			final ProgressIndicator progressIndicator, final FileType fileType, final boolean firstRowAsHeader,
			final boolean quotedValues, final Object[] headers, final Delimiter delimiter) {
		final AbstractFileProcessor fileProcessor;
		switch (fileType) {
		case CSV:
			CSVFormat csvFormat = CSVFormat.DEFAULT;
			if (firstRowAsHeader) {
				csvFormat = csvFormat.withFirstRecordAsHeader();
			}
			if (quotedValues) {
				csvFormat = csvFormat.withQuoteMode(QuoteMode.ALL);
			}
			csvFormat = csvFormat.withDelimiter(delimiter.getDelimiterCharacter());
			final String[] orderedHeaders = new String[headers.length];
			for (int i = 0; i < headers.length; i++) {
				orderedHeaders[i] = ((ApplicationField) headers[i]).name();
			}
			csvFormat.withHeader(orderedHeaders);
			fileProcessor = new CSVFileProcessor(file, listener, progressIndicator, verboseProgressLabel, csvFormat,
					middlewareProvider.getAllMiddlewareTypes());
			break;
		case XML:
			fileProcessor = new XMLFileProcessor(file, listener, progressIndicator, verboseProgressLabel,
					middlewareProvider.getAllMiddlewareTypes());
		default:
			LOG.error("Upload file format not handled: " + fileType);
			throw new ApplicationException("Unrecognized file format " + fileType);
		}
		final Thread processThread = new Thread(fileProcessor);
		processThread.start();
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
		Comma(','), Tab('\t'), Pipe('|');

		private final char delimiterCharacter;

		private Delimiter(final char delimiterCharacter) {
			this.delimiterCharacter = delimiterCharacter;
		}

		public char getDelimiterCharacter() {
			return delimiterCharacter;
		}

	}
}
