## zio-backend-example

This is a demo for a web service using 
- ZIO Http (Http server)
- Iron (Refined types)
- Magnum (Postgres access)

It has been adapted from 
[this blog post](https://www.ziverge.com/post/how-to-implement-a-rest-api-in-scala-3-with-zio-http-magnum-and-iron)
and has been customized to split out the concepts into different projects.

The `domain` project is solely for containing the entities that the project deals with. All other projects should depend 
on it, but it should not depend on any other project and have minimal dependencies (except `zio-http` required for 
endpoint codecs that make use of schemas, and `iron` for refined types of our domain entities)

The `endpoints` contain only the `zio-http` endpoints. These are to be implemented in `core`. The idea is that this 
could be distributed independently so other services can make use of it to generate clients to call this service with.

The `core` project contains the business logic and the interfaces for the repos that we need to use. The implementation 
of these repos currently live in `app` (this might change). The idea is for core to contain only the business logic,
independent of implementation of "how" data is accessed. It should only have a dependency on `domain`

The `app` project is the main app that starts a `zio-http` server and contains handlers
that are responsible for implementing our `zio-http` endpoints, the implementation to access the database (this might 
change) and the main entrypoint to start the program 

