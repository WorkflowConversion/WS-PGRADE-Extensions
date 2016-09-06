package com.workflowconversion.importer.guse.appdb.impl;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.workflowconversion.importer.guse.Settings;
import com.workflowconversion.importer.guse.appdb.Application;
import com.workflowconversion.importer.guse.appdb.ApplicationProvider;
import com.workflowconversion.importer.guse.exception.ApplicationException;
import com.workflowconversion.importer.guse.exception.DuplicateApplicationException;
import com.workflowconversion.importer.guse.exception.InvalidApplicationException;
import com.workflowconversion.importer.guse.exception.NotEditableApplicationProviderException;

/**
 * Application provider using a SQL database as backup.
 * 
 * @author delagarza
 *
 */
public class MySQLApplicationProvider implements ApplicationProvider {

	private final static String SQL_SP_VIEW = "{CALL wfip_sp_get_all_applications()}";
	private final static String SQL_SP_UPDATE = "{CALL wfip_sp_update_application(?, ?, ?, ?, ?, ?, ?)}";
	private final static String SQL_SP_ADD = "{CALL wfip_sp_add_application(?, ?, ?, ?, ?, ?)}";

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
		app.setId(resultSet.getInt(SQL_COLUMN_ID));
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
		final MySQLStoredProcedureCall call = new MySQLStoredProcedureCall(this.dataSource, SQL_SP_ADD, false) {

			@Override
			void prepareStatement(final CallableStatement statement) throws SQLException {
				setCommonAddUpdateStoredProcedureParameters(statement, app);
			}
		};

		call.performCall();
	}

	@Override
	public void saveApplication(final Application app) throws NotEditableApplicationProviderException {
		final MySQLStoredProcedureCall call = new MySQLStoredProcedureCall(this.dataSource, SQL_SP_UPDATE, false) {

			@Override
			void prepareStatement(final CallableStatement statement) throws SQLException {
				statement.setInt(SQL_SP_PARAM_ID, app.getId());
				setCommonAddUpdateStoredProcedureParameters(statement, app);
			}
		};

		call.performCall();
	}

	private void setCommonAddUpdateStoredProcedureParameters(final CallableStatement statement, final Application app)
			throws SQLException {
		statement.setString(SQL_SP_PARAM_NAME, app.getName());
		statement.setString(SQL_SP_PARAM_VERSION, app.getVersion());
		statement.setString(SQL_SP_PARAM_RESOURCE, app.getResource());
		statement.setString(SQL_SP_PARAM_RESOURCE_TYPE, app.getResourceType());
		statement.setString(SQL_SP_PARAM_DESCRIPTION, app.getDescription());
		statement.setString(SQL_SP_PARAM_PATH, app.getPath());
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
		// the app that is to be updated/added
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
			} catch (SQLException e) {
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
