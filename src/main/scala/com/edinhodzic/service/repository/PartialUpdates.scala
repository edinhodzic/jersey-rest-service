package com.edinhodzic.service.repository

import com.edinhodzic.service.domain.Identifiable

import scala.util.Try

trait PartialUpdates[T <: Identifiable] {

  def update(resourceId: String, update: String): Try[Option[AnyRef]]

}
