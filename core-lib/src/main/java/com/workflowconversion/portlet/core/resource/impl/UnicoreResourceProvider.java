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
	public boolean canAddApplications() {
		return false;
	}

	@Override
	public Collection<Resource> getResources() {
		// get the available unicore items
		final Collection<Item> unicoreItems = middlewareProvider.getEnabledItems(UNICORE_RESOURCE_TYPE);

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
				final Resource.Builder resourceBuilder = new Resource.Builder();
				resourceBuilder.withType(UNICORE_RESOURCE_TYPE).withName(resourceName);
				// UNICORE does not allow to add applications
				resourceBuilder.canModifyApplications(false);
				if (tsf != null && tsf.getResourcePropertiesDocument() != null) {
					final Collection<Application> extractedApplications = new LinkedList<Application>();
					for (final ApplicationResourceType unicoreApp : tsf.getResourcePropertiesDocument()
							.getTargetSystemFactoryProperties().getApplicationResourceArray()) {
						final Application.Builder applicationBuilder = new Application.Builder();
						applicationBuilder.withName(unicoreApp.getApplicationName());
						applicationBuilder.withVersion(unicoreApp.getApplicationVersion());
						applicationBuilder.withDescription(unicoreApp.getDescription());
						// UNICORE hides application details such as its path
						applicationBuilder.withPath("not available");
						extractedApplications.add(applicationBuilder.newInstance());
					}
					resourceBuilder.withApplications(extractedApplications);
				}
				resources.add(resourceBuilder.newInstance());

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
	public void init() {
		// nop
	}

	@Override
	public String getName() {
		return "UNICORE application database";
	}

	@Override
	public void saveApplications() {
		// nop
	}
}
