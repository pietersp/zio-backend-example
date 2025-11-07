package com.example.config

import zio.*

// Database configuration
case class DatabaseConfig(
  url: String,
  user: String,
  password: String
)

// Application configuration
case class AppConfig(
  database: DatabaseConfig
)

object AppConfig {

  private def loadDatabaseConfig(): DatabaseConfig = {
    // Try environment variables first, then system properties, then defaults
    val url = sys.env.getOrElse("DATABASE_URL",
      sys.props.getOrElse("database.url", "jdbc:postgresql://localhost:5432/zio_backend"))
    val user = sys.env.getOrElse("DATABASE_USER", sys.props.getOrElse("database.user", "postgres"))
    val password = sys.env.getOrElse("DATABASE_PASSWORD", sys.props.getOrElse("database.password", "postgres"))

    DatabaseConfig(url, user, password)
  }

  // Load configuration from environment variables
  val live: ZLayer[Any, Throwable, AppConfig] = ZLayer.fromZIO {
    ZIO.attempt {
      val database = loadDatabaseConfig()
      AppConfig(database)
    }
  }
}