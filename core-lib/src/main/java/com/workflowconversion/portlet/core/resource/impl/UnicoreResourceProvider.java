package com.workflowconversion.portlet.core.resource.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unigrids.x2006.x04.services.tss.ApplicationResourceType;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import com.workflowconversion.portlet.core.exception.ApplicationException;
import com.workflowconversion.portlet.core.exception.ProviderNotEditableException;
import com.workflowconversion.portlet.core.middleware.MiddlewareProvider;
import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.core.resource.ResourceProvider;

import dci.data.Item;
import dci.data.Item.Unicore;
import de.fzj.unicore.uas.TargetSystemFactory;
import de.fzj.unicore.uas.client.TSFClient;
import de.fzj.unicore.uas.security.ClientProperties;
import de.fzj.unicore.wsrflite.security.ISecurityProperties;
import de.fzj.unicore.wsrflite.xmlbeans.client.RegistryClient;

/**
 * Application provider that interacts with UNICORE. This is not editable. It uses UNICORE's Java API to query the
 * Incarnation Database (IDB) of each of the configured UNICORE instances.
 * 
 * @author delagarza
 *
 */
public class UnicoreResourceProvider implements ResourceProvider {

	private static final long serialVersionUID = 6542266373514172909L;

	private final static Logger LOG = LoggerFactory.getLogger(UnicoreResourceProvider.class);

	/**
	 * Since it is not possible to add UNICORE applications, some classes might find this constant useful.
	 */
	public final static String UNICORE_RESOURCE_TYPE = "unicore";
	private final MiddlewareProvider middlewareProvider;

	/**
	 * Constructor.
	 * 
	 * @param middlewareProvider
	 *            A middleware provider.
	 */
	public UnicoreResourceProvider(final MiddlewareProvider middlewareProvider) {
		Validate.notNull(middlewareProvider, "middlewareProvider cannot be null");
		this.middlewareProvider = middlewareProvider;
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public Collection<Resource> getResources() {
		// get the available unicore items
		final Collection<Item> unicoreItems = middlewareProvider.getAvailableItems(UNICORE_RESOURCE_TYPE);

		// extract the applications from each item
		final Collection<Resource> resources = new LinkedList<Resource>();
		for (final Item item : unicoreItems) {
			extractResourcesFromUnicoreInstance(item, resources);
		}

		return resources;
	}

	private void extractResourcesFromUnicoreInstance(final Item item, final Collection<Resource> resources) {
		try {
			final Unicore unicoreConfigItem = item.getUnicore();
			final ClientProperties securityProperties = createClientProperties(unicoreConfigItem);
			final String resourceName = item.getName().trim();
			final RegistryClient registryClient = initRegistryClient(resourceName, unicoreConfigItem);
			final List<EndpointReferenceType> tsfEPRs = registryClient
					.listAccessibleServices(TargetSystemFactory.TSF_PORT);

			for (final EndpointReferenceType epr : tsfEPRs) {
				final String serverUrl = epr.getAddress().getStringValue().trim();
				final TSFClient tsf = new TSFClient(serverUrl, epr, securityProperties);
				final Resource resource = new Resource();
				resource.setType(UNICORE_RESOURCE_TYPE);
				resource.setName(resourceName);
				if (tsf != null && tsf.getResourcePropertiesDocument() != null) {
					for (final ApplicationResourceType unicoreApp : tsf.getResourcePropertiesDocument()
							.getTargetSystemFactoryProperties().getApplicationResourceArray()) {
						final Application app = new Application();
						app.setName(unicoreApp.getApplicationName());
						app.setVersion(unicoreApp.getApplicationVersion());
						app.setDescription(unicoreApp.getDescription());
						// UNICORE hides application details such as its path
						app.setPath("not available");
						resource.addApplication(app);
					}
				}
				resources.add(resource);

			}
		} catch (final Exception e) {
			LOG.error("Could not retrieve UNICORE applications from " + item.getName());
			throw new ApplicationException("Could not retrieve the applicatons for " + item.getName(), e);
		}
	}

	private RegistryClient initRegistryClient(final String resource, final Unicore unicoreConfigItem) throws Exception {
		final String url = "https://" + resource + "/REGISTRY/services/Registry?res=default_registry";
		final EndpointReferenceType epr = EndpointReferenceType.Factory.newInstance();
		epr.addNewAddress().setStringValue(url);
		return new RegistryClient(url, epr, createClientProperties(unicoreConfigItem));
	}

	// Adapted from hu.sztaki.lpds.pgportal.util.resource.UnicoreIDBToolHandler, see:
	// https://sourceforge.net/p/guse/git/ci/master/tree/wspgrade/src/main/java/hu/sztaki/lpds/pgportal/util/resource/UnicoreIDBToolHandler.java
	private ClientProperties createClientProperties(final Unicore unicoreConfigItem) {
		final ClientProperties clientProperties = new ClientProperties();

		clientProperties.setProperty(ISecurityProperties.WSRF_SSL, "true");
		clientProperties.setProperty(ISecurityProperties.WSRF_SSL_CLIENTAUTH, "true");
		clientProperties.setProperty(ISecurityProperties.WSRF_SSL_KEYSTORE, unicoreConfigItem.getKeystore());
		clientProperties.setProperty(ISecurityProperties.WSRF_SSL_KEYTYPE, "pkcs12");
		clientProperties.setProperty(ISecurityProperties.WSRF_SSL_KEYPASS, unicoreConfigItem.getKeypass());
		clientProperties.setProperty(ISecurityProperties.WSRF_SSL_KEYALIAS, unicoreConfigItem.getKeyalias());
		clientProperties.setProperty(ISecurityProperties.WSRF_SSL_TRUSTSTORE, unicoreConfigItem.getTruststore());
		clientProperties.setProperty(ISecurityProperties.WSRF_SSL_TRUSTPASS, unicoreConfigItem.getTrustpass());
		clientProperties.setProperty(ISecurityProperties.WSRF_SSL_TRUSTTYPE, "JKS");
		clientProperties.setSignMessage(true);

		return clientProperties;
	}

	@Override
	public void addResource(final Resource app) throws ProviderNotEditableException {
		throw new ProviderNotEditableException(
				"The UNICORE ApplicationProvider is not editable! This is an invalid operation.");
	}

	@Override
	public void saveResource(final Resource app) throws ProviderNotEditableException {
		throw new ProviderNotEditableException(
				"The UNICORE ApplicationProvider is not editable! This is an invalid operation.");
	}

	@Override
	public boolean containsResource(final Resource resource) {
		Validate.notNull(resource, "resource cannot be null");
		final Collection<Resource> resources = getResources();
		final String key = resource.generateKey();
		for (final Resource existentResource : resources) {
			if (key.equals(existentResource.generateKey())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void init() {
		// nop
	}

	@Override
	public String getName() {
		return "UNICORE application database";
	}

	@Override
	public void removeResource(final Resource resource) throws ProviderNotEditableException {
		throw new ProviderNotEditableException(
				"The UNICORE ApplicationProvider is not editable! This is an invalid operation.");
	}

	@Override
	public void removeAllResources() throws ProviderNotEditableException {
		throw new ProviderNotEditableException(
				"The UNICORE ApplicationProvider is not editable! This is an invalid operation.");
	}

	@Override
	public void commitChanges() throws ProviderNotEditableException {
		throw new ProviderNotEditableException(
				"The UNICORE ApplicationProvider is not editable! This is an invalid operation.");
	}

}
