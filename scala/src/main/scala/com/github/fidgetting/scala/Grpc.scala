package com.github.fidgetting.scala

import io.grpc.Status

object Grpc {

  extension (status: Status) {
    def exception(message: String): Throwable =
        status.withDescription(message).asException()

    def exception(message: String, cause: Throwable): Throwable =
        status.withDescription(message).withCause(cause).asException()
  }

}
