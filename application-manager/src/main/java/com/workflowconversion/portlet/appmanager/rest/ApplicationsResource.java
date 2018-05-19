package com.workflowconversion.portlet.appmanager.rest;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.workflowconversion.portlet.core.Settings;
import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.core.resource.ResourceProvider;

/**
 * This class handles instances of {@link Resource}, but JAX-RS also defines a <i>Resource</i> as a class that contains
 * any of the JAX-RS annotations.
 * 
 * @author delagarza
 *
 */
@Path("apps")
public class ApplicationsResource {
	private final static Logger LOG = LoggerFactory.getLogger(ApplicationsResource.class);


	/**
	 * Gets the applications in XML format. We use the same format as the XML file to upload resources, e.g.:
	 * <pre>{@code
	 * <resources>
	 *   <resource name="pbs-cluster.university.eu" type="pbs">
	 *	   <application name="SampleApp" version="1.1" path="/usr/bin/sampleapp" description="Sample app"/>
	 *	   <application name="Sleepy" version="1.2" path="/usr/bin/sleep" description="Sample app, again"/>
	 *   </resource>
	 *   <resource name="moab-cluster.university.eu" type="moab">
	 *	   <application name="MagicSauce" version="2.1" path="/share/bin/magic" description="Nobel prize, here I come!"/>
	 *   </resource>
     * </resources>
	 * }</pre>
	 * @return
	 * @throws IOException
	 * @throws TransformerFactoryConfigurationError
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response getXml() throws IOException, TransformerFactoryConfigurationError, ParserConfigurationException,
			TransformerException {
		// yes, this is horrible, but we need just one method!
		// right tools for the right job ;)
		LOG.info("Servicing REST-API request (GET /apps)");

		final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		final Document document = documentBuilder.newDocument();
		final Element rootElement = document.createElement("resources");

		for (final ResourceProvider provider : Settings.getInstance().getResourceProviders()) {
			for (final Resource resource : provider.getResources()) {
				final Element resourceElement = document.createElement("resource");
				resourceElement.setAttribute("name", resource.getName());
				resourceElement.setAttribute("type", resource.getType());

				for (final Application app : resource.getApplications()) {
					final Element applicationElement = document.createElement("application");
					applicationElement.setAttribute("name", app.getName());
					applicationElement.setAttribute("version", app.getVersion());
					applicationElement.setAttribute("path", app.getPath());
					applicationElement.setAttribute("description", app.getDescription());
					resourceElement.appendChild(applicationElement);
				}

				rootElement.appendChild(resourceElement);
			}
		}
		document.appendChild(rootElement);

		final Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		final DOMSource source = new DOMSource(document);

		final Writer writer = new StringWriter();
		final StreamResult streamResult = new StreamResult(writer);
		transformer.transform(source, streamResult);

		return Response.ok(writer.toString(), MediaType.APPLICATION_JSON).build();
	}
}
