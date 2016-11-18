package com.workflowconversion.portlet.ui.apptable.upload;

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
import com.workflowconversion.portlet.ui.NotificationUtils;
import com.workflowconversion.portlet.ui.apptable.ApplicationCommittedListener;

/**
 * Modal dialog through which a CSV file containing applications can be uploaded.
 * 
 * @author delagarza
 *
 */
public class BulkUploadApplicationsDialog extends Window {

	private static final String PROPERTY_NAME_CAPTION = "caption";

	private static final long serialVersionUID = -2587575066352088585L;

	private final static Logger LOG = LoggerFactory.getLogger(BulkUploadApplicationsDialog.class);

	private final static String FILE_TYPE_TOOLTIP_HELP = "<i>TODO: add documentation for file type</i>";
	private final static String FIRST_ROW_AS_HEADER_TOOLTIP_HELP = "<i>TODO: add documentation for header</i>";
	private final static String QUOTED_VALUES_TOOLTIP_HELP = "<i>TODO: add documentation for quoted values</i>";
	private final static String COLUMN_DELIMITER_TOOLTIP_HELP = "<i>TODO: add documentation for column delimiter</i>";

	private File serverSideFile;
	private final AtomicLong contentLength;
	private final MiddlewareProvider middlewareProvider;
	private final Collection<String> errors;
	private final Label currentStatusLabel;
	private final ApplicationCommittedListener applicationCommittedListener;
	private final Upload upload;

	/**
	 * Constructor.
	 * 
	 * @param middlewareProvider
	 *            middleware provider (used to obtain the valid resource types).
	 * @param applicationCommittedListener
	 *            a listener that will be informed of parsed, valid, uploaded applications.
	 */
	public BulkUploadApplicationsDialog(final MiddlewareProvider middlewareProvider,
			final ApplicationCommittedListener applicationCommittedListener) {
		Validate.notNull(middlewareProvider, "middlewareProvider cannot be null");
		Validate.notNull(applicationCommittedListener, "applicationCommittedListener cannot be null");
		this.middlewareProvider = middlewareProvider;
		this.applicationCommittedListener = applicationCommittedListener;

		this.contentLength = new AtomicLong();
		this.errors = new LinkedList<String>();
		this.currentStatusLabel = new Label();
		this.upload = new Upload();

		setCaption("Upload CSV File with Applications");
		setModal(true);
		setUpLayout();
	}

	private void setUpLayout() {
		// file options
		final ComboBox fileTypeComboBox = new ComboBox("File type", Arrays.asList(FileType.values()));
		fileTypeComboBox.setNullSelectionAllowed(false);
		fileTypeComboBox.setImmediate(true);
		fileTypeComboBox.setWidth(70, Unit.PIXELS);
		fileTypeComboBox.select(FileType.CSV);
		fileTypeComboBox.setDescription(FILE_TYPE_TOOLTIP_HELP);

		// csv-specific options
		final CheckBox firstRowAsHeader = new CheckBox("First row as header", false);
		firstRowAsHeader.setImmediate(true);
		firstRowAsHeader.setDescription(FIRST_ROW_AS_HEADER_TOOLTIP_HELP);

		final CheckBox quotedValues = new CheckBox("Values are quoted", false);
		quotedValues.setImmediate(true);
		quotedValues.setDescription(QUOTED_VALUES_TOOLTIP_HELP);

		final IndexedContainer columnDelimiterContainer = new IndexedContainer();
		columnDelimiterContainer.addContainerProperty(PROPERTY_NAME_CAPTION, String.class, null);
		final ComboBox columnDelimiterCombo = new ComboBox("Column delimiter", columnDelimiterContainer);
		columnDelimiterCombo.setNullSelectionAllowed(false);
		columnDelimiterCombo.setImmediate(true);
		columnDelimiterCombo.setItemCaptionPropertyId(PROPERTY_NAME_CAPTION);
		columnDelimiterCombo.setWidth(95, Unit.PIXELS);
		columnDelimiterCombo.setDescription(COLUMN_DELIMITER_TOOLTIP_HELP);
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
		csvHeaders.setBuffered(false);
		csvHeaders.setImmediate(true);
		csvHeaders.setHeight(80, Unit.PIXELS);
		csvHeaders.setContainerDataSource(indexedContainer);
		// add a sample item
		csvHeaders.getContainerDataSource().addItem(0);

		// hide the headers table when "first row as header" is selected
		firstRowAsHeader.addValueChangeListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = 7360860662293281984L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				csvHeaders.setVisible(!(Boolean) event.getProperty().getValue());
			}
		});

		final Layout csvOptions = new HorizontalLayout();
		csvOptions.addComponent(firstRowAsHeader);
		csvOptions.addComponent(quotedValues);
		csvOptions.addComponent(columnDelimiterCombo);
		csvOptions.setWidth(100, Unit.PERCENTAGE);

		fileTypeComboBox.addValueChangeListener(new Property.ValueChangeListener() {
			private static final long serialVersionUID = -4635999804925764582L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				if (FileType.CSV.equals(event.getProperty().getValue())) {
					csvOptions.setVisible(true);
					csvHeaders.setVisible(!firstRowAsHeader.getValue());
				} else {
					csvOptions.setVisible(false);
					csvHeaders.setVisible(false);
				}
			}

		});

		final Layout fileTypeLayout = new FormLayout();
		fileTypeLayout.addComponent(fileTypeComboBox);
		final Layout panelLayout = new VerticalLayout();
		panelLayout.addComponent(fileTypeLayout);
		panelLayout.addComponent(csvOptions);
		panelLayout.addComponent(csvHeaders);
		final Panel fileOptionsPanel = new Panel("File options");
		fileOptionsPanel.setContent(panelLayout);

		// upload control
		final Receiver fileReceiver = new FileReceiver();
		upload.setReceiver(fileReceiver);
		upload.setImmediate(true);
		upload.setButtonCaption("Upload file...");

		// status/progress panel
		currentStatusLabel.setImmediate(true);
		currentStatusLabel.setCaption("Current state");
		currentStatusLabel.setValue("Waiting for file");

		final Layout statusDisplayLayout = new FormLayout();
		statusDisplayLayout.addComponent(currentStatusLabel);
		final Panel statusDisplayPanel = new Panel("Status");
		statusDisplayPanel.setContent(statusDisplayLayout);

		// set the listeners
		upload.addStartedListener(new Upload.StartedListener() {
			private static final long serialVersionUID = -6254312847336221333L;

			@Override
			public void uploadStarted(final StartedEvent event) {
				upload.setEnabled(false);
				errors.clear();
				contentLength.set(event.getContentLength());
				currentStatusLabel.setValue("Uploading file");
			}
		});

		upload.addProgressListener(new Upload.ProgressListener() {
			private static final long serialVersionUID = 6805421002888765593L;

			@Override
			public void updateProgress(final long readBytes, final long contentLength) {
				currentStatusLabel.setValue("Read " + readBytes + " of " + contentLength + " bytes");
			}
		});

		upload.addSucceededListener(new Upload.SucceededListener() {
			private static final long serialVersionUID = -2722969929750545998L;

			@Override
			public void uploadSucceeded(final SucceededEvent event) {
				currentStatusLabel.setValue("Parsing file");
				processFile(serverSideFile, currentStatusLabel, (FileType) fileTypeComboBox.getValue(),
						firstRowAsHeader.getValue(), quotedValues.getValue(), csvHeaders.getVisibleColumns(),
						(Delimiter) columnDelimiterCombo.getValue());
			}
		});

		upload.addFailedListener(new Upload.FailedListener() {
			private static final long serialVersionUID = 4889495034113765498L;

			@Override
			public void uploadFailed(final FailedEvent event) {
				currentStatusLabel.setValue("Upload failed");
				NotificationUtils.displayError("Could not upload file.", event.getReason());
				upload.setEnabled(true);
			}
		});

		final VerticalLayout layout = (VerticalLayout) getContent();
		layout.addComponent(fileOptionsPanel);
		layout.addComponent(upload);
		layout.addComponent(new HorizontalSeparator());
		layout.addComponent(statusDisplayPanel);
		layout.setMargin(true);
		layout.setWidth(600, Unit.PIXELS);
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

	void processFile(final File file, final Label currentStatusLabel, final FileType fileType,
			final boolean firstRowAsHeader, final boolean quotedValues, final Object[] headers,
			final Delimiter delimiter) {
		final BulkUploadListener bulkUploadListener = new DefaultBulkUploadListener(currentStatusLabel,
				applicationCommittedListener);
		final AbstractFileProcessor fileProcessor;
		switch (fileType) {
		case CSV:
			CSVFormat csvFormat = CSVFormat.DEFAULT.withCommentMarker('#');

			final String[] orderedHeaders = new String[headers.length];
			for (int i = 0; i < headers.length; i++) {
				orderedHeaders[i] = ((ApplicationField) headers[i]).name();
			}
			if (firstRowAsHeader) {
				csvFormat = csvFormat.withFirstRecordAsHeader();
			} else {
				csvFormat = csvFormat.withHeader(orderedHeaders);
			}

			if (quotedValues) {
				csvFormat = csvFormat.withQuoteMode(QuoteMode.ALL);
			}

			csvFormat = csvFormat.withDelimiter(delimiter.getDelimiterCharacter());

			fileProcessor = new CSVFileProcessor(file, bulkUploadListener, csvFormat,
					middlewareProvider.getAllMiddlewareTypes());
			break;
		case XML:
			fileProcessor = new XMLFileProcessor(file, bulkUploadListener, middlewareProvider.getAllMiddlewareTypes());
			break;
		default:
			LOG.error("Upload file format not handled: " + fileType);
			throw new ApplicationException("Unrecognized file format " + fileType);
		}
		// this has to be done in the same thread, otherwise, repaint requests from other thread
		// will not be processed!
		fileProcessor.start();
	}

	private class DefaultBulkUploadListener implements BulkUploadListener {

		private final Label statusLabel;
		private final ApplicationCommittedListener applicationCommittedListener;
		private final Collection<String> errors;

		private DefaultBulkUploadListener(final Label statusLabel,
				final ApplicationCommittedListener applicationCommittedListener) {
			this.statusLabel = statusLabel;
			this.applicationCommittedListener = applicationCommittedListener;
			this.errors = new LinkedList<String>();
		}

		@Override
		public void parsingStarted() {
			statusLabel.setValue("Parsing uploaded file, please be patient.");
		}

		@Override
		public void parsingError(final String error, final long lineNumber) {
			errors.add("Line " + lineNumber + ": " + error);
		}

		@Override
		public void parsingError(final String error) {
			errors.add(error);
		}

		@Override
		public void parsingCompleted(final Collection<Application> parsedApplications) {
			int nAddedApplications = 0;
			try {
				statusLabel.setValue("Parsed " + parsedApplications.size() + " application(s).");
				for (final Application parsedApplication : parsedApplications) {
					try {
						applicationCommittedListener.applicationCommitted(parsedApplication);
						nAddedApplications++;
					} catch (Exception e) {
						LOG.error("Could not add application " + parsedApplication, e);
						errors.add("Could not add application " + parsedApplication + ", reason: " + e.getMessage());
					}
				}
			} finally {
				try {
					if (errors.isEmpty()) {
						final String message = "Parsed and added " + nAddedApplications
								+ " applications without errors.";
						statusLabel.setValue(message);
						NotificationUtils.displayTrayMessage(message);
					} else {
						final String message = "Parsed and added " + nAddedApplications + " applications, but found "
								+ errors.size() + " error(s).";
						statusLabel.setValue(message);
						final StringBuilder formattedError = new StringBuilder(
								"<h3>The following errors occurred while processing the uploaded file:</h3><ul>");
						for (final String error : errors) {
							formattedError.append("<li>").append(error);
						}
						formattedError.append("</ul>");
						NotificationUtils.displayError(formattedError.toString());
					}
				} finally {
					upload.setEnabled(true);
				}
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