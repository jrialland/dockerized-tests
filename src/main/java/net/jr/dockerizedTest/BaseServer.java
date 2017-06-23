package net.jr.dockerizedTest;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerConfig.Builder;

public class BaseServer implements Server {

	private DockerSupport dockerSupport;

	protected BaseServer(String image, int defaultPort) {
		dockerSupport = new DockerSupport(image) {
			@Override
			public ContainerConfig configure(Builder configBuilder) {
				return exposePort(configBuilder, defaultPort).build();
			}
		};
		dockerSupport.initContainer();
	}

	protected DockerSupport getDockerSupport() {
		return dockerSupport;
	}

	public int getPort() {
		return dockerSupport.getFirstHostPort();
	}

	public void start() {
		dockerSupport.startContainer();
	}

	public void stop() {
		dockerSupport.stopAndRemoveContainer();
	}

	public boolean isRunning() {
		return !dockerSupport.isExited();
	}

	@Override
	protected void finalize() throws Throwable {
		if (isRunning()) {
			stop();
		}
		super.finalize();
	}

}
