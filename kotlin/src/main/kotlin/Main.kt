import com.github.fidgetting.kotlin.AddressRepository
import com.github.fidgetting.kotlin.AddressService
import com.github.fidgetting.kotlin.Using.using
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionService
import java.io.Closeable

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
    val server =
      ServerBuilder
        .forPort(System.getenv("ADDRESS_PORT")?.toInt() ?: 50051)
        .addService(AddressService(repository))
        .addService(ProtoReflectionService.newInstance())
        .build()

    manager.using(object: Closeable {
      override fun close() { server.shutdown() }
    })

    server.start()
    server.awaitTermination()
  }
}
