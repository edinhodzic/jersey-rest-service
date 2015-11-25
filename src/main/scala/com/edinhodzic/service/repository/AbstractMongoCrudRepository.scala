package com.dods.service.repository

import com.edinhodzic.service.Paginated
import com.edinhodzic.service.domain.Identifiable
import com.edinhodzic.service.repository.{AbstractPartialCrudRepository, PartialUpdates, Queryable}
import com.edinhodzic.service.util.Converter
import com.mongodb.DBObject
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoCollection
import org.bson.types.ObjectId
import org.slf4j.{Logger, LoggerFactory}

import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

abstract class AbstractMongoCrudRepository[T <: Identifiable]
(converter: Converter[T, DBObject], mongoClient: MongoClient = MongoClient(), databaseName: String = null)
(implicit manifest: Manifest[T]) extends AbstractPartialCrudRepository[T] with PartialUpdates[T] with Queryable[T] {

  protected val logger: Logger = LoggerFactory getLogger getClass

  lazy private val collection: MongoCollection = createCollection

  def createCollection: MongoCollection = {
    val singularDomain: String = manifest.runtimeClass.getSimpleName.toLowerCase
    val dbName: String = if (Option(databaseName) isDefined) databaseName else s"${singularDomain}s"
    logger info s"using database name $dbName"
    mongoClient(dbName)(singularDomain)
  }

  override def create(resource: T): Try[T] = {
    logger info s"creating $resource"
    val dbObject: DBObject = converter serialise resource
    Try(collection insert dbObject) match {
      case Success(writeResult) if Option(writeResult).isDefined && writeResult.getN == 0 =>
        Success({
          resource id = s"${dbObject get "_id"}"
          resource
        })
      case Failure(throwable) => logAndFail(throwable)
      case _ => logAndFail(new RuntimeException(s"unknown creation failure for $resource"))
    }
  }

  override def read(resourceId: String): Try[Option[T]] = Failure(new RuntimeException("not yet implemented"))

  override def update(resourceId: String, updateQuery: String): Try[Option[AnyRef]] = Failure(new RuntimeException("not yet implemented"))

  override def delete(resourceId: String): Try[Option[Unit]] = Failure(new RuntimeException("not yet implemented"))

  override def query(queryString: String): Try[Paginated[T]] = Failure(new RuntimeException("not yet implemented"))

  protected def idQuery(resourceId: String): DBObject =
    MongoDBObject("_id" -> new ObjectId(resourceId))

  protected def logAndFail(throwable: Throwable): Failure[Nothing] = {
    logger error s"$throwable"
    Failure(throwable)
  }

}
