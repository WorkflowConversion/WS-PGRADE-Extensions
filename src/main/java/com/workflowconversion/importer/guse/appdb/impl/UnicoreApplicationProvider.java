package com.workflowconversion.importer.guse.appdb.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workflowconversion.importer.guse.appdb.Application;
import com.workflowconversion.importer.guse.appdb.ApplicationProvider;
import com.workflowconversion.importer.guse.exception.ApplicationException;
import com.workflowconversion.importer.guse.exception.NotEditableApplicationProviderException;

import dci.data.Item;
import dci.data.Item.Unicore;
import dci.data.Middleware;
import hu.sztaki.lpds.dcibridge.client.ResourceConfigurationFace;
import hu.sztaki.lpds.information.local.InformationBase;

/**
 * Application provider that interacts with UNICORE. This is not editable.
 * 
 * @author delagarza
 *
 */
public class UnicoreApplicationProvider implements ApplicationProvider {

	private final static Logger LOG = LoggerFactory.getLogger(UnicoreApplicationProvider.class);

	public UnicoreApplicationProvider() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public Collection<Application> getApplications() {
		final Collection<Application> applications = new LinkedList<Application>();
		try {
			// get the list of middlewares, as found in dci-bridge.xml
			final ResourceConfigurationFace rc = (ResourceConfigurationFace) InformationBase.getI()
					.getServiceClient("resourceconfigure", "portal");
			// the Middleware class maps the <middleware> elements in dci-bridge.xml
			final List<Middleware> middlewares = rc.get();
			// find the unicore middleware
			Middleware unicoreMiddleware = null;
			for (final Middleware m : middlewares) {
				if ("unicore".equals(m.getType())) {
					unicoreMiddleware = m;
				}
			}
			if (unicoreMiddleware == null) {
				if (LOG.isInfoEnabled()) {
					LOG.info("The dci_bridge_service has not been configured to interact with UNICORE");
				}
			} else {
				// go through all of <item> entries
				final List<Item> unicoreItems = unicoreMiddleware.getItem();
				for (final Item item : unicoreItems) {
					if (item.isEnabled()) {
						extractAppsFromUnicoreInstance(item, applications);
					} else {
						if (LOG.isInfoEnabled()) {
							LOG.info("UNICORE instance " + item.getName()
									+ " found, but it is inactive, so it will be ignored.");
						}
					}
				}
			}
		} catch (Exception e) {
			throw new ApplicationException("An error occurred while retrieving UNICORE applications", e);
		}
		return applications;
	}

	private void extractAppsFromUnicoreInstance(final Item item, final Collection<Application> applications) {
		final Unicore unicore = item.getUnicore();

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
	public Collection<Application> searchApplicationsByName(String name) {
		return null;
	}

	@Override
	public boolean needsInit() {
		return false;
	}

	@Override
	public void init() {
		// nop
	}

}
