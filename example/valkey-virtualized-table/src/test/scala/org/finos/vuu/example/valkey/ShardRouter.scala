package org.finos.vuu.example.valkey

trait ShardRouter {

  def shardTag(primaryKey: String): String

}

object ShardRouter {

  def apply(shardCount: Int = 1): ShardRouter = ShardRouterImpl(shardCount)

}

private case class ShardRouterImpl(shardCount: Int) extends ShardRouter {

  /** Returns the shard tag for the given primaryKey. */
  def shardTag(primaryKey: String): String = {
    val hash = Math.abs(primaryKey.hashCode)
    val idx = hash % shardCount
    s"{s${idx + 1}}"
  }

}
