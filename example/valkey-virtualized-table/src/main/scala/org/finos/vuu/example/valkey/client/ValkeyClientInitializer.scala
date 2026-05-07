package org.finos.vuu.example.valkey.client

import io.valkey.{ConnectionPoolConfig, DefaultJedisClientConfig, HostAndPort, JedisCluster, UnifiedJedis}
import org.finos.vuu.example.valkey.client.options.ValkeyClientOptions

import scala.jdk.CollectionConverters.*

class ValkeyClientInitializer(val options: ValkeyClientOptions) {

  def create(): JedisCluster = {
    val nodes = options.nodes
      .map { case (host, port) => new HostAndPort(host, port) }
      .asJava

    val clientConfig = DefaultJedisClientConfig.builder
      .timeoutMillis(options.timeoutMs)
      .hostAndPortMapper(options.hostAndPortMapper.orNull)
      .build

    val poolCfg = new ConnectionPoolConfig()
    poolCfg.setMaxTotal(options.maxTotal)
    poolCfg.setMaxIdle(options.maxIdle)
    poolCfg.setMinIdle(options.minIdle)

    new JedisCluster(nodes, clientConfig, options.maxAttempts, poolCfg)
  }

}
