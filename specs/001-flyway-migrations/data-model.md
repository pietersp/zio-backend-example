# Data Model: Database Schema Management

**Date**: 2025-11-03 | **Branch**: 001-flyway-migrations

## Entity Definitions

### Migration

Represents a single database schema change with metadata.

```scala
// Domain entity
case class Migration(
  id: MigrationId,
  version: String,
  description: String,
  script: String,
  checksum: String,
  installedOn: Option[Instant],
  executionTime: Option[Duration],
  success: Boolean
)

// Refined types
type MigrationId = Long
type MigrationVersion = String Refined MatchesRegex["V\\d+__.*"]
type MigrationChecksum = String Refined Length(32, 32) // MD5 hash
```

**Fields**:
- `id`: Primary identifier from Flyway schema history
- `version`: Migration version number (e.g., "V1__Create_tables")
- `description`: Human-readable description of schema changes
- `script`: SQL script content
- `checksum`: MD5 hash for integrity verification
- `installedOn`: Timestamp when migration was applied
- `executionTime`: Time taken to execute migration
- `success`: Whether migration completed successfully

### SchemaHistory

Tracks all applied migrations with execution metadata.

```scala
case class SchemaHistory(
  installedRank: Int,
  version: String,
  description: String,
  `type`: MigrationType,
  script: String,
  checksum: Option[String],
  installedBy: String,
  installedOn: Instant,
  executionTime: Int,
  success: Boolean
)

sealed trait MigrationType
case object SQL extends MigrationType
case object BASELINE extends MigrationType
case object UNDO extends MigrationType
```

### DatabaseEnvironment

Represents different database contexts with specific migration requirements.

```scala
case class DatabaseEnvironment(
  name: EnvironmentName,
  jdbcUrl: String,
  username: String,
  migrationLocations: List[String],
  cleanDisabled: Boolean,
  validateOnMigrate: Boolean
)

type EnvironmentName = String Refined In(W("dev", "test", "prod"))
```

## Entity Relationships

```
DatabaseEnvironment (1) ──→ SchemaHistory (*) ──→ Migration (1)
```

- Each `DatabaseEnvironment` has one `SchemaHistory`
- Each `SchemaHistory` contains multiple `Migration` records
- Each `Migration` represents one schema change

## State Transitions

### Migration Lifecycle

```
Pending ──→ Running ──→ Success
    │           │           │
    └── Failed ←┘           └───→ Installed
```

**States**:
- **Pending**: Migration file discovered but not yet applied
- **Running**: Migration currently executing
- **Success**: Migration completed successfully
- **Failed**: Migration execution failed (requires manual intervention)
- **Installed**: Migration recorded in schema history

### Validation States

```
Unknown ──→ Validating ──→ Valid
    │            │           │
    └── Invalid ←┘           └───→ Current
```

## Validation Rules

### Migration Version
- Must follow pattern `V{number}__{description}` (e.g., `V1__Create_employee_tables`)
- Version numbers must be sequential and unique
- Descriptions must be snake_case and descriptive

### SQL Content
- Must be valid PostgreSQL syntax
- Should be idempotent where possible
- Must not modify `flyway_schema_history` table directly
- Should include comments explaining complex changes

### Checksum Validation
- MD5 checksum must match file content
- Checksum changes for applied migrations trigger validation failure
- Manual intervention required for checksum mismatches

## Integration with Existing Domain

### Employee Domain (Existing)

```scala
// Existing entities remain unchanged
case class Employee(id: EmployeeId, name: String, age: Int, departmentId: DepartmentId)
case class Department(id: DepartmentId, name: String)
case class Phone(id: PhoneId, number: String)
```

### Migration Support

```scala
// New migration-aware repository patterns
trait MigrationAwareRepository[Entity, ID] {
  def findById(id: ID): ZIO[Transactor, RepositoryError, Option[Entity]]
  def findAll: ZIO[Transactor, RepositoryError, List[Entity]]
  def create(entity: Entity): ZIO[Transactor, RepositoryError, Entity]
  def update(entity: Entity): ZIO[Transactor, RepositoryError, Entity]
  def delete(id: ID): ZIO[Transactor, RepositoryError, Unit]
}

// Existing repositories automatically benefit from managed schema
class EmployeeRepositoryLive extends MigrationAwareRepository[Employee, EmployeeId] {
  // Repository implementation relies on Flyway-managed schema
}
```

## Schema Evolution Strategy

### Backward Compatibility
- Additive changes preferred (new columns, new tables)
- Deprecation through new columns before removing old ones
- View-based compatibility layers for breaking changes

### Migration Ordering
- Dependencies between migrations must be explicitly managed
- Foreign key constraints applied after referenced tables created
- Index creation separated from table creation for large datasets

### Rollback Strategy
- Undo migrations for critical changes
- Baseline strategy for existing production databases
- Emergency procedures for failed deployments

## Performance Considerations

### Migration Execution
- Large table modifications split across multiple migrations
- Index creation during maintenance windows
- Batch processing for data migrations

### Runtime Performance
- Schema changes minimize impact on existing queries
- New indexes added before query optimization
- Statistics updated after structural changes

## Error Handling

### Migration Failures
- Automatic rollback of failed transactions
- Detailed error logging with context
- Manual resolution procedures documented

### Validation Errors
- Checksum mismatches require explicit override
- Schema drift detection and reporting
- Environment-specific validation rules