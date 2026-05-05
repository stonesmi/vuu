//package org.finos.vuu.example.valkey.provider
//
//import com.typesafe.scalalogging.StrictLogging
//import org.finos.toolbox.logging.LogAtFrequency
//import org.finos.toolbox.time.Clock
//import org.finos.toolbox.time.TimeIt.timeIt
//import org.finos.vuu.core.sort.SortDirection
//import org.finos.vuu.core.table.RowWithData
//import org.finos.vuu.example.valkey.client.ValkeyClient
//import org.finos.vuu.example.valkey.store.ValkeyStore
//import org.finos.vuu.plugin.virtualized.table.{VirtualizedRange, VirtualizedSessionTable, VirtualizedViewPortKeys}
//import org.finos.vuu.provider.VirtualizedProvider
//import org.finos.vuu.viewport.{ViewPort, ViewPortColumns}
//
//class ValkeyVirtualizedProvider(val store: ValkeyStore)(using clock: Clock) extends VirtualizedProvider with StrictLogging {
//
//  override def runOnce(viewPort: ViewPort): Unit = {
//    logger.trace("[ValkeyVirtualizedProvider] Starting runOnce")
//
//    val sort = viewPort.getSort
//    val filter = viewPort.filterSpec
//
//    val range = viewPort.getRange
//
//    val valkeySort = getValkeySort(viewPort, viewPort.sortSpecInternal)
//
//
//    //typically we would want to get a bigger data set than the viewport is specifically looking at
//    //as is probably more efficient, in this case we'll get just what they are asking for....
//    val startIndex = Math.max((range.from - 5000), 0)
//    val endIndex = range.to + 5000
//
//    val (totalSize, bigOrders) = valkeySort match {
//
//      case sort: ValKeyFieldSort =>
//        sort.sortDirection match {
//          case SortDirection.Ascending => storage.loadRecordsInRangeByIndex("order", sort.fieldName, startIndex, endIndex)(pool)
//          case _ => storage.loadRecordsInRevRangeByIndex("order", sort.fieldName, startIndex, endIndex)(pool)
//        }
//      case ValkeyNoSort =>
//
//        logger.info(s"[ValkeyVirtualizedProvider] Loading orders from primary key $startIndex to $endIndex")
//        storage.loadRecordsInRange("order", startIndex, endIndex)(pool)
//
//    }
//
//    viewPort.table.asTable match {
//      case tbl: VirtualizedSessionTable =>
//        logger.info("[ReallyBigVirtualizedDataProvider] Set Range")
//        val (millisRange, _) = timeIt {
//          tbl.setRange(VirtualizedRange(startIndex, endIndex))
//        }
//
//        logger.info("[ReallyBigVirtualizedDataProvider] Set Size")
//        val (millisSize, _) = timeIt {
//          tbl.setSize(totalSize)
//        }
//        logger.info("[ReallyBigVirtualizedDataProvider] Adding rows ")
//        val (millisRows, _) = timeIt {
//          bigOrders.foreach({ case (dataMap, index) => {
//
//            //currency $ccy ric $ric quantity $qty price $prc side $side strategy $strategy parentId $parentId orderTimeMs $orderTimeMs
//            val orderId = dataMap.get("id").get.toString
//            val dataTyped = Map(
//              "id" -> orderId,
//              "ric" -> dataMap.get("ric").get.toString,
//              "quantity" -> dataMap.get("quantity").get.toString.toInt,
//              "price" -> dataMap.get("price").get.toString.toDouble,
//              "side" -> dataMap.get("side").get.toString,
//              "strategy" -> dataMap.get("strategy").get.toString,
//              "parentId" -> dataMap.get("parentId").get.toString.toInt,
//              "orderTimeMs" -> dataMap.get("orderTimeMs").get.toString.toLong,
//            )
//
//            val rowWithData = RowWithData(orderId, dataTyped)
//
//            tbl.processUpdateForIndex(index, orderId, rowWithData, clock.now())
//          }
//          })
//        }
//
//        logger.info("[ReallyBigVirtualizedDataProvider] Getting Primary Keys")
//        val (millisGetKeys, tableKeys) = timeIt {
//          tbl.primaryKeys
//        }
//
//        logger.info("[ReallyBigVirtualizedDataProvider] Setting Primary Keys")
//        val (millisSetKeys, _) = timeIt {
//          viewPort.setKeys(new VirtualizedViewPortKeys(tableKeys))
//        }
//
//    }
//  }
//
//  override def getUniqueValuesVPColumn(columnName: String, viewPortColumns: ViewPortColumns): Array[String] = ???
//
//  override def getUniqueValuesStartingWithVPColumn(columnName: String, starts: String, viewPortColumns: ViewPortColumns): Array[String] = ???
//
//  override def subscribe(key: String): Unit = {}
//
//  override def doStart(): Unit = {}
//
//  override def doStop(): Unit = {}
//
//  override def doInitialize(): Unit = {}
//
//  override def doDestroy(): Unit = {}
//
//  override val lifecycleId: String = this.getClass.toString
//}
