package com.github.fidgetting;

import com.github.fidgetting.AddressOuterClass.Address;
import com.github.fidgetting.AddressServiceOuterClass.GetAddressRequest;
import com.github.fidgetting.AddressServiceOuterClass.GetAddressResponse;
import com.github.fidgetting.AddressServiceOuterClass.GetAddressesRequest;
import com.github.fidgetting.AddressServiceOuterClass.GetAddressesResponse;
import com.github.fidgetting.AddressServiceOuterClass.RemoveAddressRequest;
import com.github.fidgetting.AddressServiceOuterClass.RemoveAddressResponse;
import com.github.fidgetting.AddressServiceOuterClass.SetAddressRequest;
import com.github.fidgetting.AddressServiceOuterClass.SetAddressResponse;
import com.github.fidgetting.ReactorAddressServiceGrpc.AddressServiceImplBase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashSet;

import static com.github.fidgetting.util.GRPC.exception;
import static com.github.fidgetting.util.GRPC.monoError;
import static io.grpc.Status.*;

public class AddressService extends AddressServiceImplBase {

  private final AddressRepository repository;

  public AddressService(AddressRepository repository) {
    this.repository = repository;
  }

  @Override
  public Mono<GetAddressResponse> getAddress(GetAddressRequest request) {
    if (request.getId() == 0) {
      return Mono.error(exception(INVALID_ARGUMENT, "Must provide Address ID in request"));
    }

    return repository
        .get(request.getId())
        .switchIfEmpty(monoError(NOT_FOUND, "No Address found for " + request.getId()))
        .map(address -> GetAddressResponse.newBuilder().setAddress(address).build());
  }

  @Override
  public Flux<GetAddressesResponse> getAddresses(GetAddressesRequest request) {
    var result =
        request.getIdsList().isEmpty() ?
            repository.getAll() :
            repository.get(new HashSet<>(request.getIdsList()));
    return  result.map(address -> GetAddressesResponse.newBuilder().setAddress(address).build());
  }

  @Override
  public Mono<SetAddressResponse> setAddress(SetAddressRequest request) {
    return repository
        .set(request.getAddress())
        .switchIfEmpty(monoError(INTERNAL, "Unable to set address " + request.getAddress()))
        .map(address -> SetAddressResponse.newBuilder().setAddress(address).build());
  }

  @Override
  public Mono<RemoveAddressResponse> removeAddress(RemoveAddressRequest request) {
    if (request.getId() == 0) {
      return monoError(INVALID_ARGUMENT, "Must provide Address ID in request");
    }

    return repository
        .remove(request.getId())
        .switchIfEmpty(Mono.just(Address.getDefaultInstance()))
        .map(address -> RemoveAddressResponse.newBuilder().setAddress(address).build());
  }

}
