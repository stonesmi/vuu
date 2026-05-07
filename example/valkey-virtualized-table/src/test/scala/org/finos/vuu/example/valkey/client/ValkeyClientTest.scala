package org.finos.vuu.example.valkey.client

import org.finos.toolbox.jmx.{MetricsProvider, MetricsProviderImpl}
import org.finos.toolbox.lifecycle.LifecycleContainer
import org.finos.toolbox.time.{Clock, DefaultClock}
import org.finos.vuu.example.valkey.ValkeyTestBase
import org.finos.vuu.example.valkey.client.options.ValkeyClientOptions

import java.util

class ValkeyClientTest extends ValkeyTestBase {

  Feature("Test we can connect to a remote Valkey") {

    Scenario("Can insert and retrieve by key") {

      given metrics: MetricsProvider = MetricsProviderImpl()
      given timeProvider: Clock = DefaultClock()
      given lifecycle: LifecycleContainer = LifecycleContainer()

      val client = ValkeyClient(ValkeyClientOptions()
        .withNode(container.getHost, container.getPort)
        .withHostAndPortMapper(container.getHostAndPortMapper))
      lifecycle.start()

      val jedis = client.getClient.get

      insertOrder(jedis, "A1", 10, 1000, 1000000L, "VOD.L", "GBP", "alice")
      insertOrder(jedis, "A2", 200, 5000, 2000000L, "AAPL.O", "USD", "bob")
      insertOrder(jedis, "A3", 150, 3000, 1500000L, "MSFT.O", "USD", "charlie")

      var value: util.Map[String, String] = jedis.hgetAll(getKey("A1"))
      value.get("trader") shouldEqual "alice"

      value = jedis.hgetAll(getKey("A2"))
      value.get("trader") shouldEqual "bob"

      value = jedis.hgetAll(getKey("A3"))
      value.get("trader") shouldEqual "charlie"

      lifecycle.thread.stop()
      lifecycle.stop()
    }

  }

}
