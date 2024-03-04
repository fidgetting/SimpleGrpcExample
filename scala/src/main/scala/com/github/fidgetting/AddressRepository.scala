package com.github.fidgetting

import com.github.fidgetting.address.Address
import com.github.fidgetting.address.Address.messageCompanion
import com.github.fidgetting.util.Jooq.{flux, in, mono, toJSONB, toProto, whereOpt}
import com.github.fidgetting.schema.tables.Address.ADDRESS
import com.github.fidgetting.schema.tables.records.AddressRecord
import io.r2dbc.pool.ConnectionPool
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import org.jooq.{Publisher, SQLDialect}
import reactor.core.publisher.{Flux, Mono}

import scala.util.chaining.scalaUtilChainingOps

class AddressRepository(connection: ConnectionPool) {

  val context = DSL.using(connection, SQLDialect.POSTGRES, Settings().withRenderSchema(false))

  def get(id: Long): Mono[Address] =
    context
      .selectFrom(ADDRESS)
      .where(ADDRESS.ID eq id)
      .mono
      .map(fromDb)

  def get(ids: Set[Long]): Flux[Address] =
    context
      .selectFrom(ADDRESS)
      .whereOpt(if (ids.nonEmpty) Some(ids) else None) { ADDRESS.ID in _ }
      .flux
      .map(fromDb)

  def set(address: Address): Mono[Address] =
    context
      .insertInto(ADDRESS)
      .set(toDb(address))
      .onDuplicateKeyUpdate()
      .setAllToExcluded()
      .returning()
      .mono
      .map(fromDb)

  def remove(id: Long): Mono[Address] =
    context
      .deleteFrom(ADDRESS)
      .where(ADDRESS.ID eq id)
      .returning()
      .mono
      .map(fromDb)

  def toDb(address: Address): AddressRecord = AddressRecord().tap { record =>
    if (address.id != 0) {
      record.setId(address.id)
    }
    record.setAddress(address.toJSONB)
  }

  def fromDb(record: AddressRecord): Address =
    record.getAddress.toProto.copy(id = record.getId)

}
