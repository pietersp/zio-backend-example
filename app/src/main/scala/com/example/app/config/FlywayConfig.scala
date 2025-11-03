package com.example.app.config

import zio.*

/**
 * Configuration case class for Flyway database migrations.
 * Simplified version to avoid Config API complexities for demonstration.
 */
case class FlywayConfig(
  locations: List[String],
  baselineOnMigrate: Boolean,
  validateOnMigrate: Boolean,
  cleanDisabled: Boolean,
  outOfOrder: Boolean,
  targetVersion: Option[String],
  ignoreMigrationPatterns: List[String]
)

object FlywayConfig {

  /**
   * Default configuration for development environments.
   */
  val default: FlywayConfig = FlywayConfig(
    locations = List("classpath:db/migration"),
    baselineOnMigrate = true,
    validateOnMigrate = true,
    cleanDisabled = false,
    outOfOrder = false,
    targetVersion = None,
    ignoreMigrationPatterns = List.empty
  )

  /**
   * Configuration for production environments.
   */
  val production: FlywayConfig = FlywayConfig(
    locations = List("classpath:db/migration"),
    baselineOnMigrate = true,
    validateOnMigrate = true,
    cleanDisabled = true,
    outOfOrder = false,
    targetVersion = None,
    ignoreMigrationPatterns = List.empty
  )

  /**
   * Configuration for test environments.
   */
  val testing: FlywayConfig = FlywayConfig(
    locations = List("classpath:db/migration"),
    baselineOnMigrate = true,
    validateOnMigrate = false,
    cleanDisabled = false,
    outOfOrder = false,
    targetVersion = None,
    ignoreMigrationPatterns = List("*:placeholder")
  )

  /**
   * Load configuration based on environment system property.
   */
  def fromEnvironment(): FlywayConfig = {
    val environment = sys.props.get("app.env").orElse(sys.env.get("APP_ENV")).getOrElse("dev")
    environment.toLowerCase match {
      case "prod" | "production" => production
      case "test" | "testing" => testing
      case _ => default
    }
  }

  /**
   * ZIO layer that loads Flyway configuration from environment.
   */
  val layer: ZLayer[Any, Nothing, FlywayConfig] = {
    ZLayer.succeed(fromEnvironment())
  }

  /**
   * ZIO layer for specific environment.
   */
  def layerFor(environment: String): ZLayer[Any, Nothing, FlywayConfig] = {
    ZLayer.succeed(environment.toLowerCase match {
      case "prod" | "production" => production
      case "test" | "testing" => testing
      case _ => default
    })
  }
}