package com.edinhodzic.service.repository

import com.edinhodzic.service.Paginated

import scala.util.Try

trait Queryable[T] {

  def query(queryString: String): Try[Paginated[T]]

}
