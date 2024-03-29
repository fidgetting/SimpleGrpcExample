package com.github.fidgetting

import com.github.fidgetting.kotlin.Grpc
import com.github.fidgetting.kotlin.Using.using
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService

fun main() {
  using { manager ->
    val dbConn = manager.using(
      HikariDataSource(
        HikariConfig().also { config ->
          config.driverClassName = "org.postgresql.Driver"
          config.jdbcUrl = "jdbc:postgresql://localhost:5433/postgres"
          config.username = "anorton"
          config.password = "anorton"
        }
      )
    )

    val repository = AddressRepository(dbConn)
    val server = manager.using(
      ServerBuilder.forPort(System.getenv("ADDRESS_PORT")?.toInt() ?: 50051)
        .intercept(Grpc.LoggingInterceptor())
        .addService(AddressService(repository))
        .addService(ProtoReflectionService.newInstance())
        .build()
    )

    server.start()
    server.awaitTermination()
  }
}
