
package net.jr.dockerizedTest.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerConfig.Builder;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;

import net.jr.dockerizedTest.DockerSupport;

public class MySqlServer {

	public static final String MYSQL_VERSION = "5.7.18";

	private static final Logger LOGGER = LoggerFactory.getLogger(MySqlServer.class);

	private static final String IMAGE = "mysql:" + MYSQL_VERSION;

	private static final int MYSQL_DEFAULT_PORT = 3306;

	private static final String MYSQL_ROOT_PASSWORD = "root_password";

	private static final int MAX_CONNECTION_ATTEMPTS = 20;

	private static final int DELAY_BETWEEN_ATTEMPTS_MS = 1000;

	private DockerSupport dockerSupport;

	private boolean stopped = true;

	public MySqlServer() {
		dockerSupport = new DockerSupport(IMAGE) {
			@Override
			public ContainerConfig configure(Builder configBuilder) {

				Map<String, List<PortBinding>> portBindings = new TreeMap<>();
				portBindings.put(Integer.toString(MYSQL_DEFAULT_PORT),
						Arrays.asList(PortBinding.randomPort("0.0.0.0")));
				final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

				return configBuilder.env("MYSQL_ROOT_PASSWORD=" + MYSQL_ROOT_PASSWORD).hostConfig(hostConfig)
						.exposedPorts(Integer.toString(MYSQL_DEFAULT_PORT)).build();
			}
		};
		dockerSupport.initContainer();
	}

	public void start() throws Exception {
		if (stopped) {
			dockerSupport.startContainer();
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
					if (dockerSupport.isExited()) {
						throw new IllegalStateException("docker container has exited !");
					} else {
						Thread.sleep(DELAY_BETWEEN_ATTEMPTS_MS);
					}
				}
			}
			if (cnx == null) {
				throw new IllegalStateException("Could not get a connection !");
			} else {
				stopped = false;
				cnx.close();
			}
		}
	}

	public void stop() {
		try {
			dockerSupport.stopAndRemoveContainer();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			stopped = true;
		}
	}

	public Connection rootConnection() throws SQLException {
		return DriverManager.getConnection(getJdbcUrl("mysql"), "root", getRootPassword());
	}

	@Override
	protected void finalize() throws Throwable {
		if (!stopped) {
			stop();
		}
		super.finalize();
	}

	public String getJdbcUrl(String dbName) {
		final String dbPath = dbName == null || dbName.trim().isEmpty() ? "" : "/" + dbName;
		try {
			return "jdbc:mysql://127.0.0.1:" + dockerSupport.getFirstHostPort() + dbPath + "?useSSL=false";
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
