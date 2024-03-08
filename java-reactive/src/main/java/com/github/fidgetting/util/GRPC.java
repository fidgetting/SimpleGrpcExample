package com.github.fidgetting.util;

import io.grpc.BindableService;
import io.grpc.ForwardingServerCall.SimpleForwardingServerCall;
import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerBuilder;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import static io.grpc.Status.INTERNAL;

public class GRPC {

  private static Logger logger = LoggerFactory.getLogger(GRPC.class);

  public static RuntimeException exception(Status status, String message) {
    return status.withDescription(message).asRuntimeException();
  }

  public static RuntimeException exception(Status status, String message, Exception cause) {
    return status.withDescription(message).withCause(cause).asRuntimeException();
  }

  public static <T> Mono<T> monoError(Status status, String message) {
    return Mono.error(exception(status, message));
  }

  public static <T> Mono<T> monoError(Status status, String message, Exception cause) {
    return Mono.error(exception(status, message, cause));
  }

  public static Server server(int port, BindableService... services) {
    var builder = ServerBuilder.forPort(port).intercept(new LoggingInterceptor());
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
        logger.info("Staring AddressService");
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

  public static class LoggingInterceptor implements ServerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
        ServerCall<ReqT, RespT> call,
        Metadata headers,
        ServerCallHandler<ReqT, RespT> next
    ) {
      final var listener = new SimpleForwardingServerCall<ReqT, RespT>(call) {
        @Override
        public void close(Status status, Metadata trailers) {
          if (status.getCode() != Status.Code.OK) {
            logger.error("Error calling " + call.getMethodDescriptor().getFullMethodName() + ": " + status);
          }
          super.close(status, trailers);
        }

        @Override
        public void sendMessage(RespT message) {
          if (filterReflection(call)) {
            logger.debug("Respond to " + call.getMethodDescriptor().getFullMethodName());
          }
          super.sendMessage(message);
        }
      };


      return new SimpleForwardingServerCallListener<ReqT>(next.startCall(listener, headers)) {
        @Override
        public void onMessage(ReqT message) {
          if (filterReflection(call)) {
            logger.debug("Received call to " + call.getMethodDescriptor().getFullMethodName());
          }
          super.onMessage(message);
        }
      };
    }

    private static <ReqT, RespT> boolean filterReflection(ServerCall<ReqT, RespT> call) {
      return !call.getMethodDescriptor().getServiceName().endsWith("ServerReflection") || logger.isTraceEnabled();
    }
  }
}
