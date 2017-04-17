package com.workflowconversion.portlet.core.resource.jaxb;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.Validate;

import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.core.utils.KeyUtils;

/**
 * JAXB-friendly class containing a resource map.
 * 
 * @author delagarza
 *
 */
@XmlRootElement(name = "resourcesDB")
@XmlAccessorType(XmlAccessType.FIELD)
public class JAXBResourceDatabase {

	@XmlJavaTypeAdapter(ResourceMapXmlAdapter.class)
	private final Map<String, Resource> resources = new TreeMap<String, Resource>();

	/**
	 * @return the resource map.
	 */
	public Collection<Resource> getResources() {
		return Collections.unmodifiableCollection(resources.values());
	}

	/**
	 * @param resources
	 *            the resources to add.
	 */
	public void addResources(final Collection<Resource> resources) {
		Validate.notNull(resources, "resources cannot be null, this is a coding problem and should be reported.");
		for (final Resource resource : resources) {
			this.resources.put(KeyUtils.generate(resource), resource);
		}
	}

}
