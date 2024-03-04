package com.github.fidgetting;

import com.github.fidgetting.schema.tables.records.AddressRecord;
import com.zaxxer.hikari.HikariDataSource;
import com.github.fidgetting.AddressOuterClass.Address;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;

import java.util.Set;
import java.util.stream.Stream;

import static com.github.fidgetting.schema.Tables.ADDRESS;
import static com.github.fidgetting.util.Jooq.parse;
import static com.github.fidgetting.util.Jooq.toJSONB;

public class AddressRepository {

  private final DSLContext context;

  public static AddressRepository create(HikariDataSource connection) {
    return new AddressRepository(DSL.using(connection, SQLDialect.POSTGRES, new Settings().withRenderSchema(false)));
  }

  public AddressRepository(DSLContext context) {
    this.context = context;
  }

  public Address get(long id) {
    AddressRecord record =
        context
            .selectFrom(ADDRESS)
            .where(ADDRESS.ID.eq(id))
            .fetchOne();
    return record != null ? fromDb(record) : null;
  }

  public Stream<Address> get(Set<Long> ids) {
    return context
        .selectFrom(ADDRESS)
        .where(ADDRESS.ID.in(ids))
        .fetchStream()
        .map(AddressRepository::fromDb);
  }

  public Stream<Address> getAll() {
    return context
        .selectFrom(ADDRESS)
        .fetchStream()
        .map(AddressRepository::fromDb);
  }

  public Address set(Address address) {
    AddressRecord record =
        context
            .insertInto(ADDRESS)
            .set(toDb(address))
            .onDuplicateKeyUpdate()
            .setAllToExcluded()
            .returning()
            .fetchOne();
    return record != null ? fromDb(record) : null;
  }

  public Address remove(long id) {
    AddressRecord record =
        context
            .deleteFrom(ADDRESS)
            .where(ADDRESS.ID.eq(id))
            .returning()
            .fetchOne();
    return record != null ? fromDb(record) : null;
  }

  private static Address fromDb(AddressRecord record) {
    return parse(record.getAddress(), Address.newBuilder())
        .setId(record.getId())
        .build();
  }

  private static AddressRecord toDb(Address address) {
    return new AddressRecord(
        address.getId() != 0 ? address.getId() : null,
        toJSONB(address),
        null
    );
  }
}
