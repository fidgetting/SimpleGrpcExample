package com.github.fidgetting.util

import io.r2dbc.pool.{ConnectionPool, ConnectionPoolConfiguration}
import io.r2dbc.postgresql.{PostgresqlConnectionConfiguration, PostgresqlConnectionFactory}

import scala.concurrent.duration.{DurationInt, FiniteDuration}
import scala.jdk.javaapi.DurationConverters.toJava
import scala.util.Using.Releasable

object R2DBC {

  def createPool(
    host: String,
    port: Int,
    username: String,
    password: String,
    database: String = "postgres",
    idleTimeout: FiniteDuration = 1.second,
    maxSize: Int = 10,
  ): ConnectionPool = {
    ConnectionPool(
      ConnectionPoolConfiguration.builder(
          PostgresqlConnectionFactory(PostgresqlConnectionConfiguration.builder
            .host(host)
            .port(port)
            .username(username)
            .password(password)
            .database(database)
            .build()))
        .maxIdleTime(toJava(idleTimeout))
        .maxSize(maxSize)
        .build()
    )
  }

  given Releasable[ConnectionPool] with {
    def release(resource: ConnectionPool): Unit =
      resource.dispose()
  }

}
