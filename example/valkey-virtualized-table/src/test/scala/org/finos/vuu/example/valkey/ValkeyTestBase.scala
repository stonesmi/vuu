package org.finos.vuu.example.valkey

import com.dimafeng.testcontainers.ForAllTestContainer
import io.valkey.JedisPooled
import io.valkey.search.schemafields.*
import org.scalatest.GivenWhenThen
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers

import scala.jdk.CollectionConverters.*

class ValkeyTestBase extends AnyFeatureSpec with GivenWhenThen with Matchers with ForAllTestContainer {

  override val container: ValkeyContainer = ValkeyContainer()

  override def afterStart(): Unit = {
    createData()
    super.afterStart()
  }

  private def createData(): Unit = {
    val client = new JedisPooled(container.getHost, container.getPort)

    // Create index
    client.ftCreate("orders-idx",
      TextField.of("orderId"),
      NumericField.of("quantity"),
      NumericField.of("price"),
      NumericField.of("timestamp"),
      TextField.of("ric"),
      TextField.of("currency"),
      TextField.of("trader")
    )

    insertOrder(client, "A1", 10, 1000, 1000000L, "VOD.L", "GBP", "alice")
    insertOrder(client,"A2", 200, 5000, 2000000L, "AAPL.O", "USD", "bob")
    insertOrder(client,"A3", 150, 3000, 1500000L, "MSFT.O", "USD", "charlie")

    val query =
        """
          @quantity:[100 +inf]
          SORTBY timestamp DESC
          LIMIT 0 10
          """

      val results = client.ftSearch("orders-idx", query)

      results.getTotalResults shouldEqual 2L
  }

  private def insertOrder(
                   client: JedisPooled,
                   orderId: String,
                   quantity: Int,
                   price: Long,
                   timestamp: Long,
                   ric: String,
                   currency: String,
                   trader: String
                 ): Unit = {

    val key = s"order:$orderId"

    val fields = Map(
      "orderId" -> orderId,
      "quantity" -> quantity.toString,
      "price" -> price.toString,
      "timestamp" -> timestamp.toString,
      "ric" -> ric,
      "currency" -> currency,
      "trader" -> trader
    )

    client.hset(key, fields.asJava)
  }

}
