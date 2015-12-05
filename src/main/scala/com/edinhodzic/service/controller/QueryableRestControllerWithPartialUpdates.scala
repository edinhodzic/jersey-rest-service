package com.edinhodzic.service.controller

import javax.ws.rs.core.MediaType._
import javax.ws.rs.core.Response
import javax.ws.rs.{Consumes, POST, Path, Produces}


import com.edinhodzic.service.domain.Identifiable
import com.edinhodzic.service.repository.{Queryable, PartialUpdates, AbstractPartialCrudRepository}

import scala.util.{Failure, Success}

class QueryableRestControllerWithPartialUpdates[T <: Identifiable : Manifest]
(abstractCrudRepository: AbstractPartialCrudRepository[T] with PartialUpdates[T] with Queryable[T])
  extends RestControllerWithPartialUpdates[T](abstractCrudRepository) {

  @POST
  @Path("/query")
  @Consumes(Array(APPLICATION_JSON))
  @Produces(Array(APPLICATION_JSON))
  def query(queryString: String): Response =
    abstractCrudRepository.query(queryString) match {
      case Success(paginatedSubscriptions) =>
        Response ok() entity paginatedSubscriptions build()
      case Failure(throwable) =>
        logger error s"$throwable"
        serverError
    }

}
