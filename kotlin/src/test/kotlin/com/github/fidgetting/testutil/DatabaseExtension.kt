package com.github.fidgetting.testutil

import com.github.fidgetting.kotlin.Logging.loggerOf
import com.github.fidgetting.kotlin.Using.using
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ParameterContext
import org.junit.jupiter.api.extension.ParameterResolver
import org.junit.platform.commons.util.ReflectionUtils
import org.testcontainers.containers.PostgreSQLContainer

@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class DatabaseRepository

class DatabaseExtension: BeforeAllCallback, AfterAllCallback, AfterEachCallback, ParameterResolver {
  val log = loggerOf<DatabaseExtension>()

  private val postgresql = PostgreSQLContainer("postgres:14.10")
  private val connection: HikariDataSource by lazy {
    HikariDataSource(
      HikariConfig().also { config ->
        config.jdbcUrl = postgresql.jdbcUrl
        config.driverClassName = postgresql.driverClassName
        config.username = postgresql.username
        config.password = postgresql.password
      }
    )
  }

  override fun beforeAll(context: ExtensionContext) {
    postgresql.withUsername("junit-test-username")
    postgresql.withPassword("junit-test-password")
    postgresql.start()
    Flyway.configure().dataSource(connection).load().migrate()
  }

  override fun afterAll(context: ExtensionContext) {
    connection.close()
    postgresql.stop()
  }

  override fun afterEach(context: ExtensionContext?) =using { manager ->
    val conn = manager.using(connection.connection)
    val result = conn.createStatement().executeQuery(
      """
        SELECT table_name
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name != 'flyway_schema_history'
      """.trimIndent()
    )
    log.debug("Truncate:")
    while (result.next()) {
      log.debug("  ${result.getString(1)}")
      conn.createStatement().execute("TRUNCATE TABLE ${result.getString(1)} CASCADE")
    }
  }

  override fun supportsParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Boolean =
    parameterContext.isAnnotated(DatabaseRepository::class.java)

  override fun resolveParameter(parameterContext: ParameterContext, extensionContext: ExtensionContext): Any {
    return ReflectionUtils.newInstance(parameterContext.parameter.type, connection)
  }

}