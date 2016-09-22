package com.workflowconversion.importer.app.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workflowconversion.importer.Settings;
import com.workflowconversion.importer.app.Application;
import com.workflowconversion.importer.app.ApplicationProvider;
import com.workflowconversion.importer.exception.ApplicationException;
import com.workflowconversion.importer.exception.DuplicateApplicationException;
import com.workflowconversion.importer.exception.InvalidApplicationException;
import com.workflowconversion.importer.exception.NotEditableApplicationProviderException;

/**
 * Application provider using a SQL database as backup.
 * 
 * @author delagarza
 *
 */
public class MySQLApplicationProvider implements ApplicationProvider {

	private final static String SQL_SP_VIEW = "{CALL wfip_sp_get_all_applications()}";
	private final static String SQL_SP_UPDATE = "{CALL wfip_sp_update_application(?, ?, ?, ?, ?, ?, ?)}";
	private final static String SQL_SP_ADD = "{CALL wfip_sp_add_application(?, ?, ?, ?, ?, ?, ?)}";
	private final static String SQL_SP_DELETE = "{CALL wfip_sp_delete_application(?)}";

	private final static int SQL_ERROR_DUPLICATE_ENTRY = 1062;
	private final static int SQL_ERROR_COLUMN_CANNOT_BE_NULL = 1048;

	private final static String SQL_COLUMN_ID = "id";
	private final static String SQL_COLUMN_NAME = "name";
	private final static String SQL_COLUMN_VERSION = "version";
	private final static String SQL_COLUMN_RESOURCE = "resource";
	private final static String SQL_COLUMN_RESOURCE_TYPE = "resource_type";
	private final static String SQL_COLUMN_DESCRIPTION = "description";
	private final static String SQL_COLUMN_PATH = "path";

	private final static String SQL_SP_PARAM_ID = "param_id";
	private final static String SQL_SP_PARAM_NAME = "param_name";
	private final static String SQL_SP_PARAM_VERSION = "param_version";
	private final static String SQL_SP_PARAM_RESOURCE = "param_resource";
	private final static String SQL_SP_PARAM_RESOURCE_TYPE = "param_resource_type";
	private final static String SQL_SP_PARAM_DESCRIPTION = "param_description";
	private final static String SQL_SP_PARAM_PATH = "param_path";

	private final static String ID_PREFIX = "internal_app_db_id_";

	private final static Logger LOG = LoggerFactory.getLogger(MySQLApplicationProvider.class);

	// we could use an entry in context.xml to configure the data source, but we get the db attributes
	// from gUSE, which means to invoke a webservice
	private DataSource dataSource;
	private volatile boolean initialized;

	public MySQLApplicationProvider() {
		this.initialized = false;
	}

	@Override
	public boolean isEditable() {
		return true;
	}

	@Override
	public String getName() {
		return "Local gUSE application database";
	}

	@Override
	public Collection<Application> getApplications() {
		final Collection<Application> apps = new LinkedList<Application>();
		final MySQLStoredProcedureCall call = new MySQLStoredProcedureCall(this.dataSource, SQL_SP_VIEW, true) {

			@Override
			void processRow(final ResultSet resultSet) throws SQLException {
				apps.add(createApplicationFromResultSet(resultSet));
			}

		};

		call.performCall();
		return apps;
	}

	private Application createApplicationFromResultSet(final ResultSet resultSet) throws SQLException {
		final Application app = new Application();
		app.setId(toApplicationId(resultSet.getInt(SQL_COLUMN_ID)));
		app.setName(resultSet.getString(SQL_COLUMN_NAME));
		app.setVersion(resultSet.getString(SQL_COLUMN_VERSION));
		app.setResource(resultSet.getString(SQL_COLUMN_RESOURCE));
		app.setResourceType(resultSet.getString(SQL_COLUMN_RESOURCE_TYPE));
		app.setDescription(resultSet.getString(SQL_COLUMN_DESCRIPTION));
		app.setPath(resultSet.getString(SQL_COLUMN_PATH));
		return app;
	}

	@Override
	public void addApplication(final Application app) throws NotEditableApplicationProviderException {
		// using atomic integer in order to use a final object on which the id can be stored
		final AtomicInteger id = new AtomicInteger();
		final MySQLStoredProcedureCall call = new MySQLStoredProcedureCall(this.dataSource, SQL_SP_ADD, false, app) {

			@Override
			void prepareStatement(final CallableStatement statement) throws SQLException {
				statement.registerOutParameter(SQL_SP_PARAM_ID, Types.INTEGER);
				setCommonAddUpdateStoredProcedureParameters(statement, app);
			}

			@Override
			void afterExecution(final CallableStatement statement) throws SQLException {
				id.set(statement.getInt(SQL_SP_PARAM_ID));
			}
		};

		call.performCall();

		final String applicationId = toApplicationId(id.get());
		app.setId(applicationId);
	}

	@Override
	public void saveApplication(final Application app) throws NotEditableApplicationProviderException {
		final MySQLStoredProcedureCall call = new MySQLStoredProcedureCall(this.dataSource, SQL_SP_UPDATE, false, app) {

			@Override
			void prepareStatement(final CallableStatement statement) throws SQLException {
				statement.setInt(SQL_SP_PARAM_ID, toDatabaseId(app));
				setCommonAddUpdateStoredProcedureParameters(statement, app);
			}
		};

		call.performCall();
	}

	@Override
	public void removeApplication(final Application app) throws NotEditableApplicationProviderException {
		final MySQLStoredProcedureCall call = new MySQLStoredProcedureCall(this.dataSource, SQL_SP_DELETE, false, app) {

			@Override
			void prepareStatement(final CallableStatement statement) throws SQLException {
				statement.setInt(SQL_SP_PARAM_ID, toDatabaseId(app));
			}
		};

		call.performCall();
	}

	private int toDatabaseId(final Application application) {
		return Integer.valueOf(application.getId().substring(ID_PREFIX.length()));
	}

	private String toApplicationId(final int id) {
		return ID_PREFIX + id;
	}

	private void setCommonAddUpdateStoredProcedureParameters(final CallableStatement statement, final Application app)
			throws SQLException {
		setNullIfNeeded(statement, SQL_SP_PARAM_NAME, app.getName());
		setNullIfNeeded(statement, SQL_SP_PARAM_VERSION, app.getVersion());
		setNullIfNeeded(statement, SQL_SP_PARAM_RESOURCE, app.getResource());
		setNullIfNeeded(statement, SQL_SP_PARAM_RESOURCE_TYPE, app.getResourceType());
		setNullIfNeeded(statement, SQL_SP_PARAM_DESCRIPTION, app.getDescription());
		setNullIfNeeded(statement, SQL_SP_PARAM_PATH, app.getPath());
	}

	private void setNullIfNeeded(final CallableStatement statement, final String parameterName, final String value)
			throws SQLException {
		final String finalValue = StringUtils.trimToNull(value);
		if (finalValue == null) {
			statement.setNull(parameterName, Types.VARCHAR);
		} else {
			statement.setString(parameterName, finalValue);
		}
	}

	@Override
	public boolean needsInit() {
		return !initialized;
	}

	@Override
	public void init() {
		if (!initialized) {
			synchronized (this) {
				if (!initialized) {
					initDataSource();
					initialized = true;
				}
			}
		}
	}

	private void initDataSource() {
		LOG.info("Initializing MySQLApplicationProvider");
		// see comment on declaration of dataSource member of this class for an explanation
		// of this chunk of code
		// get a pool properties with pre-loaded settings
		final PoolProperties poolProperties = Settings.getInstance().getPoolProperties();
		// set the values we get from gUSE
		poolProperties.setUrl(Settings.getInstance().getDatabaseConfiguration().getURL());
		poolProperties.setDriverClassName(Settings.getInstance().getDatabaseConfiguration().getDriver());
		poolProperties.setUsername(Settings.getInstance().getDatabaseConfiguration().getUsername());
		poolProperties.setPassword(Settings.getInstance().getDatabaseConfiguration().getPassword());
		// and make sure we override the following settings
		poolProperties.setJmxEnabled(true);
		poolProperties.setTestWhileIdle(false);
		poolProperties.setTestOnBorrow(true);
		poolProperties.setValidationQuery("SELECT 1");
		poolProperties.setTestOnReturn(false);
		poolProperties.setRemoveAbandonedTimeout(60);
		poolProperties.setMinEvictableIdleTimeMillis(30000);
		poolProperties.setLogAbandoned(true);
		poolProperties.setRemoveAbandoned(true);
		poolProperties.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
				+ "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
		this.dataSource = new DataSource();
		dataSource.setPoolProperties(poolProperties);
	}

	private abstract class MySQLStoredProcedureCall {

		private final DataSource dataSource;
		// e.g., {call xyz(?, ?)}
		private final String jdbcCall;
		// whether rows should be processed and processRow be invoked for each row
		private final boolean needsRowProcessing;
		// the app that is to be updated/added/removed
		private final Application app;

		MySQLStoredProcedureCall(final DataSource dataSource, final String jdbcCall, final boolean needsRowProcessing) {
			this(dataSource, jdbcCall, needsRowProcessing, null);
		}

		MySQLStoredProcedureCall(final DataSource dataSource, final String jdbcCall, final boolean needsRowProcessing,
				final Application app) {
			this.dataSource = dataSource;
			this.jdbcCall = jdbcCall;
			this.needsRowProcessing = needsRowProcessing;
			this.app = app;
		}

		// here values for the stored procedure can be set
		void prepareStatement(final CallableStatement statement) throws SQLException {

		}

		// for each row of the resultset this method will be invoked
		void processRow(final ResultSet resultSet) throws SQLException {

		}

		// invoked at the end, useful to obtain output parameters
		void afterExecution(final CallableStatement statement) throws SQLException {

		}

		final void performCall() {
			try (final Connection connection = this.dataSource.getConnection();
					final CallableStatement statement = connection.prepareCall(this.jdbcCall)) {
				prepareStatement(statement);
				statement.execute();
				if (this.needsRowProcessing) {
					try (final ResultSet resultSet = statement.getResultSet()) {
						while (resultSet.next()) {
							processRow(resultSet);
						}
					}
				}
				afterExecution(statement);
			} catch (SQLException e) {
				LOG.error("An error occurred while accessing the database", e);
				switch (e.getErrorCode()) {
				case SQL_ERROR_COLUMN_CANNOT_BE_NULL:
					throw new InvalidApplicationException("Cannot update/insert an application with NULL values",
							this.app, e);
				case SQL_ERROR_DUPLICATE_ENTRY:
					throw new DuplicateApplicationException("The updated/inserted application already exists", this.app,
							e);
				default:
					throw new ApplicationException("A database error occurred while accessing the database"
							+ (this.app == null ? "" : " application: " + this.app), e);

				}
			}
		}
	}
}
