package com.edinhodzic.service.controller

import javax.ws.rs.core.Response.Status._

import com.edinhodzic.service.domain.Resource
import com.edinhodzic.service.repository.{PartialUpdates, AbstractPartialCrudRepository}
import org.specs2.mock.Mockito

import scala.util.{Failure, Try, Success}

class RestControllerWithPartialUpdatesSpec extends JerseySpecification with Mockito {
  isolated

  private val repository: AbstractPartialCrudRepositoryWithPartialUpdates = mock[AbstractPartialCrudRepositoryWithPartialUpdates]
  private val controller: RestControllerWithPartialUpdates[Resource] = new RestControllerWithPartialUpdates(repository)

  "Controller put function" should {

    val queryString: String = "{}"

    def mockRepositoryUpdateToReturnSuccess =
      mockRepositoryUpdateToReturn(Success(Some(resource)))

    def mockRepositoryUpdateToReturn(triedMaybeResource: Try[Option[Resource]]) =
      repository update(resourceId, queryString) returns triedMaybeResource

    "invoke repository update function" in {
      mockRepositoryUpdateToReturnSuccess
      controller put(resourceId, queryString)
      there was one(repository).update(resourceId, queryString)
    }

    "return http no content when repository update succeeds" in {
      mockRepositoryUpdateToReturnSuccess
      assertResponseStatusIs(NO_CONTENT)(controller put(resourceId, queryString))
    }

    "return no response body when repository update succeeds" in {
      mockRepositoryUpdateToReturnSuccess
      assertResponseBodyIs(null)(controller put(resourceId, queryString))
    }

    "return http internal server error when repository update fails" in {
      mockRepositoryUpdateToReturn(Failure(new RuntimeException))
      assertResponseStatusIs(INTERNAL_SERVER_ERROR)(controller put(resourceId, queryString))
    }

  }

  class AbstractPartialCrudRepositoryWithPartialUpdates extends AbstractPartialCrudRepository[Resource] with PartialUpdates[Resource] {
    override def create(resource: Resource): Try[Resource] = null

    override def delete(resourceId: String): Try[Option[Unit]] = null

    override def read(resourceId: String): Try[Option[Resource]] = null

    override def update(resourceId: String, update: String): Try[Option[AnyRef]] = null
  }

}
