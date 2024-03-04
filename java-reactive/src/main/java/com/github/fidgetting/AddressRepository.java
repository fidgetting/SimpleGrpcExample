package com.github.fidgetting;

import com.github.fidgetting.AddressOuterClass.Address;
import com.github.fidgetting.schema.tables.records.AddressRecord;
import io.r2dbc.pool.ConnectionPool;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Set;

import static com.github.fidgetting.schema.Tables.ADDRESS;
import static com.github.fidgetting.util.Jooq.parse;
import static com.github.fidgetting.util.Jooq.toJSONB;

public class AddressRepository {

  private static final Logger logger = LoggerFactory.getLogger(AddressRepository.class);

  private final DSLContext context;

  public AddressRepository(ConnectionPool connection) {
    this.context = DSL.using(connection, SQLDialect.POSTGRES, new Settings().withRenderSchema(false));
  }

  public Mono<Address> get(long id) {
    return Mono.from(
        context
            .selectFrom(ADDRESS)
            .where(ADDRESS.ID.eq(id))
    ).map(AddressRepository::fromDb).doOnNext(result -> logger.info("Address: {}", result));
  }

  public Flux<Address> get(Set<Long> ids) {
    return Flux.from(
        context
            .selectFrom(ADDRESS)
            .where(ADDRESS.ID.in(ids))
    ).map(AddressRepository::fromDb);
  }

  public Flux<Address> getAll() {
    return Flux.from(
        context
            .selectFrom(ADDRESS)
    ).map(AddressRepository::fromDb);
  }

  public Mono<Address> set(Address address) {
    return Mono.from(
        context
            .insertInto(ADDRESS)
            .set(toDb(address))
            .onDuplicateKeyUpdate()
            .setAllToExcluded()
            .returning()
    ).map(AddressRepository::fromDb);
  }

  public Mono<Address> remove(long id) {
    return Mono.from(
        context
            .deleteFrom(ADDRESS)
            .where(ADDRESS.ID.eq(id))
            .returning()
    ).map(AddressRepository::fromDb);
  }

  public static Address fromDb(AddressRecord record) {
    return parse(record.getAddress(), Address.newBuilder()).setId(record.getId()).build();
  }

  public static AddressRecord toDb(Address address) {
    return new AddressRecord(address.getId() != 0 ? address.getId() : null, toJSONB(address), null);
  }
}
