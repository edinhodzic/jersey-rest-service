# About

This is a work in process REST service abstraction. It assumes and operates on a set of conventions around CRUD and query operations.

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

    TODO explain how to use this shizzle