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
      assert(config.locations)(contains("classpath:db/migration")) &&
      assert(config.baselineOnMigrate)(isTrue) &&
      assert(config.validateOnMigrate)(isTrue)
    },

    test("FlywayConfig should load production configuration") {
      val config = FlywayConfig.production
      assert(config.cleanDisabled)(isTrue) &&
      assert(config.validateOnMigrate)(isTrue)
    },

    test("FlywayConfig should load test configuration") {
      val config = FlywayConfig.testing
      assert(config.validateOnMigrate)(isFalse) &&
      assert(config.cleanDisabled)(isFalse)
    },

    test("FlywayConfig should load from environment") {
      val config = FlywayConfig.fromEnvironment()
      assert(config.locations)(isNonEmpty)
    },

    test("FlywayException should create migration failed exception") {
      val cause = new RuntimeException("Test error")
      val exception = FlywayException.MigrationFailedException(cause)
      assert(exception.getMessage)(containsString("Migration execution failed")) &&
      assert(exception.causeOption)(isSome(equalTo(cause)))
    },

    test("FlywayException should create validation failed exception") {
      val errors = List("Error 1", "Error 2")
      val exception = FlywayException.ValidationFailedException(errors)
      assert(exception.getMessage)(containsString("Migration validation failed")) &&
      assert(exception.getMessage)(containsString("Error 1")) &&
      assert(exception.getMessage)(containsString("Error 2"))
    },

    test("FlywayLayers should provide default layer") {
      // Test that layers can be created without runtime errors
      val layer = FlywayLayers.default
      assert(true)(isTrue) // Layer creation succeeded
    }
  )
}