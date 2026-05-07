package org.finos.vuu.example.valkey

import com.typesafe.scalalogging.StrictLogging
import io.valkey.JedisCluster
import org.finos.vuu.example.valkey.common.ShardRouter
import org.scalatest.BeforeAndAfterAll
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers

import scala.jdk.CollectionConverters.*

class ValkeyTestBase extends AnyFeatureSpec with BeforeAndAfterAll with Matchers with StrictLogging {

  protected final val container: ValkeyClusterContainer = ValkeyClusterContainer()
  protected final val orderHSetName = "order"

  override def beforeAll(): Unit = {
    super.beforeAll()
    container.start()
  }

  override def afterAll(): Unit = {
    container.stop()
    super.afterAll()
  }

  protected def insertOrder(
                             jedis: JedisCluster,
                             orderId: String,
                             quantity: Int,
                             price: Long,
                             timestamp: Long,
                             ric: String,
                             currency: String,
                             trader: String
                           ): Unit = {
    val pipeline = jedis.pipelined()
    try {

      val shardTag = ShardRouter.shardTag(orderId)
      val key = ShardRouter.shardedKey(orderHSetName, orderId)

      val fields = Map(
        "orderId" -> orderId,
        "quantity" -> quantity.toString,
        "price" -> price.toString,
        "timestamp" -> timestamp.toString,
        "ric" -> ric,
        "currency" -> currency,
        "trader" -> trader
      ) //TODO Move this to binary
      pipeline.hset(key, fields.asJava)
      //filter indices
      pipeline.sadd(s"idx-$orderHSetName:$shardTag:ric:$ric", orderId)

      //sort indices
      pipeline.zadd(s"idx-$orderHSetName:$shardTag:sort:quantity", quantity, orderId)
      pipeline.zadd(s"idx-$orderHSetName:$shardTag:sort:price", price.toDouble, orderId)

      logger.info(s"Ingested order $orderId into shard $shardTag")
    } catch {
      case e: Exception => logger.error("Failed to publish order", e)
    } finally {
      pipeline.sync()
    }
  }

}

