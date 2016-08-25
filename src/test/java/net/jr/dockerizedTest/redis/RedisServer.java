
package net.jr.dockerizedTest.redis;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListImagesParam;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.PortBinding;

public class RedisServer {

  private String containerId;

  private DockerClient docker;

  private static final String IMAGE = "redis:3.0.7";

  private static final int REDIS_DEFAULT_PORT = 6379;

  public RedisServer() {
    try {

      docker = DefaultDockerClient.fromEnv().build();

      boolean hasImage = false;

      for (Image img : docker.listImages(ListImagesParam.allImages())) {
        if (img.repoTags().contains(IMAGE)) {
          hasImage = true;
          break;
        }
      }

      if (!hasImage) {
        docker.pull(IMAGE);
      }

      Map<String, List<PortBinding>> portBindings = new TreeMap<>();
      portBindings.put(Integer.toString(REDIS_DEFAULT_PORT), Arrays.asList(PortBinding.randomPort("0.0.0.0")));

      final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

      final ContainerConfig containerConfig = ContainerConfig.builder().image(IMAGE).hostConfig(hostConfig)
          .exposedPorts(Integer.toString(REDIS_DEFAULT_PORT)).build();

      final ContainerCreation creation = docker.createContainer(containerConfig);

      containerId = creation.id();

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int getPort() {
    try {
      ContainerInfo info = docker.inspectContainer(containerId);
      String hostPort = info.networkSettings().ports().get(Integer.toString(REDIS_DEFAULT_PORT)).get(0).hostPort();
      return Integer.parseInt(hostPort);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void start() throws Exception {
    docker.startContainer(containerId);
  }

  public void stop() {
    try {
      docker.killContainer(containerId);
      docker.removeContainer(containerId);
      docker.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
