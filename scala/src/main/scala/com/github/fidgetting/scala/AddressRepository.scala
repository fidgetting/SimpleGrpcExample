package com.github.fidgetting.scala

import com.github.fidgetting.scala.AddressOuterClass.Address
import com.github.fidgetting.scala.Jooq.{mono, toJSONB, toProto}
import com.github.fidgetting.schema.tables.records.AddressRecord
import com.github.fidgetting.schema.tables.Address.ADDRESS
import com.zaxxer.hikari.HikariDataSource
import org.jooq.{Publisher, SQLDialect}
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import reactor.core.publisher.Mono

import scala.util.chaining.scalaUtilChainingOps

class AddressRepository(connection: HikariDataSource) {

  val context = DSL.using(connection, SQLDialect.POSTGRES, Settings().withRenderSchema(false))

  def getAddress(id: Long): Mono[Address] =
    context
      .selectFrom(ADDRESS)
      .where(ADDRESS.ID eq id)
      .mono
      .map(fromDb)

  def setAddress(address: Address): Mono[Address] =
    context
      .insertInto(ADDRESS)
      .set(toDb(address))
      .onDuplicateKeyUpdate()
      .setAllToExcluded()
      .returning()
      .mono
      .map(fromDb)

  def removeAddress(id: Long): Mono[Address] =
    context
      .deleteFrom(ADDRESS)
      .where(ADDRESS.ID eq id)
      .returning()
      .mono
      .map(fromDb)

  def toDb(address: Address): AddressRecord = AddressRecord().tap { record =>
    if (address.getId != 0) {
      record.setId(address.getId)
    }
    record.setAddress(address.toJSONB)
  }

  def fromDb(record: AddressRecord): Address =
    record.getAddress.toProto(Address.newBuilder)
      .setId(record.getId)
      .build()

}
