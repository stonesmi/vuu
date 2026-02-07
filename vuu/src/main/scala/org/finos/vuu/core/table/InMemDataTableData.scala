package org.finos.vuu.core.table

import org.finos.toolbox.collection.array.ImmutableArray
import org.finos.toolbox.time.Clock
import org.finos.vuu.core.table.datatype.EpochTimestamp
import org.finos.vuu.feature.inmem.InMemTablePrimaryKeys

import java.util.concurrent.ConcurrentHashMap
import scala.collection.immutable.VectorMap

trait InMemDataTableData extends TableData { }

object InMemDataTableData {

  def apply()(using clock: Clock): InMemDataTableData = new InMemDataTableDataImpl(VectorMap.empty, rowDataMerger)

}

private class InMemDataTableDataImpl(tableData: Map[String, RowData], rowDataMerger: RowDataMerger) extends InMemDataTableData {

  private lazy val primaryKeys = InMemTablePrimaryKeys(ImmutableArray.from(tableData.keys))

  override def primaryKeyValues: TablePrimaryKeys = primaryKeys

  override def setKeyAt(index: Int, key: String): Unit = ???

  override def dataByKey(key: String): RowData = tableData.getOrElse(key, null)

  override def update(key: String, update: RowData): InMemDataTableData = {
    val existingRowData = tableData.getOrElse(key, EmptyRowData)
    val mergedRowData = rowDataMerger.mergeAndUpdateDefaultColumns(update, existingRowData)
    val newTableData = tableData + (key -> mergedRowData)
    InMemDataTableDataImpl(newTableData, rowDataMerger)     
  }
    
  override def delete(key: String): InMemDataTableData = {
    tableData.getOrElse(key, null) match {
      case RowWithData(key, data) => 
        InMemDataTableDataImpl(tableData.removed(key), rowDataMerger)
      case _ => this
    } 
  }

  def deleteAll(): InMemDataTableData = {
    if (tableData.isEmpty) this 
    else InMemDataTableDataImpl(VectorMap.empty, rowDataMerger)
  }

}

