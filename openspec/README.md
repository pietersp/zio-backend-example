# OpenSpec Guide

This repository already has the standard OpenSpec directory layout. The files in this folder describe the current backend so future change proposals can stay aligned with the existing Scala/ZIO module boundaries.

## Repository Map

- `domain`: shared entities, refined value types, schemas, and shared errors
- `endpoints`: `zio-http` endpoint declarations and codecs
- `core`: service logic and repository interfaces
- `app`: HTTP handlers, repository live implementations, runtime wiring, Flyway, Swagger/OpenAPI
- `client`: API client built from the shared contracts

## Current Baseline Specs

- `specs/project-architecture/spec.md`: layering and dependency rules
- `specs/employee-directory-api/spec.md`: REST API capabilities for departments, employees, phones, and employee-phone relationships
- `specs/runtime-and-persistence/spec.md`: database, migrations, configuration, and testing expectations

## How To Scope Changes

When creating an OpenSpec change for this repo, start by identifying which layer changes:

1. `domain` if a shared type, validation rule, or shared error changes.
2. `endpoints` if the HTTP contract changes.
3. `core` if business rules or repository interfaces change.
4. `app` if handlers, repository implementations, runtime wiring, or migrations change.
5. `client` if downstream client behavior or usage examples must change.

Most features cross more than one module. A typical API feature touches `domain`, `endpoints`, `core`, `app`, and tests.

## Practical Reminders

- Preserve the existing dependency direction: `domain` at the bottom, `app` at the edge.
- Keep endpoint declarations in `endpoints` and handler implementations in `app`.
- Keep business logic in `core` and persistence implementations in `app`.
- Add Flyway migrations under `app/src/main/resources/db/migration` for schema changes.
- Prefer ZIO Test for unit and integration coverage.

## Useful Commands

```bash
sbt test
sbt core/test
sbt app/test
sbt run
docker-compose up -d
docker-compose down
```
