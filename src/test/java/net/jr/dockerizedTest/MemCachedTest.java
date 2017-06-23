package net.jr.dockerizedTest;

import java.net.InetSocketAddress;

import org.junit.Assert;
import org.junit.Test;

import net.jr.dockerizedTest.memcached.MemCachedServer;
import net.spy.memcached.MemcachedClient;

public class MemCachedTest {

	@Test
	public void doTest() throws Exception {

		MemCachedServer server = new MemCachedServer();
		server.start();

		MemcachedClient client = new MemcachedClient(new InetSocketAddress("localhost", server.getPort()));
		client.set("testKey", 10, "testValue").get();
		Assert.assertEquals("testValue", client.get("testKey").toString());

		server.stop();

	}

}
