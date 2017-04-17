package com.workflowconversion.portlet.ui;

import com.vaadin.server.ThemeResource;
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
public class SimpleContent extends VerticalLayout {

	private static final long serialVersionUID = 7192534030109710873L;

	private final String iconLocation;
	private final int iconWidth;
	private final int iconHeight;
	private final String message;
	private final String style;

	// the only way to get instances of this class is via its builder class
	private SimpleContent(final String iconLocation, final int iconWidth, final int iconHeight, final String message,
			final String style) {
		this.iconLocation = iconLocation;
		this.iconWidth = iconWidth;
		this.iconHeight = iconHeight;
		this.message = message;
		this.style = style;
		setUpUI();
	}

	private void setUpUI() {
		// TODO: Fix scrolling... maybe use a textarea?
		final Panel panel = new Panel();
		final Label label = new Label(message, ContentMode.HTML);
		panel.setContent(label);
		panel.getContent().setSizeUndefined();
		panel.setSizeFull();

		final HorizontalLayout layout = new HorizontalLayout();
		if (iconLocation != null) {
			final Embedded icon = new Embedded(null, new ThemeResource(iconLocation));
			icon.setWidth(Integer.toString(iconWidth) + "px");
			icon.setHeight(Integer.toString(iconHeight) + "px");
			layout.addComponent(icon);
		}
		layout.addComponent(panel);

		if (style != null) {
			panel.addStyleName(style);
			layout.addStyleName(style);
			addStyleName(style);
		}
		addComponent(layout);
	}

	/**
	 * Builder for {@link SimpleContent} class. Instead of building custom warning windows by using the constructor with
	 * its many parameters, a builder class is provided.
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
		private String iconLocation = null;
		private int iconWidth = 64;
		private int iconHeight = 64;
		private String style = null;
		private String message = null;

		/**
		 * Sets the icon location. This will be used as a {@link ThemeResource}, so make sure to provide a path relative
		 * to the portlet.
		 * 
		 * @param iconLocation
		 *            The icon location, relative to the portlet, e.g., {@code ../runo/icons/64/attention.png}.
		 * @return this instance of the {@link Builder}.
		 */
		public Builder withIconLocation(final String iconLocation) {
			this.iconLocation = iconLocation;
			return this;
		}

		/**
		 * Sets the icon's dimensions.
		 * 
		 * @param iconWidth
		 *            The icon height, in pixels.
		 * @param iconHeight
		 *            The icon height, in pixels.
		 * @return this instance of the {@link Builder}.
		 */
		public Builder withIconDimensions(final int iconWidth, final int iconHeight) {
			this.iconWidth = iconWidth;
			this.iconHeight = iconHeight;
			return this;
		}

		/**
		 * Sets the message to display.
		 * 
		 * @param message
		 *            The message. HTML tags are allowed.
		 * @return this instance of the {@link Builder}.
		 */
		public Builder withMessage(final String message) {
			this.message = message;
			return this;
		}

		/**
		 * Sets the style of the content.
		 * 
		 * @param stlye
		 *            The css style to use.
		 * @return this instance of the {@link Builder}.
		 */
		public Builder withStyle(final String style) {
			this.style = style;
			return this;
		}

		/**
		 * Builds a new {@link SimpleContent} with the current parameters.
		 * 
		 * @return A new instance of a {@link SimpleContent}.
		 */
		public SimpleContent newContent() {
			return new SimpleContent(this.iconLocation, this.iconWidth, this.iconHeight, this.message, this.style);
		}

	}

}