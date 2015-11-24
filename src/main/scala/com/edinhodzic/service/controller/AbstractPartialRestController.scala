package com.edinhodzic.service.controller

import java.net.URI
import javax.ws.rs._
import javax.ws.rs.core.MediaType._
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.{NOT_FOUND, NOT_IMPLEMENTED}

import com.edinhodzic.service.domain.Identifiable
import com.edinhodzic.service.repository.AbstractPartialCrudRepository
import org.slf4j.{Logger, LoggerFactory}

import scala.language.postfixOps
import scala.util.{Failure, Success}

abstract class AbstractPartialRestController[T <: Identifiable : Manifest](abstractCrudRepository: AbstractPartialCrudRepository[T]) {

  protected val logger: Logger = LoggerFactory getLogger getClass

  @POST
  @Consumes(Array(APPLICATION_JSON))
  @Produces(Array(APPLICATION_JSON))
  def post(resource: T): Response = {
    logger info s"posting $resource"
    abstractCrudRepository create resource match {
      case Success(subject) => Response created uri(subject) entity subject build()
      case Failure(throwable) =>
        logger error s"$throwable"
        Response serverError() build()
    }
  }

  @GET
  @Path("{resourceId}")
  @Produces(Array(APPLICATION_JSON))
  def get(@PathParam("resourceId") resourceId: String): Response = {
    logger info s"getting $resourceId"
    abstractCrudRepository read resourceId match {
      case Success(maybeResource) => maybeResource match {
        case Some(resource) => Response ok() entity resource build()
        case None => Response status NOT_FOUND build()
      }
      // TODO address pattern matching repetition between this and the post function above
      case Failure(throwable) =>
        logger error s"$throwable"
        Response serverError() build()
    }
  }

  @PUT
  @Path("{resourceId}")
  @Consumes(Array(APPLICATION_JSON))
  def put(@PathParam("resourceId") resourceId: String, resource: T): Response = notImplemented

  @DELETE
  @Path("{resourceId}")
  def delete(@PathParam("resourceId") resourceId: String): Response = notImplemented

  protected def notImplemented: Response = Response status NOT_IMPLEMENTED build()

  private def uri(resource: T)(implicit manifest: Manifest[T]): URI =
    new URI(s"${manifest.runtimeClass.getSimpleName.toLowerCase}/${resource id}")

}
