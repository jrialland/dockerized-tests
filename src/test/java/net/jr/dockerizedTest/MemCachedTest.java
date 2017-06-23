package net.jr.dockerizedTest;

import java.net.InetSocketAddress;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import net.jr.dockerizedTest.memcached.MemCachedServer;
import net.spy.memcached.MemcachedClient;

public class MemCachedTest {

	private static MemCachedServer server;

	@BeforeClass
	public static void setupClass() throws Exception {
		server = new MemCachedServer();
		server.start();
	}

	@AfterClass
	public static void afterClass() {
		server.stop();
	}

	@Test
	public void doTest() throws Exception {
		MemcachedClient client = new MemcachedClient(new InetSocketAddress("localhost", server.getPort()));
		client.set("testKey", 10, "testValue").get();
		Assert.assertEquals("testValue", client.get("testKey").toString());

	}

}
