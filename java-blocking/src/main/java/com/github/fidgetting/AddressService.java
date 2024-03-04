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
import com.github.fidgetting.AddressServiceGrpc.AddressServiceImplBase;
import io.grpc.stub.StreamObserver;

import java.util.HashSet;

import static com.github.fidgetting.util.GRPC.exception;
import static io.grpc.Status.INVALID_ARGUMENT;
import static io.grpc.Status.INTERNAL;
import static io.grpc.Status.NOT_FOUND;

public class AddressService extends AddressServiceImplBase {

  private final AddressRepository repository;

  public static AddressService create(AddressRepository repository) {
    return new AddressService(repository);
  }

  public AddressService(AddressRepository repository) {
    super();
    this.repository = repository;
  }

  @Override
  public void getAddress(GetAddressRequest request, StreamObserver<GetAddressResponse> observer) {
    if (request.getId() == 0) {
      observer.onError(exception(INVALID_ARGUMENT, "Must provide Address ID in request"));
      return;
    }

    var result = repository.get(request.getId());
    if (result == null) {
      observer.onError(exception(NOT_FOUND, "No Address found for " + request.getId()));
      return;
    }

    observer.onNext(GetAddressResponse.newBuilder().setAddress(result).build());
    observer.onCompleted();
  }

  @Override
  public void getAddresses(GetAddressesRequest request, StreamObserver<GetAddressesResponse> observer) {
    var stream =
        request.getIdsList().isEmpty() ?
            repository.getAll() :
            repository.get(new HashSet<>(request.getIdsList()));
    stream.forEach((address) -> observer.onNext(GetAddressesResponse.newBuilder().setAddress(address).build()));
    observer.onCompleted();
  }

  @Override
  public void setAddress(SetAddressRequest request, StreamObserver<SetAddressResponse> observer) {
    var result = repository.set(request.getAddress());
    if (result == null) {
      observer.onError(exception(INTERNAL, "Unable to set address " + request.getAddress()));
      return;
    }

    observer.onNext(SetAddressResponse.newBuilder().setAddress(result).build());
    observer.onCompleted();
  }

  @Override
  public void removeAddress(RemoveAddressRequest request, StreamObserver<RemoveAddressResponse> observer) {
    if (request.getId() == 0) {
      observer.onError(exception(INVALID_ARGUMENT, "Must provide Address ID in request"));
      return;
    }

    var result = repository.remove(request.getId());
    observer.onNext(
        RemoveAddressResponse.newBuilder()
            .setAddress(result != null ? result : Address.getDefaultInstance())
            .build()
    );
    observer.onCompleted();
  }

}
