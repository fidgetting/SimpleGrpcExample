import com.github.fidgetting.scala.{AddressRepository, AddressService}
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import io.grpc.{Server, ServerBuilder}
import io.grpc.protobuf.services.ProtoReflectionService

import scala.util.chaining.scalaUtilChainingOps
import scala.util.Using
import scala.util.Using.Releasable

object Main {
  def main(args: Array[String]): Unit = {
    Using.Manager { use =>
      val dbConn = use(
        HikariDataSource(
          HikariConfig().tap { config =>
            config.setDriverClassName("org.postgresql.Driver")
            config.setJdbcUrl("jdbc:postgresql://localhost:5433/postgres")
            config.setUsername("anorton")
            config.setPassword("anorton")
          }
        )
      )

      val repository = AddressRepository(dbConn)
      val server = use(
        ServerBuilder
          .forPort(Option(System.getenv("ADDRESS_PORT")).map(_.toInt).getOrElse(50051))
          .addService(AddressService(repository))
          .addService(ProtoReflectionService.newInstance())
          .build()
      )

      server.start()
      server.awaitTermination()
    }
  }

  given Releasable[Server] with {
    def release(resource: Server): Unit =
      resource.shutdown()
  }

}
