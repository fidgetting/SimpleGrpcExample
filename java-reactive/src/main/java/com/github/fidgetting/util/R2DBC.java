package com.github.fidgetting.util;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;

import java.time.Duration;

public class R2DBC {

  public static Pool createPool(String host, int port, String username, String password) {
    return new Pool(
        new ConnectionPool(
            ConnectionPoolConfiguration.builder(
                new PostgresqlConnectionFactory(PostgresqlConnectionConfiguration.builder()
                    .host(host)
                    .port(port)
                    .username(username)
                    .password(password)
                    .database("postgres")
                    .build()))
                .maxIdleTime(Duration.ofSeconds(1))
                .maxSize(10)
                .build()
        )
    );
  }

  public static class Pool implements AutoCloseable {

    public final ConnectionPool pool;

    public Pool(ConnectionPool pool) {
      this.pool = pool;
    }

    public void close() {
      pool.dispose();
    }
  }

}
