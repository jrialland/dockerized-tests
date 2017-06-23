
package net.jr.dockerizedTest.redis;

import net.jr.dockerizedTest.BaseServer;

public class RedisServer extends BaseServer {

	private static final String IMAGE = "redis:3.0.7";

	private static final int DEFAULT_PORT = 6379;

	public RedisServer() {
		super(IMAGE, DEFAULT_PORT);
	}

}
