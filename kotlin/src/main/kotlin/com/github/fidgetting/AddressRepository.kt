package com.github.fidgetting

import com.github.fidgetting.kotlin.Grpc.exception
import com.github.fidgetting.kotlin.Jooq.awaitFirstOrNull
import com.github.fidgetting.kotlin.Jooq.toJSONB
import com.github.fidgetting.kotlin.Jooq.toProto
import com.github.fidgetting.kotlin.Jooq.contains
import com.github.fidgetting.AddressOuterClass.Address
import com.github.fidgetting.schema.tables.records.AddressRecord
import com.github.fidgetting.schema.tables.references.ADDRESS
import com.zaxxer.hikari.HikariDataSource
import io.grpc.Status.INTERNAL
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import org.jooq.SQLDialect
import org.jooq.conf.Settings
import org.jooq.impl.DSL

class AddressRepository(connection: HikariDataSource) {

  val context = DSL.using(connection, SQLDialect.POSTGRES, Settings().withRenderSchema(false))

  suspend fun get(id: Long): Address? =
    context
      .selectFrom(ADDRESS)
      .where(ADDRESS.ID.eq(id))
      .awaitFirstOrNull()
      ?.fromDb()

  fun get(ids: Set<Long>): Flow<Address> =
    context
      .selectFrom(ADDRESS)
      .where(ids contains ADDRESS.ID)
      .asFlow()
      .map { it.fromDb() }

  fun getAll(): Flow<Address> =
    context
      .selectFrom(ADDRESS)
      .asFlow()
      .map { it.fromDb() }

  suspend fun set(address: Address): Address? =
    context
      .insertInto(ADDRESS)
      .set(address.toDb())
      .onDuplicateKeyUpdate()
      .setAllToExcluded()
      .returning()
      .awaitFirstOrNull()
      ?.fromDb()

  suspend fun remove(id: Long): Address? =
    context
      .deleteFrom(ADDRESS)
      .where(ADDRESS.ID.eq(id))
      .returning()
      .awaitFirstOrNull()
      ?.fromDb()

  fun AddressRecord.fromDb(): Address =
    address
      .toProto(Address.newBuilder())
      .setId(id ?: throw INTERNAL.exception("Huh?"))
      .build()

  fun Address.toDb(): AddressRecord =
    AddressRecord(
      id = if (id != 0L) id else null,
      address = this.toJSONB()
    )

}