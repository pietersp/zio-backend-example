# Research: Database Schema Management with Flyway

**Date**: 2025-11-03 | **Branch**: 001-flyway-migrations

## Technical Decisions

### ZIO Flyway Integration Approach

**Decision**: Wrap Flyway Java API in ZIO effects with a dedicated FlywayService layer

**Rationale**:
- Maintains ZIO's functional programming paradigm
- Provides typed error handling for migration failures
- Enables proper resource management through ZIO layers
- Integrates cleanly with existing dependency injection pattern

**Alternatives considered**:
- Direct Java API usage: Would break functional patterns and lack proper error handling
- Third-party ZIO-Flyway libraries: Limited ecosystem and potential version conflicts

### Module Organization

**Decision**: Place migration files in `app/src/main/resources/db/migration/`

**Rationale**:
- App module owns complete application setup including database infrastructure
- Maintains constitutional module boundary integrity (infrastructure in app, domain in domain)
- Automatic classpath discovery by Flyway
- Clear separation from business logic in core module

**Alternatives considered**:
- Separate migrations module: Unnecessary complexity for current scale
- Root-level migrations directory: Breaks module cohesion

### Testing Strategy

**Decision**: Integrate Flyway with existing TestContainerSupport

**Rationale**:
- Leverages existing testcontainers infrastructure
- Ensures consistent database state across test runs
- Supports test isolation through automatic schema recreation
- Maintains test-first development principle

**Alternatives considered**:
- In-memory databases: Would not reflect PostgreSQL-specific behavior
- Manual test schema creation: Defeats purpose of automated migrations

## Best Practices Identified

### 1. Migration File Organization
```
db/migration/
├── V1__Create_initial_tables.sql
├── V2__Add_indexes.sql
└── V3__Add_audit_columns.sql
```

### 2. Configuration Management
- Environment-specific migration locations (dev/test/prod)
- Configurable validation and clean settings
- Integration with ZIO Config for type-safe configuration

### 3. Production Readiness
- Baseline migration for existing databases
- Validation before migration execution
- Comprehensive error handling and logging
- Rollback capabilities for failed migrations

### 4. Integration with Magnum ORM
- Flyway manages schema structure
- Magnum provides type-safe database access
- Clear separation of concerns between schema and data access

## Implementation Requirements

### Dependencies
- `org.flywaydb:flyway-core:11.15.0`
- `org.flywaydb:flyway-database-postgresql:11.15.0`

### SBT Configuration
- Add Flyway dependencies to app module
- Configure migration directories
- Set up database migration tasks

### Application Structure
- FlywayService layer for ZIO integration
- Updated Main.scala to execute migrations on startup
- Enhanced TestContainerSupport for test isolation

### Migration Strategy
- Extract current manual table creation to V1 migration
- Implement baseline strategy for existing databases
- Establish naming conventions and development workflow

## Risk Assessment

### Low Risk
- Well-established Flyway technology
- Strong ZIO integration patterns available
- Maintains existing Magnum ORM usage

### Medium Risk
- Application startup timing (migrations add overhead)
- Existing database migration path requires baseline strategy

### Mitigation Strategies
- Comprehensive testing with testcontainers
- Staged rollout with rollback capability
- Monitoring and logging for migration execution

## Success Metrics

- Database schema automatically created on clean startup
- Zero manual database setup required
- Consistent schema across environments
- Migration completion within 30 seconds
- All existing tests continue to pass