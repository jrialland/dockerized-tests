
package net.jr.dockerizedTest.mysql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ListImagesParam;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerCreation;
import com.spotify.docker.client.messages.ContainerInfo;
import com.spotify.docker.client.messages.HostConfig;
import com.spotify.docker.client.messages.Image;
import com.spotify.docker.client.messages.PortBinding;

public class MySqlServer {

  private static final Logger LOGGER = LoggerFactory.getLogger(MySqlServer.class);

  private static final String IMAGE = "mysql:5.5.51";

  private static final int MYSQL_DEFAULT_PORT = 3306;

  private static final String MYSQL_ROOT_PASSWORD = "root_password";

  private String containerId;

  private DockerClient docker;

  private boolean stopped = true;

  public MySqlServer() {
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
      portBindings.put(Integer.toString(MYSQL_DEFAULT_PORT), Arrays.asList(PortBinding.randomPort("0.0.0.0")));

      final HostConfig hostConfig = HostConfig.builder().portBindings(portBindings).build();

      final ContainerConfig containerConfig = ContainerConfig.builder().image(IMAGE)
          .env("MYSQL_ROOT_PASSWORD=" + MYSQL_ROOT_PASSWORD).hostConfig(hostConfig).exposedPorts("3306").build();

      final ContainerCreation creation = docker.createContainer(containerConfig);

      containerId = creation.id();

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public void start() throws Exception {
    if (stopped) {
      docker.startContainer(containerId);
      Connection cnx = null;
      LOGGER.info("Trying to obtain connection to mysql ...");
      for (int i = 0; i < 20; i++) {
        LOGGER.info("    Attempt " + (i + 1));
        try {
          cnx = DriverManager.getConnection(getJdbcUrl("mysql"), "root", MYSQL_ROOT_PASSWORD);
          LOGGER.info("     -> ok, Got connection  : " + cnx);
          break;
        } catch (Exception e) {
          LOGGER.info("     -> failure");
          Thread.sleep(1000);
        }
      }
      if (cnx == null) {
        throw new IllegalStateException();
      } else {
        stopped = false;
        cnx.close();
      }
    }
  }

  public void stop() {
    try {
      docker.killContainer(containerId);
      docker.removeContainer(containerId);
      docker.close();
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      stopped = true;
    }
  }

  @Override
  protected void finalize() throws Throwable {
    if (!stopped) {
      stop();
    }
    super.finalize();
  }

  public String getJdbcUrl(String dbName) {
    final String dbPath = dbName == null || dbName.trim().isEmpty() ? "" : "/" + dbName;
    try {
      ContainerInfo info = docker.inspectContainer(containerId);
      String hostPort = info.networkSettings().ports().get("3306").get(0).hostPort();
      return "jdbc:mysql://127.0.0.1:" + hostPort + dbPath + "?useSSL=false";
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public String getRootPassword() {
    return MYSQL_ROOT_PASSWORD;
  }

  public void createDb(String dbName) throws SQLException {
    Connection cnx = DriverManager.getConnection(getJdbcUrl("mysql"), "root", MYSQL_ROOT_PASSWORD);
    cnx.createStatement().execute("create database " + dbName + " charset 'utf8'");
    cnx.close();
  }

}
