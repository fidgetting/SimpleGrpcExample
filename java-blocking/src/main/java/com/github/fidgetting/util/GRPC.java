package com.github.fidgetting.util;

import io.grpc.BindableService;
import io.grpc.ServerBuilder;
import io.grpc.Status;

import static io.grpc.Status.INTERNAL;

public class GRPC {

  public static RuntimeException exception(Status status, String message) {
    return status.withDescription(message).asRuntimeException();
  }

  public static RuntimeException exception(Status status, String message, Exception cause) {
    return status.withDescription(message).withCause(cause).asRuntimeException();
  }

  public static Server server(int port, BindableService... services) {
    var builder = ServerBuilder.forPort(port);
    for (var service: services) {
      builder.addService(service);
    }
    return new Server(builder.build());
  }

  public static class Server implements AutoCloseable {

    private final io.grpc.Server server;

    public Server(io.grpc.Server server) {
      this.server = server;
    }

    public void run() {
      try {
        server.start();
        server.awaitTermination();
      } catch (Exception exception) {
        throw exception(INTERNAL, "Unable to start GRPC server", exception);
      }
    }

    @Override
    public void close() {
      server.shutdown();
    }
  }

}
