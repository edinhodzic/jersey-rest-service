package com.edinhodzic.service.repository

import com.edinhodzic.service.domain.Identifiable

import scala.util.Try

trait AbstractPartialCrudRepository[T <: Identifiable] {

  def create(resource: T): Try[T]

  def read(resourceId: String): Try[Option[T]]

  //def update(resource: T): Try[Option[T]]

  def delete(resourceId: String): Try[Option[Unit]]

}