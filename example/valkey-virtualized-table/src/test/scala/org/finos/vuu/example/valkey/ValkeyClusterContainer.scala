package org.finos.vuu.example.valkey

import io.valkey.{HostAndPort, HostAndPortMapper}
import org.slf4j.LoggerFactory
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.images.builder.ImageFromDockerfile

class ValkeyClusterContainer
  extends GenericContainer[ValkeyClusterContainer](
    new ImageFromDockerfile()
      .withFileFromClasspath("Dockerfile", "Dockerfile")
      .withFileFromClasspath("entrypoint.sh", "entrypoint.sh")
  ) {

  val internalPorts: Set[Int] = Set(7000, 7001, 7002, 17000, 17001, 17002)

  withExposedPorts(internalPorts.toSeq.map(Int.box): _*)

  withLogConsumer(new Slf4jLogConsumer(logger))

  waitingFor(Wait.forLogMessage(".*Valkey cluster is ready!.*", 1))

  def getPort: Int = getMappedPort(7000)

  def getHostAndPortMapper: HostAndPortMapper = (hap: HostAndPort) => {
    if (internalPorts.contains(hap.getPort)) {
      // Only map if it's an internal port (7000, 7001, or 7002)
      new HostAndPort(getHost, getMappedPort(hap.getPort))
    } else {
      // If it's already a mapped port (like 55108), return it unchanged
      hap
    }
  }

  def getClusterSize: Int = 3
}