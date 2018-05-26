package com.workflowconversion.portlet.core.resource.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.Validate;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workflowconversion.portlet.core.SupportedClusters;
import com.workflowconversion.portlet.core.exception.ApplicationException;
import com.workflowconversion.portlet.core.filter.Filter;
import com.workflowconversion.portlet.core.filter.FilterApplicator;
import com.workflowconversion.portlet.core.filter.impl.SimpleFilterFactory;
import com.workflowconversion.portlet.core.middleware.MiddlewareProvider;
import com.workflowconversion.portlet.core.resource.Application;
import com.workflowconversion.portlet.core.resource.Queue;
import com.workflowconversion.portlet.core.resource.Resource;
import com.workflowconversion.portlet.core.resource.ResourceProvider;
import com.workflowconversion.portlet.core.utils.KeyUtils;
import com.workflowconversion.portlet.core.utils.ScriptRunner;

import dci.data.Item;
import dci.data.Middleware;
import hu.sztaki.lpds.information.local.PropertyLoader;

/**
 * Resource provider for cluster-based providers. It uses the same MySQL database as gUSE in order to store information
 * about applications.
 * 
 * During the initialization, this provider will establish a JDBC connection to the MySQL database and will execute a
 * setup SQL script (SQL) that will create the needed MySQL objects if it's needed to.
 * 
 * @author delagarza
 *
 */
public class ClusterResourceProvider implements ResourceProvider {

	private static final long serialVersionUID = -3799340787944829350L;
	private final static Logger LOG = LoggerFactory.getLogger(ClusterResourceProvider.class);

	private final static String SETUP_SCRIPT_LOCATION = "setupdb.sql";
	private final static String GET_SQL = "{CALL sp_get_applications(?, ?)}";
	private final static String ADD_SQL = "{CALL sp_add_application(?, ?, ?, ?, ?, ?)}";
	private final static String DELETE_SQL = "{CALL sp_delete_applications(?, ?)}";

	private volatile boolean hasInitErrors;
	private final int maxActiveConnections;
	private final MiddlewareProvider middlewareProvider;
	private final Map<String, Resource> resources;
	private final ReadWriteLock readWriteLock;

	// these cannot be final because their values will be set when the init() method is invoked
	private DataSource dataSource;

	/**
	 * @param middlewareProvider
	 *            the middleware provider.
	 * @param maxActiveConnections
	 *            the number of maximum open sql connections to maintain.
	 */
	public ClusterResourceProvider(final MiddlewareProvider middlewareProvider, final int maxActiveConnections) {
		Validate.notNull(middlewareProvider,
				"middlewareProvider cannot be null. This seems to be a coding problem and should be reported.");
		Validate.isTrue(maxActiveConnections >= 1, "maxActiveConnections must be greater or equal to one.");
		this.middlewareProvider = middlewareProvider;
		this.maxActiveConnections = maxActiveConnections;
		this.resources = new TreeMap<String, Resource>();
		this.readWriteLock = new ReentrantReadWriteLock(false);
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
	public void init() {
		final Lock writeLock = readWriteLock.writeLock();
		writeLock.lock();
		try {
			if (dataSource != null) {
				LOG.info(
						"ClusterResourcerProvider has already been initialized, ignoring invocation of init() method.");
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
				p.setMaxActive(this.maxActiveConnections);
				p.setJmxEnabled(true);
				p.setTestWhileIdle(false);
				p.setTestOnBorrow(true);
				p.setValidationQuery("SELECT 1");
				p.setTestOnReturn(false);
				p.setValidationInterval(30000);
				p.setTimeBetweenEvictionRunsMillis(30000);
				p.setInitialSize(1);
				p.setMaxWait(10000);
				p.setRemoveAbandonedTimeout(60);
				p.setMinEvictableIdleTimeMillis(30000);
				p.setLogAbandoned(true);
				p.setRemoveAbandoned(true);
				dataSource = new DataSource();
				dataSource.setPoolProperties(p);
			} catch (final Exception e) {
				hasInitErrors = true;
				throw new ApplicationException("Could not initialize ClusterResourceProvider.", e);
			}
			// run the initialization script
			try (Connection connection = dataSource.getConnection()) {
				LOG.info("Running setupdb.sql");
				// setupdb.sql must be in the classpath to be accessible!
				final ScriptRunner runner = new ScriptRunner(connection, true, true);
				runner.runScript(new BufferedReader(new InputStreamReader(
						ClusterResourceProvider.class.getResourceAsStream('/' + SETUP_SCRIPT_LOCATION))));
			} catch (final IOException | SQLException e) {
				hasInitErrors = true;
				throw new ApplicationException("Could not execute database initialization script (setupdb.sql).", e);
			}
			loadResourcesFromDatabase_notThreadSafe();
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public boolean hasInitErrors() {
		return hasInitErrors;
	}

	@Override
	public void save(final Resource resource) {
		// TODO: maybe use transactions? but then we would have to use the same connection for two statements, and given
		// that we are using object pools, who knows how long would it take to implement this or to refactor it
		final Lock writeLock = readWriteLock.writeLock();
		writeLock.lock();
		try {
			final Resource existingResource = resources.get(KeyUtils.generate(resource));
			// existingResource and resource are holding the same reference (or they should)
			if (existingResource != null) {
				if (existingResource != resource) {
					throw new ApplicationException(
							"Inconsistent resources. This is a coding problem and should be reported.");
				}
				// delete everything related to this resource on the DB, then add whatever we've given
				try (Connection connection = dataSource.getConnection();
						CallableStatement deleteStatement = connection.prepareCall(DELETE_SQL)) {
					connection.setAutoCommit(false);
					deleteStatement.setString(1, resource.getName());
					deleteStatement.setString(2, resource.getType());
					deleteStatement.execute();

					try (CallableStatement addStatement = connection.prepareCall(ADD_SQL)) {
						for (final Application app : resource.getApplications()) {
							executeAddApplicationStatement(app, resource, addStatement);
						}
					}
					connection.commit();
				} catch (final SQLException e) {
					LOG.error("Could not save applications. Check database connectivity.", e);
				}
			} else {
				// trying to save a resource that doesn't even exist
				throw new ApplicationException("Resource does not exist or is not enabled; name=" + resource.getName()
						+ ", type=" + resource.getType());
			}
		} finally {
			writeLock.unlock();
		}
	}

	private void executeAddApplicationStatement(final Application app, final Resource resource,
			final CallableStatement addApplicationStatement) throws SQLException {
		addApplicationStatement.setString(1, resource.getName());
		addApplicationStatement.setString(2, resource.getType());
		addApplicationStatement.setString(3, app.getName());
		addApplicationStatement.setString(4, app.getVersion());
		addApplicationStatement.setString(5, app.getPath());
		addApplicationStatement.setString(6, app.getDescription());
		addApplicationStatement.execute();
	}

	@Override
	public Resource getResource(final String name, final String type) {
		Validate.notBlank(name, "name cannot be empty or only whitespace.");
		Validate.notBlank(type, "type cannot be empty or only whitespace.");

		final Lock readLock = readWriteLock.readLock();
		readLock.lock();
		try {
			return resources.get(KeyUtils.generateResourceKey(name, type));
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public Collection<Resource> getResources() {
		final Lock readLock = readWriteLock.readLock();
		readLock.lock();
		try {
			return Collections.unmodifiableCollection(resources.values());
		} finally {
			readLock.unlock();
		}
	}

	// queries the DB and loads the internal map holding resources
	private void loadResourcesFromDatabase_notThreadSafe() {
		final Collection<Resource> enabledResources = getEnabledClusterResources().values();
		resources.clear();
		try (Connection connection = dataSource.getConnection();
				CallableStatement callableStatement = connection.prepareCall(GET_SQL)) {
			// iterate over all enabled resources and query the database to retrieve applications
			for (final Resource enabledResource : enabledResources) {
				fillApplicationsForResource(enabledResource, callableStatement);
				resources.put(KeyUtils.generate(enabledResource), enabledResource);
			}
		} catch (final SQLException e) {
			LOG.error("Could not retrieve applications for ClusterResourceProvider.", e);
		}
	}

	private void fillApplicationsForResource(final Resource resource, final CallableStatement callableStatement)
			throws SQLException {
		callableStatement.setString(1, resource.getName());
		callableStatement.setString(2, resource.getType());
		callableStatement.execute();

		try (ResultSet resultSet = callableStatement.getResultSet()) {
			while (resultSet.next()) {
				// get the column values to instantiate an app
				final Application.Builder applicationBuilder = new Application.Builder();
				applicationBuilder.withName(resultSet.getString("name")).withVersion(resultSet.getString("version"))
						.withPath(resultSet.getString("path")).withDescription(resultSet.getString("description"));
				resource.addApplication(applicationBuilder.newInstance());
			}
		}
	}

	private Map<String, Resource> getEnabledClusterResources() {
		final Map<String, Resource> enabledResources = new TreeMap<String, Resource>();
		final Filter<Middleware> clusterMiddlewareFilter = new ClusterMiddlewareFilter();
		final Collection<Middleware> enabledClusterMiddlewares = FilterApplicator
				.applyFilter(middlewareProvider.getEnabledMiddlewares(), clusterMiddlewareFilter);
		final Filter<Item> enabledItemFilter = new SimpleFilterFactory().setEnabled(true).newItemFilter();
		for (final Middleware enabledClusterMiddleware : enabledClusterMiddlewares) {
			for (final Item enabledClusterItem : FilterApplicator.applyFilter(enabledClusterMiddleware.getItem(),
					enabledItemFilter)) {
				final String resourceType = enabledClusterMiddleware.getType();
				final String resourceName = enabledClusterItem.getName();
				final String key = KeyUtils.generateResourceKey(resourceName, resourceType);
				if (enabledResources.containsKey(key)) {
					LOG.warn("Ignoring duplicate enabled middleware item. Middleware type = "
							+ enabledClusterMiddleware.getType() + ", item name =" + enabledClusterItem.getName());
				} else {
					final Resource.Builder resourceBuilder = new Resource.Builder();
					resourceBuilder.withName(enabledClusterItem.getName()).withType(enabledClusterMiddleware.getType());
					resourceBuilder.withQueues(extractQueuesFromItem(resourceType, enabledClusterItem));
					resourceBuilder.canModifyApplications(true);
					enabledResources.put(key, resourceBuilder.newInstance());
				}
			}
		}
		return enabledResources;
	}

	private Collection<Queue> extractQueuesFromItem(final String middlewareType, final Item item) {
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
		case local:
			// no queues for local submitter
			extractedQueueNames = Collections.<String>emptyList();
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