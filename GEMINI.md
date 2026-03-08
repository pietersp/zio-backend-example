# Project Mandates (GEMINI.md)

This file contains foundational mandates for the `zio-backend-example` project. These instructions take absolute precedence over general workflows.

## Tech Stack & Environment
- **Language:** Scala 3.6.4 (2-space indentation, as per `.scalafmt.conf`).
- **Framework:** ZIO 2.x (Http, Test).
- **Domain Modeling:** Iron for refined types and smart constructors.
- **Persistence:** Magnum (PostgreSQL access) and Flyway (migrations).
- **Build Tool:** sbt.
- **Architecture:** Multi-module project with strict layering.

## Architectural Boundaries
Rigorously preserve the module boundaries as defined in `openspec/specs/project-architecture/spec.md`:

1.  **`domain`**: Contains entities, refined types (Iron), and schemas.
    - **Rule:** MUST NOT depend on `core`, `app`, or `client`. Minimal dependencies only.
2.  **`endpoints`**: Contains `zio-http` endpoint declarations and codecs.
    - **Rule:** Keep HTTP contracts separate from implementation.
3.  **`core`**: Contains business logic and repository interfaces (traits).
    - **Rule:** Depends only on `domain`. MUST NOT have direct database or HTTP server dependencies.
4.  **`app`**: The application edge. Contains repository implementations (Magnum), HTTP handlers, ZLayer wiring, Flyway migrations, and server startup.
5.  **`client`**: Client implementation built from `domain` and `endpoints`.
    - **Rule:** MUST NOT depend on `app` internals.

## Coding Standards & Patterns
- **Functional Programming:** Use ZIO effects exclusively for side-effecting code.
- **Type Safety:** Prefer Iron's refined types over primitive types for domain concepts (e.g., `EmployeeId`, `Age`).
- **Dependency Injection:** Use `ZLayer` for wiring dependencies.
- **Error Handling:** Define domain-specific error types in `domain` or `core`.
- **Database Migrations:** Add new SQL migrations to `app/src/main/resources/db/migration/` following Flyway naming conventions (e.g., `V3__description.sql`).

## Testing Standards
Follow the patterns established in `TESTING.md`:
- **Framework:** ZIO Test (`ZIOSpecDefault`).
- **Assertions:** Use `assertTrue` for smart, composable assertions.
- **Mocking:** Use `ZLayer` to provide in-memory or mock implementations of dependencies.
- **Isolation:** Each test should be independent and provide its own layer if it modifies state.
- **Reproduction:** Always create a reproduction test case before fixing a bug.

## OpenSpec Workflow
This project uses **OpenSpec** for managing changes and specifications.
- Specifications are located in `openspec/specs/`.
- Change proposals and tasks are tracked in `openspec/changes/`.
- When implementing a change, refer to the corresponding spec and task list.
- Update task checkboxes (`- [ ]` → `- [x]`) in the relevant markdown files as you progress.
