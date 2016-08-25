
package net.jr.dockerizedTest;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import net.jr.dockerizedTest.redis.RedisServer;
import redis.clients.jedis.Jedis;

public class RedisTest {

  private static RedisServer redisServer;

  @BeforeClass
  public static void setupClass() throws Exception {
    redisServer = new RedisServer();
    redisServer.start();
  }

  @AfterClass
  public static void afterClass() {
    redisServer.stop();
  }

  @Test
  public void connectionTest() throws Exception {
    Jedis jedis = new Jedis("localhost", redisServer.getPort());
    System.out.println("Server is running: "+jedis.ping());
    jedis.close();
  }

}
