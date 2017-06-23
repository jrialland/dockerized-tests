package net.jr.dockerizedTest;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListContainersParam;
import com.spotify.docker.client.DockerClient.ListImagesParam;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.Image;

public abstract class DockerSupport {

	private String image;

	private String containerId;

	private DockerClient docker;

	public DockerSupport(String image) {
		try {
			this.docker = DefaultDockerClient.fromEnv().build();
			this.image = image;
		} catch (DockerCertificateException e) {
			throw new RuntimeException(e);
		}
	}

	public void initContainer() {
		try {

			// ensure that we have the image
			boolean hasImage = false;
			for (Image img : docker.listImages(ListImagesParam.allImages())) {
				if (img.repoTags().contains(image)) {
					hasImage = true;
					break;
				}
			}
			if (!hasImage) {
				docker.pull(image);
			}

			final ContainerConfig.Builder configBuilder = ContainerConfig.builder().image(image);
			final ContainerCreation creation = docker.createContainer(configure(configBuilder));

			containerId = creation.id();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void startContainer() {
		try {
			docker.startContainer(containerId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void stopAndRemoveContainer() {
		try {
			docker.killContainer(containerId);
			docker.removeContainer(containerId);
			docker.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public ContainerInfo getContainerInfo() {
		try {
			return docker.inspectContainer(containerId);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public int getFirstHostPort() {
		String strPort = getContainerInfo().networkSettings().ports().values().stream().findFirst().get().get(0)
				.hostPort();
		return Integer.parseInt(strPort);
	}

	public boolean isExited() {
		try {
			return docker.listContainers(ListContainersParam.withStatusExited()).stream()
					.filter(container -> container.id().equals(containerId)).findAny().isPresent();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public abstract ContainerConfig configure(ContainerConfig.Builder configBuilder);
}
