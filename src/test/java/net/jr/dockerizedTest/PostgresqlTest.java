package net.jr.dockerizedTest;

import java.sql.Connection;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import net.jr.dockerizedTest.postgresql.PostgreSqlServer;

public class PostgresqlTest {

	private static PostgreSqlServer postgreSqlServer;

	@BeforeClass
	public static void setupClass() throws Exception {
		postgreSqlServer = new PostgreSqlServer();
		postgreSqlServer.start();
	}

	@AfterClass
	public static void afterClass() {
		postgreSqlServer.stop();
	}

	@Test
	public void connectionTest() throws Exception {
		Connection cnx = postgreSqlServer.postgresUserConnection();
		System.out.println(cnx.getMetaData().getDatabaseProductVersion());
	}
}
