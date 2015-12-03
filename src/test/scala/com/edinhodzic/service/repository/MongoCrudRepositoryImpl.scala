package com.edinhodzic.service.repository

import com.edinhodzic.service.domain.Resource
import com.edinhodzic.service.util.Converter
import com.mongodb.DBObject
import com.mongodb.casbah.MongoClient

class MongoCrudRepositoryImpl(converter: Converter[Resource, DBObject], mongoClient: MongoClient, databaseName: String)
  extends AbstractMongoCrudRepository(converter, mongoClient, databaseName)
