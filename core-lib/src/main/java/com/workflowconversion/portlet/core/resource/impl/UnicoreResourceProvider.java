package com.workflowconversion.portlet.core.resource.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unigrids.x2006.x04.services.tss.ApplicationResourceType;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.workflowconversion.portlet.core.exception.ApplicationException;
import com.workflowconversion.portlet.core.middleware.MiddlewareProvider;
import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.core.resource.ResourceProvider;
import com.workflowconversion.portlet.core.utils.KeyUtils;

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
	/**
	 * Since it is not possible to add UNICORE applications, some classes might find this constant useful.
	 */
	public final static String UNICORE_RESOURCE_TYPE = "unicore";

	private final static long serialVersionUID = 6542266373514172909L;
	private final static Logger LOG = LoggerFactory.getLogger(UnicoreResourceProvider.class);

	private final Supplier<Map<String, Resource>> cachedResources;
	private final MiddlewareProvider middlewareProvider;

	private volatile boolean hasErrors;

	/**
	 * Constructor.
	 * 
	 * @param middlewareProvider
	 *            A middleware provider.
	 * @param cacheDuration
	 *            the duration of the cache, in seconds.
	 */
	public UnicoreResourceProvider(final MiddlewareProvider middlewareProvider, final int cacheDuration) {
		Validate.notNull(middlewareProvider, "middlewareProvider cannot be null");
		Validate.isTrue(cacheDuration > 0, "invalid cacheDuration " + cacheDuration);
		this.hasErrors = false;
		this.middlewareProvider = middlewareProvider;
		this.cachedResources = Suppliers.memoizeWithExpiration(new Supplier<Map<String, Resource>>() {

			@Override
			public Map<String, Resource> get() {
				return getResources_internal();
			}

		}, cacheDuration, TimeUnit.SECONDS);
	}

	private Map<String, Resource> getResources_internal() {
		LOG.info("Refreshing UNICORE resources cache.");
		// get the available unicore items
		final Collection<Item> unicoreItems = middlewareProvider.getEnabledItems(UNICORE_RESOURCE_TYPE);

		// extract the applications from each item
		final Map<String, Resource> resources = new TreeMap<String, Resource>();
		for (final Item item : unicoreItems) {
			extractResourcesFromUnicoreInstance(item, resources);
		}

		return resources;
	}

	@Override
	public boolean canAddApplications() {
		return false;
	}

	@Override
	public Collection<Resource> getResources() {
		return Collections.unmodifiableCollection(cachedResources.get().values());
	}

	@Override
	public Resource getResource(final String name, final String type) {
		return cachedResources.get().get(KeyUtils.generateResourceKey(name, type));
	}

	private void extractResourcesFromUnicoreInstance(final Item item, final Map<String, Resource> resources) {
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
				final Resource loadedResource = resourceBuilder.newInstance();
				resources.put(KeyUtils.generate(loadedResource), loadedResource);

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
		try {
			// if this is the first invocation, which it should be, it will force a cache load
			cachedResources.get();
		} catch (final Exception e) {
			hasErrors = true;
			LOG.error("The UNICORE Resource Provider could not be initialized.", e);
		}
	}

	@Override
	public boolean hasInitErrors() {
		return hasErrors;
	}

	@Override
	public String getName() {
		return "UNICORE Application Database";
	}

	@Override
	public void save(final Resource resource) {
		throw new ApplicationException(
				"This provider does not support adding/editing applications. This is probably a coding problem and should be reported.");
	}

	@Override
	public void merge(final Collection<Resource> resources) {
		throw new ApplicationException(
				"This provider does not support adding/editing applications. This is probably a coding problem and should be reported.");
	}

}
