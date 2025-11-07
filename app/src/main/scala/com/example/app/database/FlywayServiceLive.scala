package com.example.app.database

import com.example.app.config.FlywayConfig
import com.example.domain.errors.FlywayException
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.MigrationInfoService
import zio.*
import javax.sql.DataSource

/**
 * Simplified Live implementation of FlywayService using Flyway's Java API.
 */
case class FlywayServiceLive(flyway: Flyway) extends FlywayService {

  override def migrate: IO[FlywayException, Int] = {
    ZIO.logInfo("Starting database migrations") *>
    ZIO.attemptBlocking(flyway.migrate())
      .mapBoth(
        ex => {
          ZIO.logError(s"Migration failed: ${ex.getMessage}")
          FlywayException.MigrationFailedException(ex)
        },
        result => {
          val migrationsApplied = result.migrationsExecuted
          ZIO.logInfo(s"Successfully applied $migrationsApplied migrations")
          migrationsApplied
        }
      )
  }

  override def info: IO[FlywayException, MigrationInfoService] = {
    ZIO.attemptBlocking(flyway.info())
      .mapError(FlywayException.DatabaseConnectionException.apply)
  }

  override def validate: IO[FlywayException, Boolean] = {
    ZIO.logDebug("Starting migration validation") *>
    ZIO.attemptBlocking(flyway.validateWithResult())
      .mapError(ex => {
        FlywayException.ValidationFailedException(List(ex.getMessage))
      })
      .map { result =>
        if (!result.validationSuccessful) {
          false
        } else {
          true
        }
      }
  }

  override def currentVersion: IO[FlywayException, Option[String]] = {
    info.map(info => Option(info.current()).map(_.getVersion.getVersion))
  }

  override def hasPendingMigrations: IO[FlywayException, Boolean] = {
    info.map(_.pending().nonEmpty)
  }

  override def clean: IO[FlywayException, Unit] = {
    ZIO.logWarning("Starting database clean operation") *>
    ZIO.attemptBlocking(flyway.clean())
      .mapError(FlywayException.MigrationFailedException.apply)
      .unit
  }

  override def baseline: IO[FlywayException, Unit] = {
    ZIO.logInfo("Starting database baseline") *>
    ZIO.attemptBlocking(flyway.baseline())
      .mapError(FlywayException.MigrationFailedException.apply)
      .unit
  }
}

object FlywayServiceLive {

  /**
   * Create a Flyway instance from configuration and DataSource.
   */
  private def createFlyway(config: FlywayConfig, dataSource: DataSource): Flyway = {
    Flyway.configure()
      .dataSource(dataSource)
      .locations(config.locations*)
      .baselineOnMigrate(config.baselineOnMigrate)
      .validateOnMigrate(config.validateOnMigrate)
      .cleanDisabled(config.cleanDisabled)
      .outOfOrder(config.outOfOrder)
      .load()
  }

  /**
   * ZIO layer that creates a FlywayServiceLive from a DataSource and FlywayConfig.
   */
  val layer: ZLayer[DataSource & FlywayConfig, Nothing, FlywayService] =
    ZLayer.fromFunction { (dataSource: DataSource, config: FlywayConfig) =>
      val flyway = createFlyway(config, dataSource)
      FlywayServiceLive(flyway)
    }

  /**
   * Convenience layer that only requires DataSource, using default configuration.
   */
  val defaultLayer: ZLayer[DataSource, Nothing, FlywayService] =
    ZLayer.fromFunction { (dataSource: DataSource) =>
      val defaultConfig = FlywayConfig.default
      val flyway = createFlyway(defaultConfig, dataSource)
      FlywayServiceLive(flyway)
    }
}