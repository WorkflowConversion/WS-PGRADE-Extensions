package com.workflowconversion.importer.middleware.impl;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workflowconversion.importer.exception.ApplicationException;
import com.workflowconversion.importer.filter.Filter;
import com.workflowconversion.importer.filter.impl.FilterFactory;
import com.workflowconversion.importer.middleware.MiddlewareProvider;

import dci.data.Item;
import dci.data.Middleware;
import hu.sztaki.lpds.dcibridge.client.ResourceConfigurationFace;
import hu.sztaki.lpds.information.local.InformationBase;

/**
 * Implementation of {@link MiddlewareProvider} that uses webservices to query gUSE about middlewares.
 * 
 * @author delagarza
 *
 */
public class WebServiceBasedMiddlewareProvider implements MiddlewareProvider {

	private final static Logger LOG = LoggerFactory.getLogger(WebServiceBasedMiddlewareProvider.class);

	// this is that dci-bride.xml looks like
	// there are multiple <middleware> items, but there should be only one with type "unicore"
	// inside of the middleware of type unicore there will be several <item> nodes, the name of these
	// <item>s contains the server name and port on which UNICORE will be listening for requests
	// the <unicore> child node of the <item> element contains the certificate information
	// ...
	// the gUSE webservices return this same structure replicated in JAXB-generated objects
	/**
	 * <pre>
	 <middleware type="unicore" enabled="true">
		 <item name="unicore.uni-tuebingen.de:8080" enabled="true">
		 	<unicore>
				<keystore>keystore.p12</keystore>
				<keypass>pass</keypass>
				<keyalias>mosgrid</keyalias>
				<subjectdn></subjectdn>
				<truststore>trust.jks</truststore>
				<trustpass>pass</trustpass>
			</unicore>
			<forward usethis="false">
			<wsdl/>
			</forward>
		 </item>
		 
		 <item name="flavus.informatik.uni-tuebingen.de:8090" enabled="true">
			 <unicore>
				 <keystore>/home/guseuser/certificates/knime2guse.p12</keystore>
				 <keypass>knime2guse_pass5</keypass>
				 <keyalias>knime2guse</keyalias>
				 <subjectdn>CN=knime2guse.informatik.uni-tuebingen.de, OU=Universitaet Tuebingen, O=GridGermany, C=DE</subjectdn>
				 <truststore>/home/guseuser/certificates/truststore.jks</truststore>
				 <trustpass>grid-ca_pass5</trustpass>
			 </unicore>
			 <forward usethis="true"/>
		 </item>
		 <certificate>saml</certificate>
		 <plugin>hu.sztaki.lpds.submitter.grids.Grid_unicore</plugin>
		 <threads>1</threads>
		 <disrescont>false</disrescont>
		 <resubmit>0</resubmit>
	 </middleware>
	 * </pre>
	 */

	@Override
	public Collection<Middleware> getAvailableMiddlewares() {
		// filter only using availability
		final FilterFactory middlewareFilter = new FilterFactory();
		middlewareFilter.setEnabled(true);
		return middlewareFilter.newMiddlewareFilter().apply(getAllMiddlewares());
	}

	@Override
	public Collection<Middleware> getAvailableMiddlewares(final String middlewareType) {
		Validate.isTrue(StringUtils.isNotBlank(middlewareType),
				"middlewareType cannot be null, empty or contain only whitespaces, this is probably a bug and should be reported.");
		// filter using availability and type
		final FilterFactory middlewareFilter = new FilterFactory();
		middlewareFilter.setEnabled(true).setType(middlewareType);
		return middlewareFilter.newMiddlewareFilter().apply(getAllMiddlewares());
	}

	@Override
	public Collection<Item> getAvailableItems(final String middlewareType) {
		// get the middlewares first
		Validate.isTrue(StringUtils.isNotBlank(middlewareType),
				"middlewareType cannot be null, empty or contain only whitespaces, this is probably a bug and should be reported.");
		// filter using availability and type
		final Filter<Middleware> middlewareFilter = new FilterFactory().setEnabled(true).setType(middlewareType)
				.newMiddlewareFilter();
		final Collection<Middleware> availableMiddlewares = middlewareFilter.apply(getAllMiddlewares());

		// filter the available items for each of the available middlewares
		final Collection<Item> availableItems = new LinkedList<Item>();
		final Filter<Item> itemFilter = new FilterFactory().setEnabled(true).newItemFilter();
		for (final Middleware availableMiddleware : availableMiddlewares) {
			availableItems.addAll(itemFilter.apply(availableMiddleware.getItem()));
		}

		return availableItems;
	}

	@Override
	public Collection<Middleware> getAllMiddlewares() {
		final ResourceConfigurationFace rc;
		try {
			rc = (ResourceConfigurationFace) InformationBase.getI().getServiceClient("resourceconfigure", "portal");
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			LOG.error("An error occured while trying to get the resource configuration", e);
			throw new ApplicationException("An error occured while trying to get the resource configuration", e);
		}
		// the Middleware class maps the <middleware> elements in dci-bridge.xml
		final List<Middleware> middlewares;
		try {
			middlewares = rc.get();
		} catch (Exception e) {
			LOG.error("An error occured while reding the resource configuration", e);
			throw new ApplicationException("An error occured while reding the resource configuration", e);
		}
		return middlewares;
	}
}
