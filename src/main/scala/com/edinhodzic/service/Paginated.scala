package com.edinhodzic.service

import scala.beans.BeanProperty

case class Paginated[T](@BeanProperty results: Array[T], @BeanProperty resultsTotal: Int, @BeanProperty page: Int) {
  @BeanProperty var pageTotal: Int = if (results.isEmpty) 0
  else resultsTotal / results.length +
    (if (resultsTotal % results.length > 1) 1 else 0)

}
