package com.edinhodzic.service.controller

import javax.ws.rs.core.Response.Status._

import com.edinhodzic.service.Paginated
import com.edinhodzic.service.domain.Resource
import com.edinhodzic.service.repository.{AbstractPartialCrudRepository, PartialUpdates, Queryable}
import org.specs2.mock.Mockito

import scala.util.{Failure, Success, Try}

class QueryableRestControllerWithPartialUpdatesSpec extends JerseySpecification with Mockito {
  isolated

  private val repository: QueryableAbstractPartialCrudRepositoryWithPartialUpdates = mock[QueryableAbstractPartialCrudRepositoryWithPartialUpdates]
  private val controller: QueryableRestControllerWithPartialUpdates[Resource] = new QueryableRestControllerWithPartialUpdates(repository)

  "Controller query function" should {

    val queryString: String = "{}"

    def mockRepositoryQueryToReturnSuccess =
      mockRepositoryQueryToReturn(Success(mock[Paginated[Resource]]))


    def mockRepositoryQueryToReturn(triedPaginatedResource: Try[Paginated[Resource]]) =
      repository query queryString returns triedPaginatedResource

    "invoke repository query function" in {
      mockRepositoryQueryToReturnSuccess
      controller query queryString
      there was one(repository).query(queryString)
    }

    "return http ok when repository query succeeds" in {
      mockRepositoryQueryToReturnSuccess
      assertResponseStatusIs(OK)(controller query queryString)
    }

    //    "return no response body when repository query succeeds" in {
    //      mockRepositoryQueryToReturnSuccess
    //      assertResponseBodyIs(null)(controller query queryString)
    //    }

    "return http internal server error when repository update fails" in {
      mockRepositoryQueryToReturn(Failure(new RuntimeException))
      assertResponseStatusIs(INTERNAL_SERVER_ERROR)(controller query queryString)
    }

  }

  class QueryableAbstractPartialCrudRepositoryWithPartialUpdates extends AbstractPartialCrudRepository[Resource] with PartialUpdates[Resource] with Queryable[Resource] {
    override def create(resource: Resource): Try[Resource] = null

    override def delete(resourceId: String): Try[Option[Unit]] = null

    override def read(resourceId: String): Try[Option[Resource]] = null

    override def update(resourceId: String, update: String): Try[Option[AnyRef]] = null

    override def query(queryString: String): Try[Paginated[Resource]] = null
  }

}
