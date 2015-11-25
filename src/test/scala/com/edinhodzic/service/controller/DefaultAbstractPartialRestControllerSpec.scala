package com.edinhodzic.service.controller

import javax.ws.rs.core.HttpHeaders.LOCATION
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status._

import com.edinhodzic.service.domain.{Identifiable, Resource}
import com.edinhodzic.service.repository.AbstractPartialCrudRepository
import org.specs2.mock.Mockito

import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

class DefaultAbstractPartialRestControllerSpec extends JerseySpecification with Mockito {
  isolated

  private val repository: AbstractPartialCrudRepository[Resource] = mock[AbstractPartialCrudRepository[Resource]]
  private val controller: DefaultAbstractPartialRestController[Resource] = new DefaultAbstractPartialRestController(repository)

  "Controller post function" should {

    def mockRepositoryCreateToReturnSuccess =
      mockRepositoryCreateToReturn(Success(resource))

    def mockRepositoryCreateToReturn(triedResource: Try[Resource]) =
      repository create resource returns triedResource

    "invoke repository create function" in {
      mockRepositoryCreateToReturnSuccess
      controller post resource
      there was one(repository).create(resource)
    }

    "return http created when repository create succeeds" in {
      mockRepositoryCreateToReturnSuccess
      assertResponseStatusIs(CREATED)(controller post resource)
    }

    "return correct location header when repository create succeeds" in {
      mockRepositoryCreateToReturnSuccess
      val response: Response = controller post resource
      response.getHeaderString(LOCATION) must beEqualTo("resource/55a3c507350000b400582c12")
    }

    "return resource as response body when repository create succeeds" in {
      mockRepositoryCreateToReturnSuccess
      assertResponseBodyIs(resource)(controller post resource)
    }

    "return http internal server error when repository create fails" in {
      mockRepositoryCreateToReturn(Failure(new RuntimeException))
      assertResponseStatusIs(INTERNAL_SERVER_ERROR)(controller post resource)
    }

  }

  "Controller get function" should {

    def mockRepositoryReadToReturnSuccessSome =
      mockRepositoryReadToReturn(Success(Some(resource)))

    def mockRepositoryReadToReturn(triedMaybeResource: Try[Option[Resource]]) =
      repository read anyString returns triedMaybeResource

    "invoke repository read function" in {
      mockRepositoryReadToReturnSuccessSome
      controller get resourceId
      there was one(repository).read(resourceId)
    }

    "return http ok when repository read succeeds with some" in {
      mockRepositoryReadToReturnSuccessSome
      assertResponseStatusIs(OK)(controller get resourceId)
    }

    "return resource as response body when repository read succeeds with some" in {
      mockRepositoryReadToReturnSuccessSome
      assertResponseBodyIs(resource)(controller get resourceId)
    }

    "return http not found when repository read succeeds with none" in {
      mockRepositoryReadToReturn(Success(None))
      assertResponseStatusIs(NOT_FOUND)(controller get resourceId)
    }

    "return http internal server error when repository read fails" in {
      mockRepositoryReadToReturn(Failure(new RuntimeException))
      assertResponseStatusIs(INTERNAL_SERVER_ERROR)(controller get resourceId)
    }

  }

  // TODO implement put function

  "Controller delete function" should {

    def mockRepositoryDeleteToReturnSuccess =
      mockRepositoryDeleteToReturn(Success(Some()))

    def mockRepositoryDeleteToReturn(triedMaybeUnit: Try[Option[Unit]]) =
      repository delete anyString returns triedMaybeUnit

    "invoke repository delete function" in {
      mockRepositoryDeleteToReturnSuccess
      controller delete resourceId
      there was one(repository).delete(resourceId)
    }

    "return http no content when repository delete succeeds with some" in {
      mockRepositoryDeleteToReturnSuccess
      assertResponseStatusIs(NO_CONTENT)(controller delete resourceId)
    }

    "return no response body when repository delete succeeds with some" in {
      mockRepositoryDeleteToReturnSuccess
      assertResponseBodyIs(null)(controller delete resourceId)
    }

    "return http not found when repository delete succeeds with none" in {
      mockRepositoryDeleteToReturn(Success(None))
      assertResponseStatusIs(NOT_FOUND)(controller delete resourceId)
    }

    "return http internal server error when repository delete fails" in {
      mockRepositoryDeleteToReturn(Failure(new RuntimeException))
      assertResponseStatusIs(INTERNAL_SERVER_ERROR)(controller delete resourceId)
    }

  }

  class DefaultAbstractPartialRestController[T <: Identifiable : Manifest]
  (repository: AbstractPartialCrudRepository[T])
    extends AbstractPartialRestController[T](repository)

}
