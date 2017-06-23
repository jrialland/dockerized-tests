

[![Build Status](https://travis-ci.org/jrialland/dockerized-tests.svg?branch=master)](https://travis-ci.org/jrialland/dockerized-tests)

Run real server applications (supported : mysql, redis) during unit tests by using docker :

```java

public class HowtoUse {

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
  public void testUsingJdbcConnection() {

      Connection cnx = mysqlServer.rootConnection();
      String dbVersion = cnx.getMetaData().getDatabaseProductVersion();
      System.out.println("Mysql version : " + dbVersion);
  }

```
