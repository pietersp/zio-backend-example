<!--
Sync Impact Report:
- Version change: 0.0.0 → 1.0.0 (initial constitution)
- Modified principles: N/A (new constitution)
- Added sections: Core Principles (5 principles), Architecture Constraints, Development Workflow, Governance
- Removed sections: N/A
- Templates requiring updates: ✅ plan-template.md (constitution check section validated), ✅ spec-template.md (validated), ✅ tasks-template.md (validated)
- Follow-up TODOs: None
-->

# zio-backend-example Constitution

## Core Principles

### I. Domain-First Architecture
Every entity begins in the domain module; Domain entities must be self-contained, independently testable, documented with Iron-refined types; Clear domain purpose required - no technical-only entities.

### II. Functional Core with ZIO
All business logic implemented in core using ZIO effects; Core must remain pure and dependency-free; Side effects managed through ZIO's runtime system; Error handling using typed ZIO errors.

### III. Test-First with Integration Coverage
TDD mandatory: Tests written → User approved → Tests fail → Then implement; Integration tests required for database interactions, endpoint contracts, and cross-module communication using testcontainers.

### IV. Module Boundary Integrity
Each module has clear responsibilities: domain (entities), endpoints (contracts), core (business logic), app (implementation); Dependencies flow unidirectionally (app → core → domain, endpoints → domain); No circular dependencies between modules.

### V. Type Safety and Validation
All public APIs use Iron-refined types for validation; Schema-based validation for HTTP endpoints; Compile-time guarantees for domain invariants; Runtime validation failures must be explicit and typed.

## Architecture Constraints

**Technology Stack**: Scala 3.6.4, ZIO HTTP 3.3.3, Magnum 2.0.0-M1, Iron 3.0.1, PostgreSQL
**Module Structure**: Multi-module SBT project with domain, endpoints, core, app, client
**Database**: PostgreSQL with Magnum ORM, migrations managed through Flyway
**Testing**: ZIO Test framework with testcontainers for integration tests
**Performance**: HTTP requests must complete within 200ms p95 for single-record operations

## Development Workflow

**Code Review Requirements**: All PRs must pass automated tests, maintain module boundaries, include integration tests for new endpoints
**Testing Gates**: Unit tests for business logic, integration tests for database operations, contract tests for HTTP endpoints
**Documentation Requirements**: Module README files explaining public APIs, endpoint documentation with examples, domain entity documentation

## Governance

This constitution supersedes all other development practices; Amendments require documentation, team approval, and migration plan; All code reviews must verify constitutional compliance; Module boundary violations must be explicitly justified in PR descriptions.

**Version**: 1.0.0 | **Ratified**: 2025-11-03 | **Last Amended**: 2025-11-03