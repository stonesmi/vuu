package org.finos.vuu.core.table

import org.finos.toolbox.time.Clock
import org.finos.vuu.core.index.IndexedField

trait RowDataMerger {

  def mergeAndUpdateDefaultColumns(update: RowData, data: RowData): RowData

}

object RowDataMerger {

  def apply()(using clock: Clock): RowDataMerger = RowDataMergerImpl(clock)

}

private class RowDataMergerImpl(clock: Clock) extends RowDataMerger {

  def mergeLeftToRight(left: RowData, right: RowData): RowData = {

    val leftMap = 

    var newData: RowData = data

    update match {
      case update: RowWithData =>
        update.data.foreach({ case (field, value) => newData = newData.set(field, value) })
    }

    newData
  }

}

