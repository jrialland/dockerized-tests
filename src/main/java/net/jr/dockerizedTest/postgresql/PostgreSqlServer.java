package net.jr.dockerizedTest.postgresql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerConfig.Builder;

import net.jr.dockerizedTest.BaseServer;
import net.jr.dockerizedTest.DockerSupport;

public class PostgreSqlServer extends BaseServer {

	private static final String IMAGE = "sameersbn/postgresql:9.6-2";

	private static final int DEFAULT_PORT = 5432;

	public static final String PG_USER = "postgres";

	public static final String PG_PASSWORD = "pg_PAzzW0RD";

	public PostgreSqlServer() {
		super(new DockerSupport(IMAGE) {

			@Override
			public ContainerConfig configure(Builder configBuilder) {
				return exposePort(configBuilder, DEFAULT_PORT).env("DB_USER=" + PG_USER)
						.env("PG_PASSWORD=" + PG_PASSWORD).env("PG_TRUST_LOCALNET=true").build();
			}
		});
	}

	@Override
	public void start() {
		super.start();
		pollUntilSuccess(20, 1000, () -> postgresUserConnection());
	}

	public Connection postgresUserConnection() throws SQLException {
		return DriverManager.getConnection(getJdbcUrl("template1"), PG_USER, PG_PASSWORD);
	}

	public String getJdbcUrl(String dbName) {
		return "jdbc:postgresql://localhost:" + getPort() + "/" + dbName;
	}
}
