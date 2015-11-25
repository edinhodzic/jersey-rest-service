package com.edinhodzic.service.controller

import javax.ws.rs.core.MediaType._
import javax.ws.rs.core.Response
import javax.ws.rs.{Consumes, PUT, Path, PathParam}

import com.edinhodzic.service.domain.Identifiable
import com.edinhodzic.service.repository.{AbstractPartialCrudRepository, PartialUpdates}

import scala.language.postfixOps

class RestControllerWithPartialUpdates[T <: Identifiable : Manifest]
(repository: AbstractPartialCrudRepository[T] with PartialUpdates[T])
  extends AbstractPartialRestController[T](repository) {

  @PUT
  @Path("/{resourceId}")
  @Consumes(Array(APPLICATION_JSON))
  def put(@PathParam("resourceId") resourceId: String, jsonString: String): Response = {
    logger info s"putting $resourceId"
    process[Option[AnyRef]](repository update(resourceId, jsonString),
    _ => noContent)
  }

}