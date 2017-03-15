package com.workflowconversion.portlet.ui;

import com.vaadin.server.ErrorMessage.ErrorLevel;
import com.vaadin.server.Page;
import com.vaadin.server.ThemeResource;
import com.vaadin.server.UserError;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;

/**
 * Vaadin UI window that is displayed instead of the main content.
 * 
 * @author delagarza
 */
public class SimpleWarningContent extends VerticalLayout {

	private static final long serialVersionUID = 7192534030109710873L;

	private final String iconLocation;
	private final int iconWidth;
	private final int iconHeight;
	private final String shortDescription;
	private final String longDescription;
	private final Notification.Type notificationType;

	// the only way to get instances of this class is via its builder class
	private SimpleWarningContent(final String iconLocation, final int iconWidth, final int iconHeight,
			final String shortDescription, final String longDescription, final Notification.Type notificationType) {
		this.iconLocation = iconLocation;
		this.iconWidth = iconWidth;
		this.iconHeight = iconHeight;
		this.shortDescription = shortDescription;
		this.longDescription = longDescription;
		this.notificationType = notificationType;
		setUpUI();
	}

	private void setUpUI() {

		final Embedded icon = new Embedded(null, new ThemeResource(iconLocation));
		icon.setWidth(Integer.toString(iconWidth) + "px");
		icon.setHeight(Integer.toString(iconHeight) + "px");
		final Label warningLabel = new Label("<h3>" + longDescription + "</h3>", ContentMode.HTML);

		final HorizontalLayout layout = new HorizontalLayout();
		layout.addComponent(icon);
		layout.addComponent(warningLabel);

		final Panel panel = new Panel("We're sorry");
		panel.setComponentError(new UserError("<h3>" + shortDescription + "</h3>",
				com.vaadin.server.AbstractErrorMessage.ContentMode.HTML, ErrorLevel.ERROR));
		panel.setContent(layout);

		addComponent(panel);
		final Notification notification = new Notification("We're sorry", "<h2>" + shortDescription + "</h2>",
				notificationType);
		notification.setHtmlContentAllowed(true);
		notification.show(Page.getCurrent());
	}

	/**
	 * Builder for {@link SimpleWarningContent} class. Instead of building custom warning windows by using the
	 * constructor with its many parameters, a builder class is provided.
	 * 
	 * Defaults:
	 * <ul>
	 * <li>{@code iconLocation} = {@code ../runo/icons/64/attention.png}
	 * <li>{@code iconWidth} = {@code 64}
	 * <li>{@code iconHeight} = {@code 64}
	 * <li>{@code shortDescription} = {@code Content is currently not available.}
	 * <li>{@code longDescription} =
	 * {@code There was an error in your request. Please inform the administrator of the portal.}
	 * <li>{@code notificationType} = {@link Notification#TYPE_ERROR_MESSAGE}
	 * </ul>
	 * 
	 * Use the several <i>setter</i> methods to modify the defaults.
	 * 
	 * @author delagarza
	 *
	 */
	public static class Builder {
		private String iconLocation = "../runo/icons/64/attention.png";
		private int iconWidth = 64;
		private int iconHeight = 64;
		private String shortDescription = "Content is currently not available.";
		private Notification.Type notificationType = Notification.Type.ERROR_MESSAGE;
		private String longDescription = "There was an error in your request. Please inform the administrator of the portal.";

		/**
		 * Sets the icon location. This will be used as a {@link ThemeResource}, so make sure to provide a path relative
		 * to the portlet.
		 * 
		 * @param iconLocation
		 *            The icon location, relative to the portlet, e.g., {@code ../runo/icons/64/attention.png}.
		 * @return this instance of the {@link Builder}.
		 */
		public Builder setIconLocation(final String iconLocation) {
			this.iconLocation = iconLocation;
			return this;
		}

		/**
		 * Sets the icon width.
		 * 
		 * @param iconWidth
		 *            The icon width, in pixels.
		 * @return this instance of the {@link Builder}.
		 */
		public Builder setIconWidth(final int iconWidth) {
			this.iconWidth = iconWidth;
			return this;
		}

		/**
		 * Sets the icon height.
		 * 
		 * @param iconHeight
		 *            The icon height, in pixels.
		 * @return this instance of the {@link Builder}.
		 */
		public Builder setIconHeight(final int iconHeight) {
			this.iconHeight = iconHeight;
			return this;
		}

		/**
		 * Sets the short description.
		 * 
		 * @param shortDescription
		 *            The short description.
		 * @return this instance of the {@link Builder}.
		 */
		public Builder setShortDescription(final String shortDescription) {
			this.shortDescription = shortDescription;
			return this;
		}

		/**
		 * Sets the long description.
		 * 
		 * @param longDescription
		 *            The long description. HTML tags are allowed.
		 * @return this instance of the {@link Builder}.
		 */
		public Builder setLongDescription(final String longDescription) {
			this.longDescription = longDescription;
			return this;
		}

		/**
		 * Sets the notification type.
		 * 
		 * @param notificationType
		 *            One of the possible notification types as defined in {@link Notification.Type}
		 *
		 * @return this instance of the {@link Builder}.
		 */
		public Builder setNotificationType(final Notification.Type notificationType) {
			this.notificationType = notificationType;
			return this;
		}

		/**
		 * Builds a new {@link SimpleWarningContent} with the current parameters.
		 * 
		 * @return A new instance of a {@link SimpleWarningContent}.
		 */
		public SimpleWarningContent newWarningWindow() {
			return new SimpleWarningContent(this.iconLocation, this.iconWidth, this.iconHeight, this.shortDescription,
					this.longDescription, this.notificationType);
		}

	}

}