package com.github.fidgetting.scala

import com.github.fidgetting.scala.Grpc.exception
import com.github.fidgetting.scala.Logging.loggerOf
import com.github.fidgetting.scala.ReactorAddressServiceGrpc.AddressServiceImplBase
import com.github.fidgetting.scala.AddressServiceOuterClass.{GetAddressRequest, GetAddressResponse, RemoveAddressRequest, RemoveAddressResponse, SetAddressRequest, SetAddressResponse}
import com.google.protobuf.util.JsonFormat
import io.grpc.Status.{INVALID_ARGUMENT, NOT_FOUND}
import reactor.core.publisher.Mono

class AddressService(repository: AddressRepository) extends AddressServiceImplBase {
  val logger = loggerOf[AddressService]
  val printer = JsonFormat.printer().omittingInsignificantWhitespace()

  override def getAddress(request: GetAddressRequest): Mono[GetAddressResponse] = {
    logger.info(s"GetAddress: ${printer.print(request)}")
    for {
      addressId <- request.getId.toMono("Must provide Address ID in request")
      address <- repository.getAddress(addressId)
    } yield GetAddressResponse.newBuilder.setAddress(address).build()
  }.switchIfEmpty {
    Mono.error(NOT_FOUND.exception(s"No Address found for ${request.getId}"))
  }

  override def setAddress(request: SetAddressRequest): Mono[SetAddressResponse] = {
    logger.info(s"SetAddress: ${printer.print(request)}")
    repository.setAddress(request.getAddress).map { result =>
      SetAddressResponse.newBuilder.setAddress(result).build
    }
  }

  override def removeAddress(request: RemoveAddressRequest): Mono[RemoveAddressResponse] = {
    logger.info(s"GetAddress: ${printer.print(request)}")
    for {
      addressId <- request.getId.toMono("Must provide Address ID in request")
      address <- repository.removeAddress(addressId)
    } yield RemoveAddressResponse.newBuilder.setAddress(address).build()
  }.switchIfEmpty {
    Mono.just(RemoveAddressResponse.getDefaultInstance)
  }

  extension (l: Long) {
    def toMono(error: String): Mono[Long] =
      if (l != 0) Mono.just(l) else Mono.error(INVALID_ARGUMENT.exception(error))
  }

}
