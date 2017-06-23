
package net.jr.dockerizedTest.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerConfig.Builder;

import net.jr.dockerizedTest.BaseServer;
import net.jr.dockerizedTest.DockerSupport;

public class MySqlServer extends BaseServer {

	public static final String MYSQL_VERSION = "5.7.18";

	private static final String IMAGE = "mysql:" + MYSQL_VERSION;

	private static final int MYSQL_DEFAULT_PORT = 3306;

	private static final String MYSQL_ROOT_PASSWORD = "root_password";

	public MySqlServer() {
		super(new DockerSupport(IMAGE) {
			
			@Override
			public ContainerConfig configure(Builder configBuilder) {
				return exposePort(configBuilder, MYSQL_DEFAULT_PORT)
						.env("MYSQL_ROOT_PASSWORD="+MYSQL_ROOT_PASSWORD)
						.build();
			}
		});
	}

	@Override
	public void start() {
		super.start();
		String jdbcUrl = getJdbcUrl("mysql");
		pollUntilSuccess(20, 1000, () -> DriverManager.getConnection(jdbcUrl, "root", MYSQL_ROOT_PASSWORD));
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
