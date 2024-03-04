package com.github.fidgetting.util

import org.jooq.impl.DSL.trueCondition
import org.jooq.{Condition, Field, JSONB, Record, SelectConditionStep, SelectWhereStep}
import org.reactivestreams.Publisher
import reactor.core.publisher.{Flux, Mono}
import scalapb.{GeneratedMessage, GeneratedMessageCompanion}
import scalapb.json4s.JsonFormat

import scala.jdk.CollectionConverters.SeqHasAsJava

object Jooq {

  lazy val parser = JsonFormat.parser.ignoringUnknownFields
  lazy val printer = JsonFormat.printer

  extension (message: GeneratedMessage) {
    def toJSONB: JSONB =
      JSONB.valueOf(printer.print(message))
  }

  extension (jsonb: JSONB) {
    def toProto[T <: GeneratedMessage](implicit gmc: GeneratedMessageCompanion[T]): T =
      parser.fromJsonString(jsonb.data())
  }

  extension[R <: Record] (step: SelectWhereStep[R]) {
    def whereOpt[T](opt: Option[T])(f: T => Condition): SelectConditionStep[R] =
      step.where(opt.map(f).getOrElse(trueCondition()))
  }

  extension[T] (field: Field[T]) {
    def in(values: Iterable[_]): Condition = field.in(values.toSeq.asJava)
  }

  extension[T] (publisher: Publisher[T]) {
    def mono: Mono[T] = Mono.from(publisher)
    def flux: Flux[T] = Flux.from(publisher)
  }

}
