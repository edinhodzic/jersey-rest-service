package com.edinhodzic.service.repository

import com.edinhodzic.service.Paginated
import com.edinhodzic.service.domain.Identifiable
import com.edinhodzic.service.util.Converter
import com.mongodb.DBObject
import com.mongodb.casbah.Imports._
import com.mongodb.casbah.MongoCollection
import com.mongodb.util.JSON
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

  override def read(resourceId: String): Try[Option[T]] = {
    logger info s"reading $resourceId"
    Try(collection findOne idQuery(resourceId)) match {
      case Success(maybeDbObject) => maybeDbObject match {
        case Some(dbObject) => Success(Some(converter deserialise dbObject))
        case None => Success(None)
      }
      case Failure(throwable) => logAndFail(throwable)
      case _ => logAndFail(new RuntimeException(s"unknown read failure for $resourceId"))
    }
  }

  override def update(resourceId: String, updateQuery: String): Try[Option[AnyRef]] = {
    logger info s"updating $resourceId"
    val dbObject: DBObject = JSON.parse(updateQuery).asInstanceOf[DBObject]
    Try(collection update(idQuery(resourceId), dbObject)) match {
      case Success(writeResult) if Option(writeResult).isDefined && writeResult.getN == 1 =>
        Success(Some(writeResult)) // TODO this does not contain the update
      case Failure(throwable) => logAndFail(throwable)
      case x => logAndFail(new RuntimeException(s"unknown update failure for $resourceId"))
    }
  }

  override def delete(resourceId: String): Try[Option[Unit]] = {
    logger info s"deleting $resourceId"
    Try(collection remove idQuery(resourceId)) match {
      case Success(writeResult) =>
        if (Option(writeResult).isDefined && writeResult.getN > 0) Success(Some())
        else Success(None)
      case Failure(throwable) => logAndFail(throwable)
      case _ => logAndFail(new RuntimeException(s"unknown delete failure for $resourceId"))
    }
  }

  // TODO think about cursor isolation and snapshot mode : http://docs.mongodb.org/manual/core/cursors/
  // TODO cursor iterator iterates in batches of 20 by default
  override def query(queryString: String): Try[Paginated[T]] = {
    logger info s"querying $queryString"
    Try(collection find parse(queryString)) match {
      case Success(mongoCursor) =>
        val paginated: Paginated[T] = paginate(mongoCursor)
        logger info s"query $queryString produced result $paginated"
        Success(paginated)
      case Failure(throwable) => logAndFail(throwable)
    }
  }

  protected def idQuery(resourceId: String): DBObject =
    MongoDBObject("_id" -> new ObjectId(resourceId))

  protected def logAndFail(throwable: Throwable): Failure[Nothing] = {
    logger error s"$throwable"
    Failure(throwable)
  }

  private def parse(queryString: String): DBObject =
    JSON.parse(queryString).asInstanceOf[DBObject]

  private def paginate(mongoCursor: MongoCursor): Paginated[T] = {
    val dbObjectIterator: Iterator[DBObject] = mongoCursor take 20
    val subscriptionIterator: Iterator[T] = dbObjectIterator map (converter deserialise)
    val subscriptionArray: Array[T] = subscriptionIterator.toArray[T]
    // TODO remove the hardcoded page number (2) when the initial request can take in pagination
    new Paginated[T](subscriptionArray, mongoCursor size, 2)
  }

}
