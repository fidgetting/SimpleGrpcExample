package com.github.fidgetting.kotlin

import com.github.fidgetting.kotlin.Grpc.exception
import com.github.fidgetting.kotlin.Logging.loggerOf
import io.grpc.Status.INVALID_ARGUMENT
import io.grpc.Status.NOT_FOUND
import io.grpc.Status.INTERNAL
import com.github.fidgetting.kotlin.AddressOuterClass.Address
import com.github.fidgetting.kotlin.AddressServiceGrpcKt.AddressServiceCoroutineImplBase
import com.github.fidgetting.kotlin.AddressServiceOuterClass.GetAddressRequest
import com.github.fidgetting.kotlin.AddressServiceOuterClass.GetAddressResponse
import com.github.fidgetting.kotlin.AddressServiceOuterClass.RemoveAddressRequest
import com.github.fidgetting.kotlin.AddressServiceOuterClass.RemoveAddressResponse
import com.github.fidgetting.kotlin.AddressServiceOuterClass.SetAddressRequest
import com.github.fidgetting.kotlin.AddressServiceOuterClass.SetAddressResponse
import com.github.fidgetting.kotlin.getAddressResponse
import com.github.fidgetting.kotlin.removeAddressResponse
import com.github.fidgetting.kotlin.setAddressResponse
import com.google.protobuf.util.JsonFormat

class AddressService(val repository: AddressRepository): AddressServiceCoroutineImplBase() {
  val logger = loggerOf<AddressService>()

  override suspend fun getAddress(request: GetAddressRequest): GetAddressResponse {
    if (request.id == 0L) {
      throw INVALID_ARGUMENT.exception("Must provide Address ID in request")
    }

    val result = repository.getAddress(request.id)
      ?: throw NOT_FOUND.exception("No Address found for ${request.id}")

    return getAddressResponse { address = result }
  }

  override suspend fun setAddress(request: SetAddressRequest): SetAddressResponse {
    return setAddressResponse {
      address = repository.setAddress(request.address)
        ?: throw INTERNAL.exception("Unable to set address: ${request.address}")
    }
  }

  override suspend fun removeAddress(request: RemoveAddressRequest): RemoveAddressResponse {
    if (request.id == 0L) {
      throw INVALID_ARGUMENT.exception("Must provide Address ID in request")
    }

    val result = repository.removeAddress(request.id)

    return removeAddressResponse { address = result ?: Address.getDefaultInstance() }
  }

}

fun example(parameter: Int?): Int {
  val result = parameter
    ?: return 0 
  
  return result * 10
}
