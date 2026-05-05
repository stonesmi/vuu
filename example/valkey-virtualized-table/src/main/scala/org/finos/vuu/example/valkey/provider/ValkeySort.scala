//package org.finos.vuu.example.valkey.provider
//
//import org.finos.vuu.core.sort.SortDirection
//import org.finos.vuu.viewport.ViewPort
//
//trait ValkeySort { }
//
//object ValkeySort {
//  
//  def apply(viewPort: ViewPort): ValkeySort = {
//    
//  }
//  
//}
//
//case class ValKeyFieldSort(fieldName: String, sortDirection: SortDirection.TYPE) extends ValkeySort
//
//object ValkeyNoSort extends ValkeySort
//
//private def getValkeySort(viewPort: ViewPort, sortSpecInternal: SortSpecInternal): ValkeySort = {
//  if (viewPort.sortSpecInternal.keys.size > 0) {
//    val sortField = viewPort.sortSpecInternal.keys.take(1).head
//    val ascDesc = viewPort.sortSpecInternal.values.take(1).head
//    ValKeyFieldSort(sortField, ascDesc)
//  } else {
//    ValkeyNoSort
//  }
//}
