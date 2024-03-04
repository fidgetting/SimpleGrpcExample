# Kotlin Version

Very simple GRPC service implemented in Kotlin. This is simply a tech-demo for a simple, scalable, easy to understand
service architecture built on top of Kotlin and the JVM.

## Technolgies Used:

* [GRPC/Protobuf](https://grpc.io/docs/languages/kotlin/) - RPC/request IDL
* [gradle](https://gradle.org/) - Build System
* [Postgresql](https://www.postgresql.org/) - Database
* [jOOQ](https://www.jooq.org/) - SQL Query Builder
* [Flyway](https://flywaydb.org/) - SQL Database Migrations
* [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) - Concurrency
* (for testing) [Test Containers](https://testcontainers.com/)

## Running

Everything necessary to run this is built into the Gradle file. This will:
* Run the FLyway migrations on the Postgresql database
* Run the `jOOQ` code generation against the database
* Compile the service
* Run the service in the terminal

```shell
$ gradle run
```

Once running you can use `grpc-cli` or `grpcurl` to query the service
