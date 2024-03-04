package com.github.fidgetting;

import com.github.fidgetting.util.GRPC;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.grpc.protobuf.services.ProtoReflectionService;

public class Main {

  public static void main(String[] args) {
    var config = new HikariConfig();
    config.setDriverClassName("org.postgresql.Driver");
    config.setJdbcUrl("jdbc:postgresql://localhost:5433/postgres");
    config.setUsername("anorton");
    config.setPassword("anorton");

    try (
        var dbConn = new HikariDataSource(config);
        var server = GRPC.server(
            getPort(),
            AddressService.create(AddressRepository.create(dbConn)),
            ProtoReflectionService.newInstance()
        )
    ) {
      server.run();
    }
  }

  public static int getPort() {
    var port = System.getenv("ADDRESS_PORT");
    return port == null ? 50051 : Integer.parseInt(port);
  }

}
