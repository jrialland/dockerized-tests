
package net.jr.dockerizedTest.redis;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerConfig.Builder;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;

import net.jr.dockerizedTest.DockerSupport;

public class RedisServer {

	private static final String IMAGE = "redis:3.0.7";

	private static final int REDIS_DEFAULT_PORT = 6379;

	private DockerSupport dockerSupport;

	public RedisServer() {
		dockerSupport = new DockerSupport(IMAGE) {
			@Override
			public ContainerConfig configure(Builder configBuilder) {
				Map<String, List<PortBinding>> portBindings = new TreeMap<>();
				portBindings.put(Integer.toString(REDIS_DEFAULT_PORT),
						Arrays.asList(PortBinding.randomPort("0.0.0.0")));
				final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();
				return configBuilder.hostConfig(hostConfig).exposedPorts(Integer.toString(REDIS_DEFAULT_PORT)).build();
			}
		};
		dockerSupport.initContainer();
	}

	public int getPort() {
		return dockerSupport.getFirstHostPort();
	}

	public void start() throws Exception {
		dockerSupport.startContainer();
	}

	public void stop() {
		dockerSupport.stopAndRemoveContainer();
	}
}
