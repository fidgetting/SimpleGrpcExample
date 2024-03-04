package com.github.fidgetting

import com.github.fidgetting.AddressServiceOuterClass.*
import com.github.fidgetting.address.Address
import com.github.fidgetting.address.Address.messageCompanion
import com.github.fidgetting.address_service.{
  GetAddressResponse as ScalaGetAddressResponse,
  GetAddressesResponse as ScalaGetAddressesResponse,
  RemoveAddressResponse as ScalaRemoveAddressResponse,
  SetAddressResponse as ScalaSetAddressResponse,
}
import com.github.fidgetting.address_service.GetAddressResponse.messageCompanion
import com.github.fidgetting.address_service.GetAddressesResponse.messageCompanion
import com.github.fidgetting.address_service.RemoveAddressResponse.messageCompanion
import com.github.fidgetting.address_service.SetAddressResponse.messageCompanion
import com.github.fidgetting.util.Grpc.{exception, toJava, toScala}
import com.github.fidgetting.util.Logging.loggerOf
import com.github.fidgetting.ReactorAddressServiceGrpc.AddressServiceImplBase
import com.google.protobuf.util.JsonFormat
import io.grpc.Status.{INVALID_ARGUMENT, NOT_FOUND}
import reactor.core.publisher.{Flux, Mono}

import scala.jdk.CollectionConverters.ListHasAsScala

class AddressService(repository: AddressRepository) extends AddressServiceImplBase {
  val logger = loggerOf[AddressService]
  val printer = JsonFormat.printer().omittingInsignificantWhitespace()

  override def getAddress(request: GetAddressRequest): Mono[GetAddressResponse] = {
    for {
      addressId <- request.getId.toMono("Must provide Address ID in request")
      address <- repository.get(addressId)
    } yield ScalaGetAddressResponse(address = address).toJava
  }.switchIfEmpty {
    Mono.error(NOT_FOUND.exception(s"No Address found for ${request.getId}"))
  }

  override def getAddresses(request: GetAddressesRequest): Flux[GetAddressesResponse] = {
    repository.get(request.getIdsList.asScala.map(_.toLong).toSet).map { address =>
      ScalaGetAddressesResponse(address = address).toJava
    }
  }

  override def setAddress(request: SetAddressRequest): Mono[SetAddressResponse] = {
    repository.set(request.getAddress.toScala).map { result =>
      ScalaSetAddressResponse(result).toJava
    }
  }

  override def removeAddress(request: RemoveAddressRequest): Mono[RemoveAddressResponse] = {
    for {
      addressId <- request.getId.toMono("Must provide Address ID in request")
      address <- repository.remove(addressId)
    } yield ScalaRemoveAddressResponse(address = address).toJava
  }.switchIfEmpty {
    Mono.just(RemoveAddressResponse.getDefaultInstance)
  }

  extension (l: Long) {
    def toMono(error: String): Mono[Long] =
      if (l != 0) Mono.just(l) else Mono.error(INVALID_ARGUMENT.exception(error))
  }

}
