package com.github.fidgetting.scala

import com.google.protobuf.Message
import com.google.protobuf.Message.Builder
import com.google.protobuf.util.JsonFormat
import org.jooq.JSONB
import org.reactivestreams.Publisher
import reactor.core.publisher.{Flux, Mono}

object Jooq {

  lazy val parser = JsonFormat.parser().ignoringUnknownFields()
  lazy val printer = JsonFormat.printer().omittingInsignificantWhitespace()

  extension (message: Message) {
    def toJSONB: JSONB = JSONB.valueOf(printer.print(message))
  }

  extension (jsonb: JSONB) {
    def toProto[T <: Message.Builder](builder: T): T = {
      parser.merge(jsonb.data(), builder)
      builder
    }
  }

  extension[T] (publisher: Publisher[T]) {
    def mono: Mono[T] = Mono.from(publisher)
    def flux: Flux[T] = Flux.from(publisher)
  }

}
