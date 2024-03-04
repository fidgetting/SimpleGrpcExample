package com.github.fidgetting.util

import com.google.protobuf.Message
import io.grpc.Status
import scalapb.{GeneratedMessage, JavaProtoSupport}

object Grpc {

  extension (status: Status) {
    def exception(message: String): Throwable =
        status.withDescription(message).asException()

    def exception(message: String, cause: Throwable): Throwable =
        status.withDescription(message).withCause(cause).asException()
  }

  extension[T <: GeneratedMessage] (message: T) {
    def toJava[J <: Message](implicit jpc: JavaProtoSupport[T, J]): J =
      jpc.toJavaProto(message)
  }

  extension[T <: Message] (message: T) {
    def toScala[S <: GeneratedMessage](implicit jpc: JavaProtoSupport[S, T]): S =
      jpc.fromJavaProto(message)
  }

}
