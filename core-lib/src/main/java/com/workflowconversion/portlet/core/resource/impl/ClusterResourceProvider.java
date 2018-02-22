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
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.SoftReferenceObjectPool;
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
 * 
 * 
 * @author delagarza
 *
 */
public class ClusterResourceProvider implements ResourceProvider {

	private static final long serialVersionUID = -3799340787944829350L;
	private final static Logger LOG = LoggerFactory.getLogger(ClusterResourceProvider.class);

	private final static String SETUP_SCRIPT_LOCATION = "setupdb.sql";
	private final static String GET_ALL_SQL = "{CALL sp_get_applications(?, ?)}";
	private final static String ADD_SQL = "{CALL sp_add_application(?, ?, ?, ?, ?, ?)}";
	private final static String EDIT_SQL = "{CALL sp_edit_application(?, ?, ?, ?, ?, ?)}";
	private final static String DELETE_SQL = "{CALL sp_delete_application(?)}";

	private volatile boolean hasInitErrors;
	private final int maxActiveConnections;
	private final MiddlewareProvider middlewareProvider;

	// these cannot be final because their values will be set when the init() method is invoked
	private DataSource dataSource;
	// creation of CallableStatement objects is expensive, so we will have a dedicated pool for each kind of statement
	private ObjectPool<CallableStatement> getAllStatementPool;
	private ObjectPool<CallableStatement> addStatementPool;
	private ObjectPool<CallableStatement> editStatementPool;
	private ObjectPool<CallableStatement> deleteStatementPool;

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
		// init the CallableStatement object pools
		getAllStatementPool = new SoftReferenceObjectPool<CallableStatement>(
				new CallableStatementFactory(dataSource, GET_ALL_SQL));
		addStatementPool = new SoftReferenceObjectPool<CallableStatement>(
				new CallableStatementFactory(dataSource, ADD_SQL));
		editStatementPool = new SoftReferenceObjectPool<CallableStatement>(
				new CallableStatementFactory(dataSource, EDIT_SQL));
		deleteStatementPool = new SoftReferenceObjectPool<CallableStatement>(
				new CallableStatementFactory(dataSource, DELETE_SQL));
	}

	@Override
	public boolean hasInitErrors() {
		return hasInitErrors;
	}

	@Override
	public void save(final Resource resource) {
		// TODO: maybe use transactions? but then we would have to use the same connection for two statements, and given
		// that we are using object pools, who knows how long would it take to implement this or to refactor it

		// delete everything related to this resource, then add whatever we've given
		final CallableStatement deleteStatement = borrowCallableStatement(deleteStatementPool);
		try {
			deleteStatement.setString(1, resource.getName());
			deleteStatement.setString(2, resource.getType());
			deleteStatement.execute();
		} catch (final SQLException e) {
			throw new ApplicationException(
					"Could not delete applications from the database. Check database connectivity.", e);
		} finally {
			returnCallableStatement(deleteStatementPool, deleteStatement);
		}

		saveResource_internal(resource);
	}

	// saves all apps in this resource into the DB
	private void saveResource_internal(final Resource resource) {
		final CallableStatement addStatement = borrowCallableStatement(addStatementPool);
		try {
			for (final Application app : resource.getApplications()) {
				executeApplicationCallableStatement(app, resource, addStatement);
			}
		} finally {
			returnCallableStatement(addStatementPool, addStatement);
		}
	}

	// since both add and edit stored procedures require the same ammount of parameters, it's pretty useful to use a
	// method like this
	private void executeApplicationCallableStatement(final Application app, final Resource resource,
			final CallableStatement callableStatement) {
		try {
			callableStatement.setString(1, resource.getName());
			callableStatement.setString(2, resource.getType());
			callableStatement.setString(3, app.getName());
			callableStatement.setString(4, app.getVersion());
			callableStatement.setString(5, app.getPath());
			callableStatement.setString(6, app.getDescription());
			callableStatement.execute();
		} catch (final SQLException e) {
			throw new ApplicationException("Could not add application in the database. Check database connectivity.",
					e);
		}
	}

	@Override
	public void merge(final Collection<Resource> resources) {
		// basically, we need to create two collections: one containing apps to add and one containing apps to edit
		// this is to know which stored procedure we will be using
		final Collection<ApplicationWithResource> appsToAdd = new LinkedList<ApplicationWithResource>();
		final Collection<ApplicationWithResource> appsToEdit = new LinkedList<ApplicationWithResource>();

		final Map<String, Resource> existingResources = new TreeMap<String, Resource>();
		for (final Resource existingResource : getResources_internal(null, null)) {
			existingResources.put(KeyUtils.generate(existingResource), existingResource);
		}

		for (final Resource resource : resources) {
			final String resourceKey = KeyUtils.generate(resource);
			final Resource existingResource = existingResources.get(resourceKey);
			if (existingResource == null) {
				for (final Application app : resource.getApplications()) {
					appsToAdd.add(new ApplicationWithResource(app, resource));
				}
			} else {
				// like in Inception, we need to go deeper! This means that instead of just adding all apps,
				// we need to check which ones already exist to update them and which ones we need to add...
				// luckily the difference is just which CallableStatement to use
				final Map<String, Application> existingApplications = new TreeMap<String, Application>();
				for (final Application existingApplication : existingResource.getApplications()) {
					existingApplications.put(KeyUtils.generate(existingApplication), existingApplication);
				}
				for (final Application application : resource.getApplications()) {
					final String appKey = KeyUtils.generate(application);
					final Application existingApplication = existingApplications.get(appKey);
					if (existingApplication == null) {
						appsToAdd.add(new ApplicationWithResource(application, resource));
					} else {
						appsToEdit.add(new ApplicationWithResource(application, resource));
					}
				}
			}
		}

		final CallableStatement addStatement = borrowCallableStatement(addStatementPool);
		try {
			executeBatchApplicationCallableStatement(appsToAdd, addStatement);
		} finally {
			returnCallableStatement(addStatementPool, addStatement);
		}

		final CallableStatement editStatement = borrowCallableStatement(editStatementPool);
		try {
			executeBatchApplicationCallableStatement(appsToEdit, editStatement);
		} finally {
			returnCallableStatement(editStatementPool, editStatement);
		}
	}

	private void executeBatchApplicationCallableStatement(final Collection<ApplicationWithResource> applications,
			final CallableStatement callableStatement) {
		// keep track of apps we've already processed
		final Set<String> processedApps = new TreeSet<String>();
		try {
			// enable transactions
			callableStatement.getConnection().setAutoCommit(false);
			for (final ApplicationWithResource appWithResource : applications) {
				if (!processedApps.add(appWithResource.getKey())) {
					LOG.warn("This application has been added to the database already and will be ignored. "
							+ appWithResource);
				}
				executeApplicationCallableStatement(appWithResource.application, appWithResource.resource,
						callableStatement);
			}
			callableStatement.getConnection().commit();
		} catch (final SQLException e) {
			throw new ApplicationException("Could not save application in database. Check database connectivity.", e);
		}
	}

	@Override
	public Resource getResource(final String name, final String type) {
		Validate.notBlank(name, "name cannot be empty or only whitespace.");
		Validate.notBlank(type, "type cannot be empty or only whitespace.");
		final Collection<Resource> resources = getResources_internal(name, type);
		Validate.isTrue(resources.size() == 1,
				"More than one resource was returned when only one was expected. This is a bug and should be reported.");
		return resources.iterator().next();
	}

	@Override
	public Collection<Resource> getResources() {
		return Collections.unmodifiableCollection(getResources_internal(null, null));
	}

	private Collection<Resource> getResources_internal(final String desiredResourceName,
			final String desiredResourceType) {
		final Collection<Resource> resources = new LinkedList<Resource>();
		final Map<String, Item> enabledClusterItems = getEnabledClusterItems();
		final CallableStatement callableStatement = borrowCallableStatement(getAllStatementPool);
		try {
			// set both input parameters to null
			callableStatement.setString(1, desiredResourceName);
			callableStatement.setString(2, desiredResourceType);
			callableStatement.execute();

			String previousResourceName = null;
			String previousResourceType = null;
			Item previousItem = null;
			// store the key to avoid computing it every time we need to compare it to the current key
			String previousResourceKey = null;
			// these will hold only applications associated to one resource
			final Collection<Application> applications = new LinkedList<Application>();

			try (ResultSet resultSet = callableStatement.getResultSet()) {
				while (resultSet.next()) {
					// verify that the resource belonging to this application is enabled
					final String resourceName = StringUtils.trimToEmpty(resultSet.getString("resource_name"));
					final String resourceType = StringUtils.trimToEmpty(resultSet.getString("resource_type"));
					final String resourceKey = KeyUtils.generateResourceKey(resourceName, resourceType);
					final Item item = enabledClusterItems.get(resourceKey);
					if (item == null) {
						// TODO: generate this warning only once per resource and not once per application associated to
						// it!
						LOG.warn("Resource [name=" + resourceName + ", type=" + resourceType
								+ "] is not enabled. Ignoring applications associated to it.");
						// make sure we add the previous active resource with its applications
						if (previousItem != null) {
							addResource(resources, previousResourceName, previousResourceType, applications,
									previousItem);
						}
					} else {
						// get the column values to instantiate an app
						final Application application = new Application.Builder().withName(resultSet.getString("name"))
								.withVersion(resultSet.getString("version")).withPath(resultSet.getString("path"))
								.withDescription(resultSet.getString("description")).newInstance();
						// are we seeing the first active resource OR are we seeing the same resource as before?
						if (previousResourceKey == null || previousResourceKey.equals(resourceKey)) {
							applications.add(application);
						} else {
							// this is a new resource we're seeing, so we need to actually instantiate a new resource
							// with all the applications we've gotten
							addResource(resources, previousResourceName, previousResourceType, applications,
									previousItem);
						}
					}
					previousResourceKey = resourceKey;
					previousResourceName = resourceName;
					previousResourceType = resourceType;
					previousItem = item;
				}
			}
			// we still have to add the last resource (unless not a single resource was active!)
			if (previousItem != null) {
				addResource(resources, previousResourceName, previousResourceType, applications, previousItem);
			}
		} catch (final SQLException e) {
			throw new ApplicationException("Could not retrieve applications for ClusterResourceProvider.", e);
		} finally {
			// return the object no matter what
			returnCallableStatement(getAllStatementPool, callableStatement);
		}
		return resources;
	}

	private void addResource(final Collection<Resource> resources, final String resourceName, final String resourceType,
			final Collection<Application> applications, final Item associatedItem) {
		final Resource.Builder resourceBuilder = new Resource.Builder();
		resourceBuilder.canModifyApplications(true).withName(resourceName).withType(resourceType)
				.withApplications(applications);
		resourceBuilder.withQueues(extractQueuesFromItem(resourceType, associatedItem));
		resources.add(resourceBuilder.newInstance());
		// clear them so the new resource sees only its own apps
		applications.clear();
	}

	private CallableStatement borrowCallableStatement(final ObjectPool<CallableStatement> pool) {
		try {
			return pool.borrowObject();
		} catch (final Exception e) {
			// there's not much we can do here other than add some message
			throw new ApplicationException("Could not retrieve a CallableStatement. Check database connectivity.", e);
		}
	}

	private void returnCallableStatement(final ObjectPool<CallableStatement> pool,
			final CallableStatement callableStatement) {
		try {
			pool.returnObject(callableStatement);
		} catch (final Exception e) {
			throw new ApplicationException(
					"Could not return CallableStatement back to its pool. This might be a bug and should be reported.",
					e);
		}
	}

	private Map<String, Item> getEnabledClusterItems() {
		// get the enabled cluster middlewares
		final Filter<Middleware> clusterMiddlewareFilter = new ClusterMiddlewareFilter();
		final Collection<Middleware> enabledClusterMiddlewares = FilterApplicator
				.applyFilter(middlewareProvider.getEnabledMiddlewares(), clusterMiddlewareFilter);
		// select the enabled items from the list of enabled middlewares
		final Map<String, Item> enabledClusterItemMap = new TreeMap<String, Item>();
		final Filter<Item> enabledItemFilter = new SimpleFilterFactory().setEnabled(true).newItemFilter();
		for (final Middleware enabledClusterMiddleware : enabledClusterMiddlewares) {
			for (final Item enabledClusterItem : FilterApplicator.applyFilter(enabledClusterMiddleware.getItem(),
					enabledItemFilter)) {
				final String key = KeyUtils.generateResourceKey(enabledClusterItem.getName(),
						enabledClusterMiddleware.getType());
				if (enabledClusterItemMap.put(key, enabledClusterItem) != null) {
					LOG.warn("There is a duplicate enabled middleware item. Middleware type = "
							+ enabledClusterMiddleware.getType() + ", item name =" + enabledClusterItem.getName());
				}
			}
		}
		return enabledClusterItemMap;
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

	private final static class CallableStatementFactory extends BasePooledObjectFactory<CallableStatement> {

		private final String sql;
		private final DataSource dataSource;

		public CallableStatementFactory(final DataSource dataSource, final String sql) {
			Validate.notNull(dataSource, "Null dataSource provided! This seems to be a bug and should be reported.");
			Validate.notBlank(sql,
					"Invalid SQL! Passed value: '" + sql + "'. This seems to be a bug and should be reported.");

			this.dataSource = dataSource;
			this.sql = sql;
		}

		@Override
		public CallableStatement create() throws Exception {
			// we cannot use "try with resources" because we need to keep the JDBC connection open as long as the
			// CallableStatement is "active"
			final Connection connection;
			try {
				connection = dataSource.getConnection();
			} catch (final SQLException e) {
				throw new ApplicationException(
						"Could not retrieve SQL connection from the JDBC data source. Check database connectivity.", e);
			}

			return connection.prepareCall(sql);
		}

		@Override
		public void destroyObject(final PooledObject<CallableStatement> obj) throws Exception {
			passivateObject(obj);
		}

		@Override
		public void passivateObject(final PooledObject<CallableStatement> obj) throws Exception {
			final Connection connection = obj.getObject().getConnection();
			if (connection != null && !connection.isClosed()) {
				try {
					connection.close();
				} catch (final SQLException e) {
					throw new ApplicationException("Could not close SQL connection. Check database connectivity", e);
				}
			}
		}

		@Override
		public PooledObject<CallableStatement> wrap(final CallableStatement obj) {
			return new DefaultPooledObject<CallableStatement>(obj);
		}
	}

	private static class ApplicationWithResource {
		final Application application;
		final Resource resource;

		public ApplicationWithResource(final Application application, final Resource resource) {
			this.application = application;
			this.resource = resource;
		}

		public String getKey() {
			return KeyUtils.generate(resource) + '_' + KeyUtils.generate(application);
		}

		@Override
		public String toString() {
			return resource.toString() + "; " + application.toString();
		}
	}
}
