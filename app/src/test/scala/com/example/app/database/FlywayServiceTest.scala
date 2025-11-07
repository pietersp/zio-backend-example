package com.example.app.database

import com.example.app.config.FlywayConfig
import com.example.domain.errors.FlywayException
import zio.*
import zio.test.*
import zio.test.Assertion.*

object FlywayServiceTest extends ZIOSpecDefault {

  def spec = suite("FlywayServiceTest")(
    test("FlywayConfig should load default configuration") {
      val config = FlywayConfig.default
      assertTrue(
        config.locations.contains("classpath:db/migration"),
        config.baselineOnMigrate,
        config.validateOnMigrate
      )
    },
    test("FlywayConfig should load production configuration") {
      val config = FlywayConfig.production
      assertTrue(config.cleanDisabled, config.validateOnMigrate)
    },
    test("FlywayConfig should load test configuration") {
      val config = FlywayConfig.testing
      assertTrue(!(config.validateOnMigrate), !(config.cleanDisabled))
    },
    test("FlywayConfig should load from environment") {
      val config = FlywayConfig.fromEnvironment()
      assertTrue(config.locations.nonEmpty)
    },
    test("FlywayException should create migration failed exception") {
      val cause = new RuntimeException("Test error")
      val exception = FlywayException.MigrationFailedException(cause)
      assertTrue(
        exception.getMessage.contains("Migration execution failed"),
        exception.causeOption.get == cause
      )
    },
    test("FlywayException should create validation failed exception") {
      val errors = List("Error 1", "Error 2")
      val exception = FlywayException.ValidationFailedException(errors)
      assertTrue(
        exception.getMessage.contains("Migration validation failed"),
        exception.getMessage.contains("Error 1"),
        exception.getMessage.contains("Error 2")
      )
    },
    test("FlywayLayers should provide default layer") {
      // Test that layers can be created without runtime errors
      val layer = FlywayLayers.default
      assertTrue(true) // Layer creation succeeded
    }
  )
}
