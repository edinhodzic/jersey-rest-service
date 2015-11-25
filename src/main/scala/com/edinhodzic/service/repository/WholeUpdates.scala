package com.edinhodzic.service.repository

import com.edinhodzic.service.domain.Identifiable

import scala.util.Try

trait WholeUpdates[T <: Identifiable] {

  def update(resource: T): Try[Option[T]]

}
