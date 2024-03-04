package com.github.fidgetting

import com.github.fidgetting.kotlin.Grpc.exception
import io.grpc.Status.INVALID_ARGUMENT
import io.grpc.Status.NOT_FOUND
import io.grpc.Status.INTERNAL
import com.github.fidgetting.AddressOuterClass.Address
import com.github.fidgetting.AddressServiceGrpcKt.AddressServiceCoroutineImplBase
import com.github.fidgetting.AddressServiceOuterClass.GetAddressRequest
import com.github.fidgetting.AddressServiceOuterClass.GetAddressResponse
import com.github.fidgetting.AddressServiceOuterClass.GetAddressesRequest
import com.github.fidgetting.AddressServiceOuterClass.GetAddressesResponse
import com.github.fidgetting.AddressServiceOuterClass.RemoveAddressRequest
import com.github.fidgetting.AddressServiceOuterClass.RemoveAddressResponse
import com.github.fidgetting.AddressServiceOuterClass.SetAddressRequest
import com.github.fidgetting.AddressServiceOuterClass.SetAddressResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AddressService(val repository: AddressRepository): AddressServiceCoroutineImplBase() {

  override suspend fun getAddress(request: GetAddressRequest): GetAddressResponse {
    if (request.id == 0L) {
      throw INVALID_ARGUMENT.exception("Must provide Address ID in request")
    }

    val result = repository.get(request.id)
      ?: throw NOT_FOUND.exception("No Address found for ${request.id}")

    return getAddressResponse {
      address = result
    }
  }

  override fun getAddresses(request: GetAddressesRequest): Flow<GetAddressesResponse> {
    val addresses: Flow<Address> =
      if (request.idsList.isEmpty()) repository.getAll()
      else repository.get(request.idsList.toSet())
    return addresses.map { addr -> getAddressesResponse { address = addr } }
  }

  override suspend fun setAddress(request: SetAddressRequest): SetAddressResponse {
    return setAddressResponse {
      address = repository.set(request.address)
        ?: throw INTERNAL.exception("Unable to set address: ${request.address}")
    }
  }

  override suspend fun removeAddress(request: RemoveAddressRequest): RemoveAddressResponse {
    if (request.id == 0L) {
      throw INVALID_ARGUMENT.exception("Must provide Address ID in request")
    }

    val result = repository.remove(request.id)

    return removeAddressResponse {
      address = result ?: Address.getDefaultInstance()
    }
  }

}
