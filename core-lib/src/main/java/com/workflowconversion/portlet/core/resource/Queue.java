package com.workflowconversion.portlet.core.resource;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

/**
 * Simple wrapper for resource queues.
 * 
 * @author delagarza
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Queue implements Comparable<Queue>, Serializable, HasKey {
	private static final long serialVersionUID = -1202346412388738016L;

	@XmlAttribute
	private String name = "";
	@XmlTransient
	private Resource resource;

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(final String name) {
		Validate.isTrue(StringUtils.isNotBlank(name), "name cannot be null, empty or contain only whitespaces.");
		this.name = name;
	}

	/**
	 * @return the resource
	 */
	public Resource getResource() {
		return resource;
	}

	/**
	 * @param resource
	 *            the resource to set
	 */
	public void setResource(final Resource resource) {
		this.resource = resource;
	}

	@Override
	public String generateKey() {
		return "_name=" + name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Queue other = (Queue) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "Queue [name=" + name + "]";
	}

	@Override
	public int compareTo(final Queue other) {
		return name.compareTo(other.name);
	}

	public enum Field implements FormField {
		Name(64, "name", "Name");

		private final int maxLength;
		private final String memberName;
		private final String displayName;

		private Field(final int maxLength, final String memberName, final String displayName) {
			this.maxLength = maxLength;
			this.memberName = memberName;
			this.displayName = displayName;
		}

		@Override
		public int getMaxLength() {
			return maxLength;
		}

		@Override
		public String getMemberName() {
			return memberName;
		}

		@Override
		public String getDisplayName() {
			return displayName;
		}
	}

}
