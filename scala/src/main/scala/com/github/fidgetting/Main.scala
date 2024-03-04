package com.github.fidgetting

import com.github.fidgetting.{AddressRepository, AddressService}
import com.github.fidgetting.util.Logging
import com.github.fidgetting.util.R2DBC
import io.grpc.protobuf.services.ProtoReflectionService
import io.grpc.{Server, ServerBuilder}

import scala.util.Using
import scala.util.Using.Releasable

object Main {
  val logger = Logging.loggerOf[AddressService]

  def main(args: Array[String]): Unit = {
    Using.Manager { use =>
      val dbPool = R2DBC.createPool(
        host = "localhost",
        port = 5433,
        username = "anorton",
        password = "anorton"
      )

      val repository = AddressRepository(dbPool)
      val server = use(
        ServerBuilder
          .forPort(Option(System.getenv("ADDRESS_PORT")).map(_.toInt).getOrElse(50051))
          .addService(AddressService(repository))
          .addService(ProtoReflectionService.newInstance())
          .build()
      )

      logger.info("Starting AddressService")
      server.start()
      server.awaitTermination()
    }
  }

  given Releasable[Server] with {
    def release(resource: Server): Unit =
      resource.shutdown()
  }

}
