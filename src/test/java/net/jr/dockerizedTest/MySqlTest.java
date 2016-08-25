
package net.jr.dockerizedTest;

import java.sql.Connection;
import java.sql.DriverManager;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import net.jr.dockerizedTest.mysql.MySqlServer;

public class MySqlTest {

  private static MySqlServer mysqlServer;

  @BeforeClass
  public static void setupClass() throws Exception {
    mysqlServer = new MySqlServer();
    mysqlServer.start();
  }
  
  @AfterClass
  public static void afterClass() {
    mysqlServer.stop();
  }

  @Test
  public void connectionTest() throws Exception {
    String jdbcUrl = mysqlServer.getJdbcUrl("mysql");
    Connection cnx = DriverManager.getConnection(jdbcUrl, "root", mysqlServer.getRootPassword());
    Assert.assertNotNull(cnx);
    String dbVersion = cnx.getMetaData().getDatabaseProductVersion();
    Assert.assertEquals("5.5.51", dbVersion);
    mysqlServer.createDb("TEST_DB");
  }
}
