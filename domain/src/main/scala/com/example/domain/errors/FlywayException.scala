package com.example.domain.errors

/**
 * Sealed trait for all Flyway-related exceptions.
 * Provides typed error handling for migration operations.
 */
sealed trait FlywayException extends Throwable {
  def causeOption: Option[Throwable]
}

object FlywayException {

  /**
   * Exception thrown when migration execution fails.
   */
  case class MigrationFailedException(cause: Throwable) extends FlywayException {
    override def getMessage: String = s"Migration execution failed: ${cause.getMessage}"
    override def causeOption: Option[Throwable] = Some(cause)
    override def getCause: Throwable = cause
  }

  /**
   * Exception thrown when migration validation fails.
   */
  case class ValidationFailedException(errors: List[String]) extends FlywayException {
    override def getMessage: String = s"Migration validation failed: ${errors.mkString(", ")}"
    override def causeOption: Option[Throwable] = None
    override def getCause: Throwable = null
  }

  /**
   * Exception thrown when database connection fails during migration operations.
   */
  case class DatabaseConnectionException(cause: Throwable) extends FlywayException {
    override def getMessage: String = s"Database connection failed: ${cause.getMessage}"
    override def causeOption: Option[Throwable] = Some(cause)
    override def getCause: Throwable = cause
  }

  /**
   * Exception thrown when migration configuration is invalid.
   */
  case class ConfigurationException(message: String, cause: Option[Throwable] = None) extends FlywayException {
    override def getMessage: String = s"Configuration error: $message"
    override def causeOption: Option[Throwable] = cause
    override def getCause: Throwable = cause.orNull
  }

  /**
   * Exception thrown when migration checksum validation fails.
   */
  case class ChecksumMismatchException(
    migrationVersion: String,
    expectedChecksum: String,
    actualChecksum: String
  ) extends FlywayException {
    override def getMessage: String =
      s"Checksum mismatch for migration $migrationVersion: expected $expectedChecksum, got $actualChecksum"
    override def causeOption: Option[Throwable] = None
    override def getCause: Throwable = null
  }

  /**
   * Exception thrown when migration execution times out.
   */
  case class MigrationTimeoutException(
    migrationVersion: String,
    timeoutDuration: String
  ) extends FlywayException {
    override def getMessage: String =
      s"Migration $migrationVersion timed out after $timeoutDuration"
    override def causeOption: Option[Throwable] = None
    override def getCause: Throwable = null
  }

  /**
   * Exception thrown when attempting an operation that requires a clean database.
   */
  case class DatabaseNotCleanException(message: String) extends FlywayException {
    override def getMessage: String = s"Database is not clean: $message"
    override def causeOption: Option[Throwable] = None
    override def getCause: Throwable = null
  }

  /**
   * Exception thrown when migration scripts contain syntax errors.
   */
  case class MigrationSyntaxException(
    migrationVersion: String,
    lineNumber: Option[Int] = None,
    cause: Throwable
  ) extends FlywayException {
    override def getMessage: String = {
      val location = lineNumber.map(n => s" at line $n").getOrElse("")
      s"Syntax error in migration $migrationVersion$location: ${cause.getMessage}"
    }
    override def causeOption: Option[Throwable] = Some(cause)
    override def getCause: Throwable = cause
  }

  /**
   * Exception thrown when concurrent migration attempts are detected.
   */
  case class ConcurrentMigrationException(attemptedBy: String) extends FlywayException {
    override def getMessage: String =
      s"Concurrent migration detected. Migration already in progress by: $attemptedBy"
    override def causeOption: Option[Throwable] = None
    override def getCause: Throwable = null
  }

  /**
   * Exception thrown when migration execution is interrupted.
   */
  case class MigrationInterruptedException(cause: Throwable) extends FlywayException {
    override def getMessage: String = s"Migration execution interrupted: ${cause.getMessage}"
    override def causeOption: Option[Throwable] = Some(cause)
    override def getCause: Throwable = cause
  }
}