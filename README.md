

[![Build Status](https://travis-ci.org/jrialland/dockerized-tests.svg?branch=master)](https://travis-ci.org/jrialland/dockerized-tests)
[![jitpack.io](https://jitpack.io/v/jrialland/dockerized-tests.svg)](https://jitpack.io/#jrialland/dockerized-tests)

Run real server applications during unit tests by using docker -

You can instanciate the following servers :

* Mysql 5.7.18
* Postgresql 9.1
* Redis 3.0.7
* MemCached 1.4.37

MySql example : 

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
