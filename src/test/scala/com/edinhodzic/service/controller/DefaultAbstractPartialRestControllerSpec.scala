package com.edinhodzic.service.controller

import javax.ws.rs.core.HttpHeaders.LOCATION
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.{CREATED, INTERNAL_SERVER_ERROR}

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

  class DefaultAbstractPartialRestController[T <: Identifiable : Manifest]
  (repository: AbstractPartialCrudRepository[T])
    extends AbstractPartialRestController[T](repository)

}
