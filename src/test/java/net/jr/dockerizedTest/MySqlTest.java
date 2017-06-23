
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
    cnx.close();
    Assert.assertEquals(MySqlServer.MYSQL_VERSION, dbVersion);
    
    mysqlServer.createDb("TEST_DB");
    
    
    cnx = DriverManager.getConnection(mysqlServer.getJdbcUrl("TEST_DB"), "root", mysqlServer.getRootPassword());
    
    cnx.createStatement().execute("create table test_table(id int not null auto_increment, val varchar(10), primary key (id));");
    
  }
}
