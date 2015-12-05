# About

This is a work in process REST service abstraction. It assumes and operates on a set of conventions around CRUD and query operations.

[![Build Status](https://travis-ci.org/edinhodzic/jersey-rest-service.svg?branch=master)](https://travis-ci.org/edinhodzic/jersey-rest-service)

## Service conventions

This service abstraction conforms to a set of conventions regarding REST and database naming.

### REST

| Method | Description | Collection URI HTTP response       | Item URI HTTP response             |
|--------|-------------|------------------------------------|------------------------------------|
| POST   | Create      | `201 Created` / `409 Conflict`?    | unsupported                        |
| GET    | Read        | unsupported but probably should be | `200 Ok` / `404 Not Found`         |
| PUT    | Update      | unsupported                        | `204 No Content` / `404 Not Found` |
| DELETE | Delete      | unsupported                        | `204 No Content` / `404 Not Found` |

    TODO add query endpoint convention

<sup>**Top tip** : use the [`jersey-rest-service-archetype`](https://github.com/edinhodzic/jersey-rest-service-archetype) to very quickly create RESTful web service projects from scratch which use this libary and therefore the above conventions</sup>

# What's under the hood?

Implementation:

- [Scala](http://www.scala-lang.org/)
- [Jersey](https://jersey.java.net/)
- [Spring Boot](http://projects.spring.io/spring-boot/)
- [Casbah](https://mongodb.github.io/casbah/)
- [Logback](http://logback.qos.ch/)

Testing:

- [Specs2](https://etorreborre.github.io/specs2/)

# Quick start

Follow the below steps to implement a simple service using the abstraction API and then run and invoke service operations.

## Implementation

An implementation needs to have three things:

- controller : this will handle HTTP requests
- repository : this will perform database queries
- converter : this will convert a domain model objects to a database object equivalent and vice versa

The controller and repository rely on abstractions within this library hence they are quite succinct pieces of code. Suppose we were implementing a user REST service; we would need a:

- `UserRestController`
- `UserCrudRepository`
- `UserConverter`

Let's build this bottom up.

### Domain model implementation

    case class User(@BeanProperty var data: String) extends Identifiable {
      def this() = this(null)
    }

### Converter implementation

    @Component
    class UserConverter extends Converter[User, DBObject] {
    
      override def serialise(user: User): DBObject =
        MongoDBObject.newBuilder
          .+=("data" -> user.data)
          .result()
    
      override def deserialise(dbObject: DBObject): User =
        User(dbObject.get("data").asInstanceOf[String])
    
    }
    
### Repository implementation

    @Component
    class UserCrudRepository @Autowired()
    (converter: Converter[User, DBObject])
      extends AbstractMongoCrudRepository[User](converter)
      
As previously mentioned, the MongoDB database and collection name will be determined by the class name supplied to `AbstractMongoCrudRepository` via the generic parameter. In this case that parameter is the `User` class, therefore the derived database and collection names will be `users` and `user` respectively.
    
### Controller implementation

    @Component
    @Path("user")
    class UserRestController @Autowired()
    (userCrudRepository: AbstractPartialCrudRepository[User]
      with PartialUpdates[User] with Queryable[User])
      extends QueryableRestControllerWithPartialUpdates[User](userCrudRepository)

## Usage

Assuming the above implementations are in a web app, packaged up, deployed and running on `http://api.example.com:9000/user` for example ([`jersey-rest-service-archetype`](https://github.com/edinhodzic/jersey-rest-service-archetype) will automate this for you), then the below CRUD and query operations should be possible.

### Create a resource
    
    // TODO
    
### Read a resource
    
    // TODO
    
### Update a resource
    
    // TODO
    
### Delete a resource
    
    // TODO
    
### Query a resource
    
    // TODO

# What's next?

- write a REST service using this library
- dervie from the above service, a Maven arcehtype
- use the maven archetype to build micro services from scratch, based on this stack, within minutes