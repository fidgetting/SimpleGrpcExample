package com.github.fidgetting.kotlin

import io.grpc.Status

object Grpc {

  fun Status.exception(message: String): Throwable =
    this.withDescription(message).asException()

  fun Status.exception(message: String, cause: Throwable): Throwable =
    this.withDescription(message).withCause(cause).asException()

}