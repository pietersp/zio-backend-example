# Runtime And Persistence

## Purpose

Capture the current expectations for database schema management, runtime configuration, dependency wiring, and test coverage.

## Requirements

### Requirement: Schema changes are managed through Flyway

The system SHALL keep database schema evolution in versioned SQL migrations under `app/src/main/resources/db/migration`.

#### Scenario: Introducing a database schema change

- GIVEN a feature requires a new table, column, index, or constraint
- WHEN the change is implemented
- THEN a new Flyway migration is added under the migration directory
- AND the runtime startup path continues to apply migrations before serving requests

### Requirement: Application startup prepares database access before serving traffic

The system SHALL create the database datasource, assemble the transactor, and run Flyway migrations as part of application startup before the HTTP server begins serving routes.

#### Scenario: Starting the application in a local environment

- GIVEN the database is reachable
- WHEN the application starts
- THEN datasource and transactor layers are created
- AND pending Flyway migrations are applied
- AND the HTTP server serves the API routes and Swagger UI

### Requirement: Database access uses application-edge implementations

The system SHALL keep concrete repository implementations and database-specific wiring in the `app` module while preserving repository abstractions in `core`.

#### Scenario: Adding a new query or command

- GIVEN a change requires additional persistence behavior
- WHEN the implementation is planned
- THEN repository contracts are updated in `core`
- AND database-specific query logic is implemented in `app`

### Requirement: Runtime database configuration remains externally overridable

The system SHALL allow database connection settings to be supplied through environment variables or JVM system properties, while preserving local defaults that support development.

#### Scenario: Running in production or CI

- GIVEN a non-local environment provides its own database settings
- WHEN the application starts
- THEN those external values override the local defaults

### Requirement: Tests reflect the project's module responsibilities

The system SHALL use ZIO Test across modules, with `core` emphasizing business-logic tests and `app` emphasizing repository and Flyway integration tests.

#### Scenario: Verifying a change in business logic

- GIVEN a service behavior changes in `core`
- WHEN tests are added or updated
- THEN the change is covered by ZIO Test suites in `core/src/test`

#### Scenario: Verifying a change in persistence behavior

- GIVEN repository logic, migrations, or datasource behavior changes in `app`
- WHEN tests are added or updated
- THEN the change is covered by tests in `app/src/test`
- AND integration-oriented tests may use H2 or TestContainers as already established in the project
