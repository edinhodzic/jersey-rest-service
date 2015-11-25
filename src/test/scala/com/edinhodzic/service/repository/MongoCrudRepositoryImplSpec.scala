package com.edinhodzic.service.repository

import com.edinhodzic.service.domain.Resource
import com.edinhodzic.service.util.Converter
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoCollection
import com.mongodb.casbah.commons.Imports
import org.specs2.mock.Mockito
import org.specs2.mutable.SpecificationWithJUnit

import scala.language.postfixOps

class MongoCrudRepositoryImplSpec extends SpecificationWithJUnit with Mockito {
  isolated

  private val collection: MongoCollection = mock[MongoCollection]
  private val converter: Converter[Resource, DBObject] = mock[Converter[Resource, DBObject]]
  private val repository: MongoCrudRepositoryImpl = new MongoCrudRepositoryImpl(converter, mock[MongoClient], "databaseName") {
    override def createCollection: MongoCollection = collection
  }

  private val writeResult: WriteResult = mock[WriteResult]
  private val dbObject: DBObject = mock[DBObject]

  private val resourceId: String = "55e9b1df456895dafe48059a"
  private implicit val resource: Resource = new Resource {
    id = resourceId
  }

  private val idQuery: Imports.DBObject = MongoDBObject("_id" -> new ObjectId(resourceId))

  "repository create function" should {

    def mockConverterSerialiseFunction: DBObject = {
      dbObject get "_id" returns resourceId
      converter serialise resource returns dbObject
      dbObject
    }

    def mockCollectionInsertToReturn(n: Int) = {
      writeResult.getN returns n
      collection insert dbObject returns writeResult
    }

    "invoke converter serialise and collection insert" in {
      mockCollectionInsertToReturn(0)

      repository create resource
      there was one(converter).serialise(resource)
      there was one(collection).insert(dbObject)
    }

    "return success when collection insert succeeds and write result is not empty" in {
      mockConverterSerialiseFunction
      mockCollectionInsertToReturn(0)
      repository create resource must beSuccessfulTry
    }

    "return failure when collection insert succeeds and write result is empty" in {
      mockCollectionInsertToReturn(-1)
      repository create resource must beFailedTry
    }

    "return failure when collection insert throws an exception" in {
      collection insert any throws new RuntimeException
      repository create resource must beFailedTry
    }

    "return a resource with an id when collection insert succeeds" in {
      mockConverterSerialiseFunction
      mockCollectionInsertToReturn(0)
      (repository create resource).get.id mustEqual "55e9b1df456895dafe48059a"
    }

  }

  // TODO test read, update and delete functions

}
