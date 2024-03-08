package com.github.fidgetting.kotlin

import io.grpc.*
import io.grpc.ForwardingServerCall.SimpleForwardingServerCall
import io.grpc.ForwardingServerCallListener.SimpleForwardingServerCallListener

object Grpc {

  fun Status.exception(message: String): Throwable =
    this.withDescription(message).asException()

  fun Status.exception(message: String, cause: Throwable): Throwable =
    this.withDescription(message).withCause(cause).asException()

  class LoggingInterceptor: ServerInterceptor {
    val logger = Logging.loggerOf<Grpc>()

    override fun <ReqT : Any?, RespT : Any?> interceptCall(
      call: ServerCall<ReqT, RespT>?,
      headers: Metadata?,
      next: ServerCallHandler<ReqT, RespT>?
    ): ServerCall.Listener<ReqT> {
      val listener = object: SimpleForwardingServerCall<ReqT, RespT>(call) {
        override fun close(status: Status?, trailers: Metadata?) {
          if (status?.code != Status.Code.OK) {
            logger.error("Error calling ${call.methodName}: $status")
          }
          super.close(status, trailers)
        }

        override fun sendMessage(message: RespT) {
          if (call.isNotReflection) {
            logger.debug("Response to ${call.methodName}")
          }
          super.sendMessage(message)
        }
      }`

      return object: SimpleForwardingServerCallListener<ReqT>(next?.startCall(listener, headers)) {
        override fun onMessage(message: ReqT) {
          if (call.isNotReflection) {
            logger.debug("Received call to ${call.methodName}")
          }
          super.onMessage(message)
        }
      }
    }

    val <ReqT, RespT> ServerCall<ReqT, RespT>?.isNotReflection get(): Boolean =
      !(this?.methodDescriptor?.serviceName?.endsWith("ServerReflection") ?: false) || logger.isTraceEnabled
    val <ReqT, RespT> ServerCall<ReqT, RespT>?.methodName get(): String =
      this?.methodDescriptor?.fullMethodName ?: "UNKNOWN_METHOD_NAME"
  }

}
