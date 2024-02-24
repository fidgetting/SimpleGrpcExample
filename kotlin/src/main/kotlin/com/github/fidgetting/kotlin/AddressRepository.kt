package com.github.fidgetting.kotlin

import com.github.fidgetting.kotlin.Grpc.exception
import com.github.fidgetting.kotlin.Jooq.awaitFirstOrNull
import com.github.fidgetting.kotlin.Jooq.toJSONB
import com.github.fidgetting.kotlin.Jooq.toProto
import com.github.fidgetting.kotlin.AddressOuterClass.Address
import com.github.fidgetting.schema.tables.records.AddressRecord
import com.github.fidgetting.schema.tables.references.ADDRESS
import com.zaxxer.hikari.HikariDataSource
import io.grpc.Status.INTERNAL
import org.jooq.SQLDialect
import org.jooq.conf.Settings
import org.jooq.impl.DSL

class AddressRepository(connection: HikariDataSource) {

  val context = DSL.using(connection, SQLDialect.POSTGRES, Settings().withRenderSchema(false))

  suspend fun getAddress(id: Long): Address? =
    context
      .selectFrom(ADDRESS)
      .where(ADDRESS.ID.eq(id))
      .awaitFirstOrNull()
      ?.fromDb()

  suspend fun setAddress(address: Address): Address? =
    context
      .insertInto(ADDRESS)
      .set(address.toDb())
      .onDuplicateKeyUpdate()
      .setAllToExcluded()
      .returning()
      .awaitFirstOrNull()
      ?.fromDb()

  suspend fun removeAddress(id: Long): Address? =
    context
      .deleteFrom(ADDRESS)
      .where(ADDRESS.ID.eq(id))
      .returning()
      .awaitFirstOrNull()
      ?.fromDb()

  fun AddressRecord.fromDb(): Address =
    address.toProto<Address>(Address.newBuilder()).copy {
      id = this@fromDb.id ?: throw INTERNAL.exception("Huh?")
    }

  fun Address.toDb(): AddressRecord =
    AddressRecord(
      id = if (id != 0L) id else null,
      address = this.toJSONB()
    )

}