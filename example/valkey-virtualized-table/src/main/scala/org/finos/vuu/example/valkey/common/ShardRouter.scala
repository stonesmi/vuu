package org.finos.vuu.example.valkey.common

object ShardRouter {

  private final val ShardCount = 4
  private final val Mask = ShardCount - 1
  private final val shardTags: Array[String] = (1 to ShardCount).map(i => s"{s$i}").toArray

  def getShardTags: Array[String] = shardTags
  
  def shardTag(key: String): String = {
    val index = (key.hashCode & 0x7fffffff) & Mask
    shardTags(index)
  }

  def shardedKey(table: String, key: String): String = {
    val tag = shardTag(key)
    val sb = new java.lang.StringBuilder(table.length + tag.length + key.length + 2)
    sb.append(table)
      .append(':')
      .append(tag)
      .append(':')
      .append(key)
      .toString
  }

}
