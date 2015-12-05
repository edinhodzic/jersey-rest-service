package com.edinhodzic.service.controller

import java.net.URI
import javax.ws.rs._
import javax.ws.rs.core.MediaType._
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.{NOT_FOUND, NO_CONTENT}

import com.edinhodzic.service.domain.Identifiable
import com.edinhodzic.service.repository.AbstractPartialCrudRepository
import org.slf4j.{Logger, LoggerFactory}

import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

abstract class AbstractPartialRestController[T <: Identifiable : Manifest](abstractCrudRepository: AbstractPartialCrudRepository[T]) {

  protected val logger: Logger = LoggerFactory getLogger getClass

  @POST
  @Consumes(Array(APPLICATION_JSON))
  @Produces(Array(APPLICATION_JSON))
  def post(resource: T): Response = {
    logger info s"posting $resource"
    process[T](abstractCrudRepository create resource,
      t => Response created uri(t) entity t build())
  }

  @GET
  @Path("{resourceId}")
  @Produces(Array(APPLICATION_JSON))
  def get(@PathParam("resourceId") resourceId: String): Response = {
    logger info s"getting $resourceId"
    process[Option[T]](abstractCrudRepository read resourceId, {
      case Some(resource) => Response ok() entity resource build()
      case None => notFound
    })
  }

  @DELETE
  @Path("{resourceId}")
  def delete(@PathParam("resourceId") resourceId: String): Response = {
    logger info s"deleting $resourceId"
    process[Option[Unit]](abstractCrudRepository delete resourceId, {
      case Some(resource) => noContent
      case None => notFound
    })
  }

  private def notFound: Response = Response status NOT_FOUND build()

  protected def noContent: Response = Response status NO_CONTENT build()

  protected def serverError: Response = Response serverError() build()

  private def uri(resource: T)(implicit manifest: Manifest[T]): URI =
    new URI(s"${manifest.runtimeClass.getSimpleName.toLowerCase}/${resource id}")

  protected def process[S](repositoryFunction: (Try[S]), successFunction: (S => Response)): Response =
    repositoryFunction match {
      case Success(subject) => successFunction(subject)
      case Failure(throwable) =>
        logger error s"$throwable"
        Response serverError() build()
    }
}
