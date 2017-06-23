
package net.jr.dockerizedTest.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jr.dockerizedTest.BaseServer;

public class MySqlServer extends BaseServer {

	public static final String MYSQL_VERSION = "5.7.18";

	private static final Logger LOGGER = LoggerFactory.getLogger(MySqlServer.class);

	private static final String IMAGE = "mysql:" + MYSQL_VERSION;

	private static final int MYSQL_DEFAULT_PORT = 3306;

	private static final String MYSQL_ROOT_PASSWORD = "root_password";

	private static final int MAX_CONNECTION_ATTEMPTS = 20;

	private static final int DELAY_BETWEEN_ATTEMPTS_MS = 1000;

	public MySqlServer() {
		super(IMAGE, MYSQL_DEFAULT_PORT);
	}

	@Override
	public void start() {
		super.start();
		Connection cnx = null;
		LOGGER.info("Trying to obtain connection to mysql ...");
		String jdbcUrl = getJdbcUrl("mysql");
		LOGGER.info("using jdbc url : " + jdbcUrl);

		for (int i = 0; i < MAX_CONNECTION_ATTEMPTS; i++) {
			LOGGER.info("Connection attempt " + (i + 1) + "/" + MAX_CONNECTION_ATTEMPTS);
			try {
				cnx = DriverManager.getConnection(jdbcUrl, "root", MYSQL_ROOT_PASSWORD);
				LOGGER.info("\t-> ok, Got connection  : " + cnx);
				break;
			} catch (Exception e) {
				LOGGER.info("\t-> failure");
				if (getDockerSupport().isExited()) {
					throw new IllegalStateException("docker container has exited !");
				} else {
					try {
						Thread.sleep(DELAY_BETWEEN_ATTEMPTS_MS);
					} catch (InterruptedException interruptedException) {
						throw new RuntimeException(interruptedException);
					}
				}
			}
		}
		if (cnx == null) {
			throw new IllegalStateException("Could not get a connection !");
		} else {
			try {
				cnx.close();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public Connection rootConnection() throws SQLException {
		return DriverManager.getConnection(getJdbcUrl("mysql"), "root", getRootPassword());
	}

	public String getJdbcUrl(String dbName) {
		final String dbPath = dbName == null || dbName.trim().isEmpty() ? "" : "/" + dbName;
		try {
			return "jdbc:mysql://127.0.0.1:" + getPort() + dbPath + "?useSSL=false";
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String getRootPassword() {
		return MYSQL_ROOT_PASSWORD;
	}

	public void createDb(String dbName) throws SQLException {
		Connection cnx = DriverManager.getConnection(getJdbcUrl("mysql"), "root", MYSQL_ROOT_PASSWORD);
		cnx.createStatement().execute("create database " + dbName + " charset 'utf8'");
		cnx.close();
	}

}
