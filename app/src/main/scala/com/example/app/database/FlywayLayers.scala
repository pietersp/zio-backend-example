package com.example.app.database

import com.example.app.config.FlywayConfig
import zio.*
import javax.sql.DataSource

/**
 * Simplified ZIO layers for Flyway service configuration and setup.
 */
object FlywayLayers {

  /**
   * Layer that provides FlywayService with configuration from environment.
   */
  val live: ZLayer[DataSource, Nothing, FlywayService] =
    FlywayConfig.layer >>> FlywayServiceLive.layer

  /**
   * Layer that provides FlywayService with default configuration.
   */
  val default: ZLayer[DataSource, Nothing, FlywayService] =
    FlywayServiceLive.defaultLayer

  /**
   * Layer that provides FlywayService for specific environment.
   */
  def forEnvironment(environment: String): ZLayer[DataSource, Nothing, FlywayService] =
    FlywayConfig.layerFor(environment) >>> FlywayServiceLive.layer

  /**
   * Layer that provides FlywayService with custom configuration.
   */
  def withConfig(config: FlywayConfig): ZLayer[DataSource, Nothing, FlywayService] =
    ZLayer.succeed(config) >>> FlywayServiceLive.layer

  /**
   * Layer that provides FlywayService with test configuration.
   */
  val testing: ZLayer[DataSource, Nothing, FlywayService] =
    ZLayer.succeed(FlywayConfig.testing) >>> FlywayServiceLive.layer

  /**
   * Layer that provides FlywayService with production configuration.
   */
  val production: ZLayer[DataSource, Nothing, FlywayService] =
    ZLayer.succeed(FlywayConfig.production) >>> FlywayServiceLive.layer

  /**
   * Helper to create a layer with environment-specific configuration.
   */
  val environmentAware: ZLayer[DataSource, Nothing, FlywayService] = {
    val environment = sys.props.get("app.env").orElse(sys.env.get("APP_ENV")).getOrElse("dev")
    forEnvironment(environment)
  }
}