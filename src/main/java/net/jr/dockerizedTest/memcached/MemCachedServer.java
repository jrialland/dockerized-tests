package net.jr.dockerizedTest.memcached;

import net.jr.dockerizedTest.BaseServer;

public class MemCachedServer extends BaseServer {

	private static final String IMAGE = "library/memcached:1.4.37-alpine";

	private static final int DEFAULT_PORT = 11211;

	public MemCachedServer() {
		super(IMAGE, DEFAULT_PORT);
	}
}
