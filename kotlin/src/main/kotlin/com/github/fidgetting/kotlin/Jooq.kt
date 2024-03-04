package com.github.fidgetting.kotlin

import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.jooq.Condition
import org.jooq.Field
import org.jooq.JSONB
import org.jooq.Publisher

object Jooq {

  val parser = JsonFormat.parser().ignoringUnknownFields()
  val printer = JsonFormat.printer().omittingInsignificantWhitespace()

  fun <T: Message.Builder> JSONB.toProto(builder: T): T =
    builder.also { parser.merge(this.data(), it) }

  fun Message.toJSONB(): JSONB =
    JSONB.valueOf(printer.print(this))

  val <T: Any> Publisher<T>.reactive get() =
    this as org.reactivestreams.Publisher<T>

  fun <T: Any> Publisher<T>.asFlow(): Flow<T> =
    reactive.asFlow()

  suspend fun <T: Any> Publisher<T>.awaitFirst(): T =
    reactive.awaitFirst()

  suspend fun <T: Any> Publisher<T>.awaitFirstOrNull(): T? =
    reactive.awaitFirstOrNull()

  infix fun <T> Collection<T>.contains(field: Field<T>): Condition =
    field.`in`(this)
}