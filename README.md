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
```scala
case class User(@BeanProperty var data: String) extends Identifiable {
  def this() = this(null)
}
```
### Converter implementation
```scala
@Component
class UserConverter extends Converter[User, DBObject] {

  override def serialise(user: User): DBObject =
    MongoDBObject.newBuilder
      .+=("data" -> user.data)
      .result()

  override def deserialise(dbObject: DBObject): User =
    User(dbObject.get("data").asInstanceOf[String])

}
```
### Repository implementation
```scala
@Component
class UserCrudRepository @Autowired()
(converter: Converter[User, DBObject])
  extends AbstractMongoCrudRepository[User](converter)
```
As previously mentioned, the MongoDB database and collection name will be determined by the class name supplied to `AbstractMongoCrudRepository` via the generic parameter. In this case that parameter is the `User` class, therefore the derived database and collection names will be `users` and `user` respectively.
    
### Controller implementation
```scala
@Component
@Path("user")
class UserRestController @Autowired()
(userCrudRepository: AbstractPartialCrudRepository[User]
  with PartialUpdates[User] with Queryable[User])
  extends QueryableRestControllerWithPartialUpdates[User](userCrudRepository)
```
## Usage

Assuming the above implementations are in a web app, packaged up, deployed and running on `http://api.example.com:9000/user` for example ([`jersey-rest-service-archetype`](https://github.com/edinhodzic/jersey-rest-service-archetype) will automate this for you), then the below CRUD and query operations should be possible.

### Create a resource
    
    curl -iL -X POST http://localhost:9000/user \
    > -H content-type:application/json \
    > -d '{ "data" : "some user data" }' \
    > -u user:m0nkey
    HTTP/1.1 201 Created
    Server: Apache-Coyote/1.1
    Strict-Transport-Security: max-age=31536000 ; includeSubDomains
    X-Application-Context: Dods User REST Service:9000
    Location: http://localhost:9000/user/5662b08fd4c686edee605ef8
    Content-Type: application/json;charset=UTF-8
    Content-Length: 57
    Date: Sat, 05 Dec 2015 09:38:23 GMT

    {"data":"some user data","id":"5662b08fd4c686edee605ef8"}
    
### Read a resource
    
    curl -iL http://localhost:9000/user/5662b08fd4c686edee605ef8 -u user:m0nkey
    HTTP/1.1 200 OK
    Server: Apache-Coyote/1.1
    Strict-Transport-Security: max-age=31536000 ; includeSubDomains
    X-Application-Context: Dods User REST Service:9000
    Content-Type: application/json;charset=UTF-8
    Content-Length: 35
    Date: Sat, 05 Dec 2015 09:38:57 GMT

    {"data":"some user data","id":null}
    
### Update a resource
    
    curl -iL -X PUT http://localhost:9000/user/5662b08fd4c686edee605ef8 \
    > -H content-type:application/json \
    > -d '{$set : {"data" : "update existing field"}}' \
    > -u user:m0nkey
    HTTP/1.1 204 No Content
    Server: Apache-Coyote/1.1
    Strict-Transport-Security: max-age=31536000 ; includeSubDomains
    X-Application-Context: Dods User REST Service:9000
    Date: Sat, 05 Dec 2015 09:39:38 GMT

- noteworthy is that the above can be a partial update; not so obvious with this example where the domain object has a single field

### Delete a resource
    
    curl -iL -X DELETE http://localhost:9000/user/5662b08fd4c686edee605ef8 -u user:m0nkey
    HTTP/1.1 204 No Content
    Server: Apache-Coyote/1.1
    Strict-Transport-Security: max-age=31536000 ; includeSubDomains
    X-Application-Context: Dods User REST Service:9000
    Date: Sat, 05 Dec 2015 09:40:28 GMT
    
### Query a resource
    
    curl -v -X POST http://localhost:9000/user/query \
    > -H content-type:application/json \
    > -d '{ "data" : { "$regex" : "goes here" } }' \
    > -u user:m0nkey| python -m json.tool
      % Total    % Received % Xferd  Average Speed   Time    Time     Time  Current
                                     Dload  Upload   Total   Spent    Left  Speed
      0     0    0     0    0     0      0      0 --:--:-- --:--:-- --:--:--     0*   Trying ::1...
    * Connected to localhost (::1) port 9000 (#0)
    * Server auth using Basic with user 'user'
    > POST /user/query HTTP/1.1
    > Host: localhost:9000
    > Authorization: Basic dXNlcjptMG5rZXk=
    > User-Agent: curl/7.43.0
    > Accept: */*
    > content-type:application/json
    > Content-Length: 39
    > 
    } [39 bytes data]
    * upload completely sent off: 39 out of 39 bytes
    < HTTP/1.1 200 OK
    < Server: Apache-Coyote/1.1
    < Strict-Transport-Security: max-age=31536000 ; includeSubDomains
    < X-Application-Context: Dods User REST Service:9000
    < Content-Type: application/json;charset=UTF-8
    < Content-Length: 94
    < Date: Sat, 05 Dec 2015 09:42:26 GMT
    < 
    { [94 bytes data]
    100   133  100    94  100    39   3087   1281 --:--:-- --:--:-- --:--:--  3133
    * Connection #0 to host localhost left intact
    {
        "page": 2,
        "pageTotal": 1,
        "results": [
            {
                "data": "user data goes here",
                "id": null
            }
        ],
        "resultsTotal": 1
    }

# Abstractions

![REST service abstractions](https://cloud.githubusercontent.com/assets/4981314/11613130/c727cace-9c0d-11e5-913f-189dc08fe2c1.jpg)

# What's next?

- write a REST service using this library - done, see [somecompany-user](https://github.com/edinhodzic/somecompany-user)
- derive from the above service, a Maven archetype - in progress, see [jersey-rest-service-archetype](https://github.com/edinhodzic/jersey-rest-service-archetype)
- use the maven archetype to build micro services from scratch, based on this stack, within minutes

## Incomplete features

- querying is currently implemented but we may need to take it further and make use of [Mongo's snapshots](https://docs.mongodb.org/manual/reference/operator/meta/snapshot/)
- pagination is partially implemented in the sense that a query response contains pagination but the initial request does not

## Future development ideas

- Cross-cutting concerns
    - security
    - validation
    - caching
    - logging
    - audit events?
- at the moment the service assumes a local MongoDB instance, need for this to be configurable via a MongoDB database string property e.g. `mongo.url=...`
- might be worth investigating whether it would have been easier to leverage Spring Boot's support for MondoDB via Spring Data; see [http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-mongodb](http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#boot-features-mongodb)
- service url versioning
- integrate HATEOAS links (Spring Boot may already have support for it?)
- swap HTTP basic authentication for [OAuth](http://oauth.net/2/)
- upcoming in Spring Boot is [CORS support](http://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/htmlsingle/#boot-features-cors)
- integrate [Swagger](http://swagger.io/), Spring Boot may already have support for it
