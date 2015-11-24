package com.edinhodzic.service.controller

import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status

import com.edinhodzic.service.domain.Resource
import org.specs2.mutable.SpecificationWithJUnit

trait JerseySpecification extends SpecificationWithJUnit {

  private[controller] val resourceId: String = "55a3c507350000b400582c12"
  private[controller] implicit val resource: Resource = new Resource {
    id = resourceId
  }

  private[controller] def assertResponseStatusIs(status: Status)(implicit response: Response) =
    response.getStatus must beEqualTo(status.getStatusCode)

  private[controller] def assertResponseBodyIs(resource: Resource)(implicit response: Response) =
    response.getEntity must beEqualTo(resource)

}
