package com.workflowconversion.portlet.ui.workflow;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.core.resource.Queue;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.core.resource.ResourceProvider;
import com.workflowconversion.portlet.core.workflow.Job;

/**
 * Displays a job.
 * 
 * @author delagarza
 *
 */
public class JobView extends HorizontalLayout {
	private static final long serialVersionUID = 8676895583929397206L;

	private final static Logger LOG = LoggerFactory.getLogger(JobView.class);

	private final static String PROPERTY_APPLICATION_CAPTION = "WorkflowView_property_app_name";
	private final static String PROPERTY_APPLICATION = "WorkflowView_property_app";
	private final static String PROPERTY_RESOURCE = "WorkflowView_property_resource";
	private final static String PROPERTY_QUEUE_CAPTION = "WorkflowView_property_queue_name";
	private final static String PROPERTY_QUEUE = "WorkflowView_property_queue";

	private final ComboBox applicationComboBox;
	private final ComboBox queueComboBox;
	private final Collection<ResourceProvider> resourceProviders;
	private final String jobName;

	JobView(final Job job, final Collection<ResourceProvider> resourceProviders) {
		this.applicationComboBox = getComboBox("Application", "Select an application");
		this.queueComboBox = getComboBox("Queue", "Select a queue");
		this.jobName = job.getName();
		this.resourceProviders = resourceProviders;
		initUI(job);
	}

	// FIXME: comboboxes selection of saved jobs is not working
	@SuppressWarnings("unchecked")
	private void initUI(final Job job) {
		applicationComboBox.setWidth(650, Unit.PIXELS);
		applicationComboBox.addContainerProperty(PROPERTY_APPLICATION_CAPTION, String.class, null);
		applicationComboBox.addContainerProperty(PROPERTY_APPLICATION, Application.class, null);
		applicationComboBox.addContainerProperty(PROPERTY_RESOURCE, Resource.class, null);
		applicationComboBox.setItemCaptionPropertyId(PROPERTY_APPLICATION_CAPTION);

		queueComboBox.setWidth(200, Unit.PIXELS);
		queueComboBox.addContainerProperty(PROPERTY_QUEUE_CAPTION, String.class, null);
		queueComboBox.addContainerProperty(PROPERTY_QUEUE, Queue.class, null);
		queueComboBox.setItemCaptionPropertyId(PROPERTY_QUEUE_CAPTION);

		for (final ResourceProvider resourceProvider : resourceProviders) {
			for (final Resource resource : resourceProvider.getResources()) {
				for (final Application application : resource.getApplications()) {
					final Object itemKey = applicationComboBox.addItem();
					final Item item = applicationComboBox.getItem(itemKey);
					item.getItemProperty(PROPERTY_APPLICATION).setValue(application);
					item.getItemProperty(PROPERTY_RESOURCE).setValue(resource);
					item.getItemProperty(PROPERTY_APPLICATION_CAPTION)
							.setValue(generateApplicationCaption(resource, application));
				}
			}
		}

		applicationComboBox.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = -6747726727427744762L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				final Item selectedApplicationItem = applicationComboBox.getItem(event.getProperty().getValue());
				final Resource resource = (Resource) selectedApplicationItem.getItemProperty(PROPERTY_RESOURCE)
						.getValue();

				queueComboBox.removeAllItems();
				if (resource.getQueues().isEmpty()) {
					queueComboBox.setEnabled(false);
				} else {
					queueComboBox.setEnabled(true);
					for (final Queue queue : resource.getQueues()) {
						final Object queueItemKey = queueComboBox.addItem();
						final Item queueItem = queueComboBox.getItem(queueItemKey);
						queueItem.getItemProperty(PROPERTY_QUEUE).setValue(queue);
						queueItem.getItemProperty(PROPERTY_QUEUE_CAPTION).setValue(queue.getName());
					}
				}
				queueComboBox.markAsDirtyRecursive();
			}
		});

		if (LOG.isDebugEnabled()) {
			LOG.debug("Finding resource, application, queue for job=" + job);
		}

		final Resource jobResource = job.getResource();
		final Application jobApplication = job.getApplication();
		if (jobResource != null && jobApplication != null) {
			for (final Object applicationItemKey : applicationComboBox.getItemIds()) {
				final Item applicationItem = applicationComboBox.getItem(applicationItemKey);
				final Resource resource = (Resource) applicationItem.getItemProperty(PROPERTY_RESOURCE).getValue();
				final Application application = (Application) applicationItem.getItemProperty(PROPERTY_APPLICATION)
						.getValue();
				if (LOG.isDebugEnabled()) {
					LOG.debug("Checking resource=" + resource == null ? "null"
							: resource.getName() + ", application=" + application);
				}
				if (application == jobApplication && jobResource == resource) {
					LOG.debug("Found resource, application for job.");
					applicationComboBox.setValue(applicationItemKey);
					break;
				}
			}
		}
		final Queue jobQueue = job.getQueue();
		if (jobQueue != null) {
			for (final Object queueItemKey : queueComboBox.getItemIds()) {
				final Item queueItem = queueComboBox.getItem(queueItemKey);
				final Queue queue = (Queue) queueItem.getItemProperty(PROPERTY_QUEUE).getValue();
				if (LOG.isDebugEnabled()) {
					LOG.debug("Checking queue=" + queue);
				}
				if (queue == jobQueue) {
					LOG.debug("Found queue, application for job.");
					queueComboBox.setValue(queueItemKey);
					break;
				}
			}
		}

		setMargin(false);
		setSpacing(true);
		addComponent(applicationComboBox);
		addComponent(queueComboBox);
	}

	private Object generateApplicationCaption(final Resource resource, final Application application) {
		final StringBuilder builder = new StringBuilder(application.getName());
		builder.append(" (").append(application.getVersion()).append(") on ").append(resource.getName());
		return builder.toString();
	}

	/**
	 * @return the job, as configured.
	 */
	Job getJob() {
		final Job job = new Job(jobName);
		final Object selectedApplicationItemKey = applicationComboBox.getValue();
		if (selectedApplicationItemKey != null) {
			final Item selectedApplicationItem = applicationComboBox.getItem(selectedApplicationItemKey);
			final Application selectedApplication = (Application) selectedApplicationItem
					.getItemProperty(PROPERTY_APPLICATION).getValue();
			final Resource selectedResource = (Resource) selectedApplicationItem.getItemProperty(PROPERTY_RESOURCE)
					.getValue();
			job.setApplication(selectedApplication);
			job.setResource(selectedResource);
			final Object selectedQueueItemKey = queueComboBox.getValue();
			if (selectedQueueItemKey != null) {
				final Item selectedQueueItem = queueComboBox.getItem(selectedQueueItemKey);
				final Queue selectedQueue = (Queue) selectedQueueItem.getItemProperty(PROPERTY_QUEUE).getValue();
				job.setQueue(selectedQueue);
			}
		}
		return job;
	}

	private ComboBox getComboBox(final String caption, final String description) {
		final ComboBox comboBox = new ComboBox();
		comboBox.setCaptionAsHtml(true);
		comboBox.setCaption("<h3>" + caption + "</h3>");
		comboBox.setNullSelectionAllowed(false);
		comboBox.setImmediate(true);
		comboBox.setDescription(description);
		comboBox.setInputPrompt(description);
		return comboBox;
	}
}
