package com.example.app.database

import com.example.domain.errors.FlywayException
import org.flywaydb.core.api.MigrationInfoService
import zio._

/**
 * ZIO service for managing Flyway database migrations.
 * Provides a functional interface to Flyway operations with proper error handling.
 */
trait FlywayService {

  /**
   * Execute all pending database migrations.
   *
   * @return Number of migrations applied
   */
  def migrate: IO[FlywayException, Int]

  /**
   * Get information about all applied and pending migrations.
   *
   * @return MigrationInfoService with detailed migration status
   */
  def info: IO[FlywayException, MigrationInfoService]

  /**
   * Validate the database against applied migrations.
   * Checks for checksum mismatches and other validation issues.
   *
   * @return Boolean indicating if validation passed
   */
  def validate: IO[FlywayException, Boolean]

  /**
   * Get the current schema version.
   *
   * @return Current version as a string, or "null" if no migrations applied
   */
  def currentVersion: IO[FlywayException, Option[String]]

  /**
   * Check if there are any pending migrations.
   *
   * @return Boolean indicating if pending migrations exist
   */
  def hasPendingMigrations: IO[FlywayException, Boolean]

  /**
   * Clean the database (remove all objects).
   * WARNING: This is a destructive operation and should be used carefully.
   *
   * @return Unit
   */
  def clean: IO[FlywayException, Unit]

  /**
   * Baseline an existing database.
   * Marks the current schema as the baseline version for future migrations.
   *
   * @return Unit
   */
  def baseline: IO[FlywayException, Unit]
}

object FlywayService {
  // ZIO service accessor methods
  val migrate: ZIO[FlywayService, FlywayException, Int] =
    ZIO.serviceWithZIO[FlywayService](_.migrate)

  val info: ZIO[FlywayService, FlywayException, MigrationInfoService] =
    ZIO.serviceWithZIO[FlywayService](_.info)

  val validate: ZIO[FlywayService, FlywayException, Boolean] =
    ZIO.serviceWithZIO[FlywayService](_.validate)

  val currentVersion: ZIO[FlywayService, FlywayException, Option[String]] =
    ZIO.serviceWithZIO[FlywayService](_.currentVersion)

  val hasPendingMigrations: ZIO[FlywayService, FlywayException, Boolean] =
    ZIO.serviceWithZIO[FlywayService](_.hasPendingMigrations)

  val clean: ZIO[FlywayService, FlywayException, Unit] =
    ZIO.serviceWithZIO[FlywayService](_.clean)

  val baseline: ZIO[FlywayService, FlywayException, Unit] =
    ZIO.serviceWithZIO[FlywayService](_.baseline)
}