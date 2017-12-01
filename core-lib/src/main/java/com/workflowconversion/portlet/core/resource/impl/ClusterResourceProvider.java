package com.workflowconversion.portlet.core.resource.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

import org.apache.commons.lang.Validate;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workflowconversion.portlet.core.SupportedClusters;
import com.workflowconversion.portlet.core.exception.ApplicationException;
import com.workflowconversion.portlet.core.filter.Filter;
import com.workflowconversion.portlet.core.middleware.MiddlewareProvider;
import com.workflowconversion.portlet.core.resource.Queue;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.core.resource.ResourceProvider;

import dci.data.Item;
import dci.data.Middleware;
import hu.sztaki.lpds.information.local.PropertyLoader;

/**
 * Resource provider for cluster-based providers.
 * 
 * The {@code dci_bridge} provides a way to
 * 
 * @author delagarza
 *
 */
public class ClusterResourceProvider implements ResourceProvider {

	private static final long serialVersionUID = -3799340787944829350L;
	private final static Logger LOG = LoggerFactory.getLogger(ClusterResourceProvider.class);

	private volatile boolean hasInitErrors;
	// this is not final because it will be set when the init() method is invoked
	private DataSource dataSource;

	/**
	 * @param middlewareProvider
	 *            the middleware provider.
	 * @param jaxbApplicationsXmlFileLocation
	 *            the location on which the applications will be stored.
	 */
	public ClusterResourceProvider(final MiddlewareProvider middlewareProvider) {
		Validate.notNull(middlewareProvider,
				"middlewareProvider cannot be null. This seems to be a coding problem and should be reported.");
		this.hasInitErrors = false;
	}

	@Override
	public boolean canAddApplications() {
		return true;
	}

	@Override
	public String getName() {
		return "WS-PGRADE Cluster Middlewares";
	}

	@Override
	public synchronized void init() {
		if (dataSource != null) {
			LOG.info("ClusterResourcerProvider has already been initialized, ignoring invocation of init() method.");
			return;
		}
		LOG.info("Initializing ClusterResourceProvider");
		try {
			// taken from: https://people.apache.org/~fhanik/jdbc-pool/jdbc-pool.html
			final PoolProperties p = new PoolProperties();
			// these values come from invoking gUSE webservices
			p.setUrl(PropertyLoader.getInstance().getProperty("guse.system.database.url"));
			// we know it's MySQL, but there's no need to hardcode these settings
			p.setDriverClassName(PropertyLoader.getInstance().getProperty("guse.system.database.driver"));
			p.setUsername(PropertyLoader.getInstance().getProperty("guse.system.database.user"));
			p.setPassword(PropertyLoader.getInstance().getProperty("guse.system.database.password"));
			// TODO: maybe put these in web.xml or somewhere else? at least maxActive?
			p.setJmxEnabled(true);
			p.setTestWhileIdle(false);
			p.setTestOnBorrow(true);
			p.setValidationQuery("SELECT 1");
			p.setTestOnReturn(false);
			p.setValidationInterval(30000);
			p.setTimeBetweenEvictionRunsMillis(30000);
			p.setMaxActive(10);
			p.setInitialSize(1);
			p.setMaxWait(10000);
			p.setRemoveAbandonedTimeout(60);
			p.setMinEvictableIdleTimeMillis(30000);
			p.setLogAbandoned(true);
			p.setRemoveAbandoned(true);
			dataSource = new DataSource();
			dataSource.setPoolProperties(p);
		} catch (final Exception e) {
			throw new ApplicationException("Could not initialize ClusterResourceProvider.", e);
		}
	}

	@Override
	public boolean hasInitErrors() {
		return hasInitErrors;
	}

	@Override
	public Collection<Resource> getResources() {
		// TODO: use MySQL
		return Collections.emptyList();
	}

	@Override
	public Resource getResource(final String name, final String type) {
		// TODO: use MySQL
		final Resource.Builder builder = new Resource.Builder();
		builder.withName(name);
		builder.withType(type);
		return builder.newInstance();
	}

	@Override
	public void save(final Resource resource) {
		// TODO: use MySQL (or maybe remove this method?)
	}

	@Override
	public void merge(final Collection<Resource> resources) {
		// TODO: do some fancy MySQL stuff to merge the hell out of these resources
	}

	private Collection<Queue> extractQueuesFromItem_notThreadSafe(final String middlewareType, final Item item) {
		final Collection<String> extractedQueueNames;
		switch (SupportedClusters.valueOf(middlewareType)) {
		// could be done with reflection, but it's just four lines of code and reflection would make this code
		// even harder to read
		case lsf:
			extractedQueueNames = item.getLsf().getQueue();
			break;
		case moab:
			extractedQueueNames = item.getMoab().getQueue();
			break;
		case pbs:
			extractedQueueNames = item.getPbs().getQueue();
			break;
		case sge:
			extractedQueueNames = item.getSge().getQueue();
			break;
		default:
			throw new IllegalArgumentException("Cannot handle items of type " + middlewareType);
		}
		final Collection<Queue> extractedQueues = new LinkedList<Queue>();
		for (final String extractedQueueName : extractedQueueNames) {
			final Queue.Builder queueBuilder = new Queue.Builder();
			queueBuilder.withName(extractedQueueName);
			extractedQueues.add(queueBuilder.newInstance());
		}
		return extractedQueues;
	}

	private final static class ClusterMiddlewareFilter implements Filter<Middleware> {
		@Override
		public boolean passes(final Middleware element) {
			return SupportedClusters.isSupported(element.getType());
		}
	}
}
