package org.finos.vuu.example.valkey

import com.typesafe.scalalogging.StrictLogging
import io.valkey.params.ScanParams
import io.valkey.{HostAndPortMapper, Jedis, JedisCluster, JedisPooled, UnifiedJedis}
import org.finos.vuu.example.valkey.client.ValkeyClient
import org.finos.vuu.viewport.InMemViewPortCallable.logger
import org.scalatest.featurespec.AnyFeatureSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, GivenWhenThen}

import scala.jdk.CollectionConverters.*
import scala.util.Using
import scala.util.control.NonFatal

class ValkeyTestBase extends AnyFeatureSpec with BeforeAndAfterAll with Matchers with StrictLogging {

  protected final val container: ValkeyClusterContainer = ValkeyClusterContainer()
  protected final val shardRouter = ShardRouter(container.getClusterSize)
  protected final val orderHSetName = "order"

  override def beforeAll(): Unit = {
    super.beforeAll()
    container.start()
  }

  override def afterAll(): Unit = {
    container.stop()
    super.afterAll()
  }

  protected def getKey(orderId: String): String = {
    val shardTag = shardRouter.shardTag(orderId)
    s"$orderHSetName:$shardTag:$orderId"
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

      val shardTag = shardRouter.shardTag(orderId)
      val key = s"$orderHSetName:$shardTag:$orderId"

      val fields = Map(
        "orderId" -> orderId,
        "quantity" -> quantity.toString,
        "price" -> price.toString,
        "timestamp" -> timestamp.toString,
        "ric" -> ric,
        "currency" -> currency,
        "trader" -> trader
      )
      pipeline.hset(key, fields.asJava)
      //filter indices
      pipeline.sadd(s"idx-$orderHSetName:$shardTag:ric:$ric", ric)

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

