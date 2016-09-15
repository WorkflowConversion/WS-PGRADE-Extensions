package com.workflowconversion.importer.guse.appdb.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unigrids.x2006.x04.services.tss.ApplicationResourceType;
import org.w3.x2005.x08.addressing.EndpointReferenceType;

import com.workflowconversion.importer.guse.Settings;
import com.workflowconversion.importer.guse.appdb.Application;
import com.workflowconversion.importer.guse.appdb.ApplicationProvider;
import com.workflowconversion.importer.guse.exception.ApplicationException;
import com.workflowconversion.importer.guse.exception.NotEditableApplicationProviderException;
import com.workflowconversion.importer.guse.middleware.MiddlewareProvider;

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
public class UnicoreApplicationProvider implements ApplicationProvider {

	private final static Logger LOG = LoggerFactory.getLogger(UnicoreApplicationProvider.class);

	/**
	 * Since it is not possible to add UNICORE applications, some classes might find this constant useful.
	 */
	public final static String UNICORE_RESOURCE_TYPE = "unicore";

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public Collection<Application> getApplications() {
		// get the available unicore items
		final MiddlewareProvider middlewareProvider = Settings.getInstance().getMiddlewareProvider();
		final Collection<Item> unicoreItems = middlewareProvider.getAvailableItems(UNICORE_RESOURCE_TYPE);

		// extract the applications from each item
		final Collection<Application> applications = new LinkedList<Application>();
		for (final Item item : unicoreItems) {
			extractAppsFromUnicoreInstance(item, applications);
		}

		return applications;
	}

	private void extractAppsFromUnicoreInstance(final Item item, final Collection<Application> applications) {
		try {
			final Unicore unicoreConfigItem = item.getUnicore();
			final ClientProperties securityProperties = createClientProperties(unicoreConfigItem);
			final String resource = item.getName().trim();
			final RegistryClient registryClient = initRegistryClient(resource, unicoreConfigItem);
			final List<EndpointReferenceType> tsfEPRs = registryClient
					.listAccessibleServices(TargetSystemFactory.TSF_PORT);

			for (final EndpointReferenceType epr : tsfEPRs) {
				final String serverUrl = epr.getAddress().getStringValue().trim();
				final TSFClient tsf = new TSFClient(serverUrl, epr, securityProperties);
				int nApplications = 0;
				if (tsf != null && tsf.getResourcePropertiesDocument() != null) {
					for (final ApplicationResourceType unicoreApp : tsf.getResourcePropertiesDocument()
							.getTargetSystemFactoryProperties().getApplicationResourceArray()) {
						final Application app = new Application();
						// set some id, since we don't get any from UNICORE
						app.setId(resource + "_app_id_" + nApplications++);
						app.setName(unicoreApp.getApplicationName());
						app.setVersion(unicoreApp.getApplicationVersion());
						app.setDescription(unicoreApp.getDescription());
						app.setResource(resource);
						app.setResourceType(UNICORE_RESOURCE_TYPE);
						// UNICORE hides application details such as its path
						app.setPath("not available");
						applications.add(app);
					}
				}

			}
		} catch (Exception e) {
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
	public void addApplication(Application app) throws NotEditableApplicationProviderException {
		throw new NotEditableApplicationProviderException(
				"The UNICORE ApplicationProvider is not editable! This is an invalid operation.");
	}

	@Override
	public void saveApplication(Application app) throws NotEditableApplicationProviderException {
		throw new NotEditableApplicationProviderException(
				"The UNICORE ApplicationProvider is not editable! This is an invalid operation.");
	}

	@Override
	public boolean needsInit() {
		return false;
	}

	@Override
	public void init() {
		// nop
	}

	@Override
	public String getName() {
		return "UNICORE application database (read-only)";
	}

	@Override
	public void removeApplication(Application app) throws NotEditableApplicationProviderException {
		throw new NotEditableApplicationProviderException(
				"The UNICORE ApplicationProvider is not editable! This is an invalid operation.");
	}

}
