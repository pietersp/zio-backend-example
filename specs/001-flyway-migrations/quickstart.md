# Quickstart Guide: Database Schema Management with Flyway

**Branch**: 001-flyway-migrations | **Date**: 2025-11-03

## Overview

This guide walks you through setting up and using Flyway database migrations in the ZIO backend application. Flyway automates database schema management, replacing manual table creation with version-controlled migrations.

## Prerequisites

- PostgreSQL 14+ installed and running
- SBT 1.9+ for building the application
- Java 17+ for runtime

## Quick Setup

### 1. Add Dependencies

Add to `app/build.sbt`:

```scala
libraryDependencies ++= Seq(
  "org.flywaydb" % "flyway-core" % "11.15.0",
  "org.flywaydb" % "flyway-database-postgresql" % "11.15.0"
)
```

### 2. Create Migration Directory

```bash
mkdir -p app/src/main/resources/db/migration
```

### 3. Create Initial Migration

Create `app/src/main/resources/db/migration/V1__Create_initial_tables.sql`:

```sql
-- Create employee tables
CREATE TABLE department (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

CREATE TABLE employee (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    age INT NOT NULL,
    department_id INT NOT NULL,
    FOREIGN KEY (department_id) REFERENCES department(id) ON DELETE CASCADE
);

CREATE TABLE phone (
    id SERIAL PRIMARY KEY,
    number VARCHAR(15) NOT NULL
);

CREATE TABLE employee_phone (
    employee_id INT NOT NULL,
    phone_id INT NOT NULL,
    PRIMARY KEY (employee_id, phone_id),
    FOREIGN KEY (employee_id) REFERENCES employee(id) ON DELETE CASCADE,
    FOREIGN KEY (phone_id) REFERENCES phone(id) ON DELETE CASCADE
);
```

### 4. Start Application

```bash
sbt run
```

The application will automatically:
1. Connect to the database
2. Apply pending migrations
3. Start the HTTP server

## Development Workflow

### Creating New Migrations

1. **Generate migration file** with proper naming:
   ```bash
   # Format: V{number}__{description}.sql
   touch app/src/main/resources/db/migration/V2__Add_audit_columns.sql
   ```

2. **Write SQL changes**:
   ```sql
   -- Add audit columns to employee table
   ALTER TABLE employee
   ADD COLUMN created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
   ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
   ```

3. **Test migration**:
   ```bash
   # Run with clean database
   docker-compose down
   docker-compose up -d postgres
   sbt run
   ```

### Migration Best Practices

#### File Naming
- Use descriptive names: `V3__Add_user_indexes.sql`
- Sequential numbering: V1, V2, V3...
- Snake case descriptions

#### SQL Content
- Each migration should be **idempotent**
- Include **comments** explaining complex changes
- Use **transaction-safe** operations
- Test migrations on clean database

#### Examples

**Add new table:**
```sql
-- Create user accounts table
CREATE TABLE user_account (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Add index for performance
CREATE INDEX idx_user_account_username ON user_account(username);
```

**Add column to existing table:**
```sql
-- Add status column to employee
ALTER TABLE employee
ADD COLUMN status VARCHAR(20) DEFAULT 'active',
ADD COLUMN last_login TIMESTAMP;

-- Add constraint for valid statuses
ALTER TABLE employee
ADD CONSTRAINT chk_employee_status
CHECK (status IN ('active', 'inactive', 'suspended'));
```

**Data migration:**
```sql
-- Populate new status column based on existing data
UPDATE employee
SET status = CASE
    WHEN updated_at > CURRENT_DATE - INTERVAL '30 days' THEN 'active'
    ELSE 'inactive'
END;
```

## Testing Migrations

### Unit Testing

```scala
// In your test suite
test("migration should create tables correctly") {
  val testLayer = TestContainerSupport.testDbLayerWithFlyway
  val assertion = for {
    // Migration automatically runs in test setup
    departments <- ZIO.serviceWithZIO[Transactor] { xa =>
      sql"SELECT COUNT(*) FROM department".query[Int].unique.transact(xa)
    }
    employees <- ZIO.serviceWithZIO[Transactor] { xa =>
      sql"SELECT COUNT(*) FROM employee".query[Int].unique.transact(xa)
    }
  } yield assert(departments)(isGreaterThan(0)) && assert(employees)(isGreaterThan(0))

  assertion.provide(testLayer)
}
```

### Integration Testing

```scala
test("application should start with migrations") {
  val testApp = for {
    _ <- ZIO.serviceWithZIO[FlywayService](_.migrate)
    // Test application functionality with migrated schema
    result <- employeeService.create(Employee(name = "John", age = 30, departmentId = 1))
  } yield assert(result.name)(equalTo("John"))

  testApp.provideSome[FlywayService](TestContainerSupport.testDbLayerWithFlyway)
}
```

## Configuration

### Application Configuration

Add to `src/main/resources/application.conf`:

```hocon
flyway {
  locations = ["classpath:db/migration"]
  baseline-on-migrate = true
  validate-on-migrate = true
  out-of-order = false
}

environments {
  dev {
    flyway.locations = ["classpath:db/migration", "classpath:db/dev"]
  }
  test {
    flyway.locations = ["classpath:db/migration"]
    flyway.clean-disabled = false
  }
  prod {
    flyway.locations = ["classpath:db/migration"]
    flyway.clean-disabled = true
    flyway.validate-on-migrate = true
  }
}
```

### Environment Variables

```bash
# Development
export DB_URL=jdbc:postgresql://localhost:5432/zio_backend_dev
export DB_USER=postgres
export DB_PASSWORD=postgres

# Testing
export DB_URL=jdbc:postgresql://localhost:5432/zio_backend_test
export DB_USER=test
export DB_PASSWORD=test

# Production
export DB_URL=jdbc:postgresql://prod-db:5432/zio_backend
export DB_USER=app_user
export DB_PASSWORD=${SECURE_PASSWORD}
```

## Troubleshooting

### Common Issues

#### Migration Failed
```bash
Error: Migration V2__Add_column failed
```

**Solution**:
1. Check the SQL syntax in the migration file
2. Verify database connectivity
3. Review logs for specific error details

#### Checksum Mismatch
```bash
Error: Checksum mismatch for migration V1
```

**Solution**:
1. If migration was manually modified: `flyway repair`
2. If change is intentional: create new migration instead

#### Pending Migrations
```bash
Warning: 2 pending migrations found
```

**Solution**:
1. Migrations will run automatically on startup
2. Or run manually: `curl -X POST http://localhost:8080/api/migrations/migrate`

### Monitoring Migration Status

#### Check Current Status
```bash
curl http://localhost:8080/api/migrations
```

#### Validate Pending Migrations
```bash
curl -X POST http://localhost:8080/api/migrations/validate
```

#### Get Migration History
```bash
curl http://localhost:8080/api/migrations/info
```

## Production Deployment

### Pre-deployment Checklist

1. **Backup database**:
   ```bash
   pg_dump zio_backend > backup_$(date +%Y%m%d).sql
   ```

2. **Test migrations** on staging environment
3. **Verify migration count** and expected changes
4. **Schedule maintenance window** for large migrations

### Deployment Steps

1. **Deploy application** with new migrations
2. **Monitor startup logs** for migration execution
3. **Verify application health**:
   ```bash
   curl http://localhost:8080/health
   ```
4. **Check migration status**:
   ```bash
   curl http://localhost:8080/api/migrations
   ```

### Rollback Strategy

If migrations fail:
1. **Stop application** to prevent partial state
2. **Investigate failure** from application logs
3. **Restore database** from backup if necessary
4. **Fix migration** and redeploy

## Migration Commands Reference

### SBT Commands

```bash
# Run migrations manually (if not automatic)
sbt "project app" "runMain com.example.app.Migrate"

# Clean database (development only)
sbt "project app" "runMain com.example.app.Clean"

# Validate migrations
sbt "project app" "runMain com.example.app.Validate"
```

### HTTP API Commands

```bash
# Get migration status
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8080/api/migrations

# Validate pending migrations
curl -X POST -H "Authorization: Bearer $TOKEN" \
     http://localhost:8080/api/migrations/validate

# Execute migrations
curl -X POST -H "Authorization: Bearer $TOKEN" \
     http://localhost:8080/api/migrations/migrate

# Get specific migration info
curl -H "Authorization: Bearer $TOKEN" \
     "http://localhost:8080/api/migrations/info?version=V1__Create_initial_tables"
```

## Next Steps

1. **Review existing database schema** and plan V1 migration
2. **Set up development database** with migration testing
3. **Configure CI/CD pipeline** for migration validation
4. **Document team migration workflow** and naming conventions
5. **Plan production migration strategy** and rollback procedures

For more detailed information, see:
- [Research findings](research.md) for technical implementation details
- [Data model](data-model.md) for entity definitions
- [API contracts](contracts/management-api.yaml) for management endpoints