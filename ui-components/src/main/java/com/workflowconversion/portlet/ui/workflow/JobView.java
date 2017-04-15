package com.workflowconversion.portlet.ui.workflow;

import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.HorizontalLayout;
import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.core.resource.Queue;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.core.utils.SystemWideKeyUtils;
import com.workflowconversion.portlet.core.workflow.Job;

/**
 * Displays a job.
 * 
 * @author delagarza
 *
 */
public class JobView extends HorizontalLayout {
	private static final long serialVersionUID = 8676895583929397206L;

	private static final String PROPERTY_QUEUE_CAPTION = "WorkflowView_property_queue_name";
	private static final String PROPERTY_APPLICATION_CAPTION = "WorkflowView_property_app_name";

	private final ComboBox applicationComboBox;
	private final ComboBox queueComboBox;
	private final Map<String, Application> applicationMap;
	private final String jobName;

	JobView(final Job job, final Map<String, Application> applicationMap) {
		this.applicationComboBox = getComboBox("Application", "Select an application");
		this.queueComboBox = getComboBox("Queue", "Select a queue");
		this.jobName = job.getName();
		this.applicationMap = applicationMap;
		initUI(job);
	}

	@SuppressWarnings("unchecked")
	private void initUI(final Job job) {
		applicationComboBox.setWidth(650, Unit.PIXELS);
		applicationComboBox.addContainerProperty(PROPERTY_APPLICATION_CAPTION, String.class, null);
		applicationComboBox.setItemCaptionPropertyId(PROPERTY_APPLICATION_CAPTION);

		queueComboBox.setWidth(200, Unit.PIXELS);
		queueComboBox.addContainerProperty(PROPERTY_QUEUE_CAPTION, String.class, null);
		queueComboBox.setItemCaptionPropertyId(PROPERTY_QUEUE_CAPTION);

		for (final Map.Entry<String, Application> entry : applicationMap.entrySet()) {
			final Application application = entry.getValue();
			final String key = entry.getKey();
			final Item applicationItem = applicationComboBox.addItem(key);
			applicationItem.getItemProperty(PROPERTY_APPLICATION_CAPTION)
					.setValue(generateApplicationCaption(application));
		}

		applicationComboBox.addValueChangeListener(new ValueChangeListener() {
			private static final long serialVersionUID = -6747726727427744762L;

			@Override
			public void valueChange(final ValueChangeEvent event) {
				final String applicationKey = (String) event.getProperty().getValue();
				final Application application = applicationMap.get(applicationKey);
				final Resource resource = application.getResource();

				queueComboBox.removeAllItems();
				if (resource.getQueues().isEmpty()) {
					queueComboBox.setEnabled(false);
				} else {
					queueComboBox.setEnabled(true);
					for (final Queue queue : resource.getQueues()) {
						final Item queueItem = queueComboBox.addItem(queue.generateKey());
						queueItem.getItemProperty(PROPERTY_QUEUE_CAPTION).setValue(queue.getName());
					}
				}
				queueComboBox.markAsDirtyRecursive();
			}
		});

		final Application jobApplication = job.getApplication();
		final Queue jobQueue = job.getQueue();
		if (jobApplication != null) {
			applicationComboBox.setValue(SystemWideKeyUtils.generate(jobApplication.getResource(), jobApplication));
		}
		if (jobQueue != null) {
			queueComboBox.setValue(jobQueue.generateKey());
		}

		setMargin(false);
		setSpacing(true);
		addComponent(applicationComboBox);
		addComponent(queueComboBox);
	}

	private Object generateApplicationCaption(final Application application) {
		final StringBuilder builder = new StringBuilder(application.getName());
		builder.append(" (").append(application.getVersion()).append(") on ")
				.append(application.getResource().getName());
		return builder.toString();
	}

	/**
	 * @return the job, as configured.
	 */
	Job getJob() {
		final Job job = new Job(jobName);
		final String systemWideApplicationKey = (String) applicationComboBox.getValue();
		if (StringUtils.isNotBlank(systemWideApplicationKey)) {
			final Application application = applicationMap.get(systemWideApplicationKey);
			job.setApplication(application);
			final String queueKey = (String) queueComboBox.getValue();
			if (StringUtils.isNotBlank(queueKey)) {
				final Resource resource = application.getResource();
				for (final Queue queue : resource.getQueues()) {
					if (queueKey.equals(queue.generateKey())) {
						job.setQueue(queue);
						break;
					}
				}
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
