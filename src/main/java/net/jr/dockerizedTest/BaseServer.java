package net.jr.dockerizedTest;

import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerConfig.Builder;

public class BaseServer implements Server {

	private final Logger LOGGER;

	private DockerSupport dockerSupport;

	protected BaseServer(String image, int defaultPort) {
		this(new DockerSupport(image) {
			@Override
			public ContainerConfig configure(Builder configBuilder) {
				return exposePort(configBuilder, defaultPort).build();
			}
		});
	}

	protected BaseServer(DockerSupport dockerSupport) {
		LOGGER = LoggerFactory.getLogger(getClass());
		this.dockerSupport = dockerSupport;
		this.dockerSupport.initContainer();
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

	protected void pollUntilSuccess(final int nPolls, final long delayBetweenPolls, final Callable<?> action) {
		Object obj = null;
		for (int i = 0; i < nPolls; i++) {
			LOGGER.info("attempt " + (i + 1) + "/" + nPolls);
			try {
				obj = action.call();
				break;
			} catch (Exception e) {
				LOGGER.info("\t-> failure");
				if (getDockerSupport().isExited()) {
					System.out.println(dockerSupport.getLogs());
					throw new IllegalStateException("docker container has exited !");
				} else {
					try {
						Thread.sleep(delayBetweenPolls);
					} catch (InterruptedException interruptedException) {
						throw new RuntimeException(interruptedException);
					}
				}
			}
		}

		if (obj != null && obj instanceof AutoCloseable) {
			try {
				((AutoCloseable) obj).close();
			} catch (Exception e) {
				LOGGER.error("while closing", e);
			}
		}
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
