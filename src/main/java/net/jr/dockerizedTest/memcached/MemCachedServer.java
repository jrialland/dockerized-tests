package net.jr.dockerizedTest.memcached;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerConfig.Builder;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.PortBinding;

import net.jr.dockerizedTest.DockerSupport;

public class MemCachedServer {

	private static final String IMAGE = "library/memcached:1.4.37-alpine";

	private static final int DEFAULT_PORT = 11211;

	private DockerSupport dockerSupport;

	public MemCachedServer() {
		dockerSupport = new DockerSupport(IMAGE) {
			@Override
			public ContainerConfig configure(Builder configBuilder) {
				Map<String, List<PortBinding>> portBindings = new TreeMap<>();
				portBindings.put(Integer.toString(DEFAULT_PORT), Arrays.asList(PortBinding.randomPort("0.0.0.0")));
				final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();
				return configBuilder.hostConfig(hostConfig).exposedPorts(Integer.toString(DEFAULT_PORT)).build();
			}
		};
		dockerSupport.initContainer();
	}

	public void start() {
		dockerSupport.startContainer();
	}

	public void stop() {
		dockerSupport.stopAndRemoveContainer();
	}

	public int getPort() {
		return dockerSupport.getFirstHostPort();
	}
}
