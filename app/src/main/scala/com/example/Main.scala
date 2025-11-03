package com.example

import com.augustnagro.magnum.magzio.*
import com.example.api.Router
import com.example.app.database.{FlywayService, FlywayServiceLive}
import com.example.config.AppConfig
import com.example.repository.{
  DepartmentRepositoryLive,
  EmployeePhoneRepositoryLive,
  EmployeeRepositoryLive,
  PhoneRepositoryLive
}
import com.example.service.{
  DepartmentServiceLive,
  EmployeePhoneServiceLive,
  EmployeeServiceLive,
  PhoneServiceLive
}
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import zio.*
import zio.http.*

object Main extends ZIOAppDefault with Router {

  override val bootstrap =
    Runtime.removeDefaultLoggers

  /** Create a DataSource from application configuration
    */
  def createDataSource(
    config: com.example.config.DatabaseConfig
  ): ZIO[Any, Throwable, HikariDataSource] =
    ZIO.logInfo(s"Creating HikariDataSource for: ${config.url}") *>
      ZIO
        .attemptBlocking {
          val hikariConfig = new HikariConfig()
          hikariConfig.setJdbcUrl(config.url)
          hikariConfig.setUsername(config.user)
          hikariConfig.setPassword(config.password)
          hikariConfig.setMaximumPoolSize(10)
          hikariConfig.setMinimumIdle(2)
          hikariConfig.setConnectionTimeout(30000)
          hikariConfig.setIdleTimeout(600000)
          hikariConfig.setMaxLifetime(1800000)

          new HikariDataSource(hikariConfig)
        }
        .tap(_ => ZIO.logInfo("HikariDataSource created successfully"))

  /** DataSource layer using configuration from application.conf
    */
  def dataSourceLayer(
    config: com.example.config.DatabaseConfig
  ): ZLayer[Any, Throwable, HikariDataSource] =
    ZLayer.fromZIO(createDataSource(config))

  /** Run database migrations on startup using Flyway
    */
  val migrateDatabase: ZIO[FlywayService, Throwable, Unit] =
    for {
      hasPending <- FlywayService.hasPendingMigrations
      _ <- ZIO.logInfo(s"Has pending migrations: $hasPending")
      _ <-
        if (hasPending) {
          ZIO.logInfo("Applying pending database migrations...") *>
            FlywayService.migrate.unit <*
            ZIO.logInfo("Database migrations completed successfully")
        } else {
          ZIO.logInfo("No pending migrations to apply")
        }
    } yield ()

  /** Database layer that includes DataSource, Flyway service, and Transactor
    */
  def dbLayer(
    config: com.example.config.DatabaseConfig
  ): ZLayer[Any, Throwable, Transactor] = {
    val realDataSourceLayer = ZLayer.scoped {
      ZIO.fromAutoCloseable(createDataSource(config))
    }

    // Run migrations as a side effect
    val migrationSideEffect = ZLayer.fromZIO {
      for {
        _ <- ZIO.logInfo("Starting Flyway migration process")
        result <- FlywayService.migrate
          .provide(
            FlywayServiceLive.defaultLayer,
            realDataSourceLayer
          )
          .tapError(err => ZIO.logError(s"Migration failed: ${err.getMessage}"))
          .tap(_ => ZIO.logInfo("Flyway migrations completed successfully"))
        _ <- ZIO.logInfo(s"Migration result: $result")
      } yield result
    }.orDie

    // Combine all layers: migrations + DataSource -> Transactor
    (realDataSourceLayer ++ migrationSideEffect) >>> ZLayer
      .fromFunction((ds: HikariDataSource) =>
        Transactor.layer(ds: javax.sql.DataSource)
      )
      .flatten
  }

  val run = {
    for {
      config <- ZIO.service[AppConfig]
      _ <- ZIO.logInfo("Starting ZIO Backend application...")
      _ <- ZIO.logInfo(s"Database config: ${config.database}")
      _ <- Server.serve(routes ++ swaggerRoutes)
    } yield ()
  }.provide(
    Server.default,
    DepartmentServiceLive.layer,
    EmployeeServiceLive.layer,
    PhoneServiceLive.layer,
    EmployeePhoneServiceLive.layer,
    DepartmentRepositoryLive.layer,
    EmployeeRepositoryLive.layer,
    PhoneRepositoryLive.layer,
    EmployeePhoneRepositoryLive.layer,
    AppConfig.live,
    ZLayer.fromFunction((appConfig: AppConfig) => appConfig.database) >>>
      ZLayer
        .fromFunction((config: com.example.config.DatabaseConfig) =>
          dbLayer(config)
        )
        .flatten
  )
}
