---

description: "Task list for Database Schema Management with Flyway implementation"
---

# Tasks: Database Schema Management with Flyway

**Input**: Design documents from `/specs/001-flyway-migrations/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Constitution mandates test-first development with integration coverage for database operations.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions

- **Multi-module SBT project**: `modules/domain/src/`, `modules/core/src/`, `modules/app/src/`, `modules/endpoints/src/`
- **Tests**: `modules/*/src/test/scala/`
- **Migrations**: `modules/app/src/main/resources/db/migration/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and Flyway dependencies

- [x] T001 Add Flyway dependencies to app module in build.sbt
- [x] T002 Create migration directory structure in app/src/main/resources/db/migration/
- [x] T003 [P] Create application configuration for Flyway in app/src/main/resources/application.conf
- [x] T004 [P] Add Flyway SBT tasks to project/plugins.sbt and build.sbt

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core Flyway service layer that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T005 Create FlywayService trait in app/src/main/scala/com/example/app/database/FlywayService.scala
- [x] T006 [P] Create FlywayServiceLive implementation in app/src/main/scala/com/example/app/database/FlywayServiceLive.scala
- [x] T007 [P] Create FlywayException types in domain/src/main/scala/com/example/domain/errors/FlywayException.scala
- [x] T008 [P] Create Flyway configuration case class in app/src/main/scala/com/example/app/config/FlywayConfig.scala
- [x] T009 Create Flyway ZIO layer in app/src/main/scala/com/example/app/database/FlywayLayers.scala
- [x] T010 [P] Update Main.scala to integrate Flyway service before database layer creation
- [x] T011 [P] Remove manual table creation logic from existing Main.scala

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Database Schema Initialization (Priority: P1) 🎯 MVP

**Goal**: Automatically create and version database schema on application startup

**Independent Test**: Start application with clean database and verify all tables are created with correct structure and Flyway schema history table is initialized

### Tests for User Story 1 (REQUIRED by Constitution) ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [x] T012 [P] [US1] Create integration test for clean database startup in app/src/test/scala/com/example/app/database/FlywayIntegrationTest.scala
- [x] T013 [P] [US1] Create test for existing database baseline in app/src/test/scala/com/example/app/database/FlywayBaselineTest.scala
- [x] T014 [P] [US1] Create test for schema history table verification in app/src/test/scala/com/example/app/database/SchemaHistoryTest.scala

### Implementation for User Story 1

- [x] T015 [US1] Create initial V1 migration for employee tables in app/src/main/resources/db/migration/V1__Create_initial_tables.sql
- [x] T016 [US1] Implement automatic migration execution in FlywayServiceLive.migrate method
- [x] T017 [US1] Add migration status tracking in FlywayServiceLive.info method
- [x] T018 [US1] Implement checksum validation in FlywayServiceLive.validate method
- [x] T019 [US1] Add error handling and logging for migration failures
- [x] T020 [US1] Update application startup sequence to run migrations before serving requests

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - Schema Evolution and Migration (Priority: P2)

**Goal**: Create and apply migration files that automatically update existing databases

**Independent Test**: Create new migration file, apply to database with existing schema, and verify changes are correctly applied without data loss

### Tests for User Story 2 (REQUIRED by Constitution) ⚠️

- [ ] T021 [P] [US2] Create test for migration execution on existing schema in modules/app/src/test/scala/com/example/app/database/MigrationExecutionTest.scala
- [ ] T022 [P] [US2] Create test for migration failure handling in modules/app/src/test/scala/com/example/app/database/MigrationFailureTest.scala
- [ ] T023 [P] [US2] Create test for multiple pending migrations in modules/app/src/test/scala/com/example/app/database/MultipleMigrationTest.scala

### Implementation for User Story 2

- [ ] T024 [P] [US2] Create V2 migration example in modules/app/src/main/resources/db/migration/V2__Add_audit_columns.sql
- [ ] T025 [US2] Implement migration ordering logic in FlywayServiceLive
- [ ] T026 [US2] Add rollback capabilities for failed migrations
- [ ] T027 [US2] Implement comprehensive error reporting for migration failures
- [ ] T028 [US2] Add migration validation before execution
- [ ] T029 [US2] Create utility methods for migration state inspection

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - Development and Testing Database Management (Priority: P3)

**Goal**: Automatically create and clean database schemas for each test run

**Independent Test**: Run integration tests multiple times and verify each run starts with clean database state

### Tests for User Story 3 (REQUIRED by Constitution) ⚠️

- [ ] T030 [P] [US3] Update TestContainerSupport for Flyway integration in modules/app/src/test/scala/com/example/app/database/TestContainerSupport.scala
- [ ] T031 [P] [US3] Create test for test database isolation in modules/app/src/test/scala/com/example/app/database/TestIsolationTest.scala
- [ ] T032 [P] [US3] Create test for test cleanup between runs in modules/app/src/test/scala/com/example/app/database/TestCleanupTest.scala

### Implementation for User Story 3

- [ ] T033 [P] [US3] Create test-specific Flyway configuration in modules/app/src/test/resources/application-test.conf
- [ ] T034 [US3] Implement test database cleanup in TestContainerSupport
- [ ] T035 [US3] Add test data migration support for isolated test runs
- [ ] T036 [US3] Create test migration files in modules/app/src/test/resources/db/test/migration/
- [ ] T037 [US3] Update existing integration tests to use Flyway-managed schema

**Checkpoint**: All user stories should now be independently functional

---

## Phase 6: Management API (Additional Enhancement)

**Purpose**: HTTP endpoints for migration monitoring and management (from contracts)

### Tests for Management API (OPTIONAL but recommended)

- [ ] T038 [P] Create contract test for migration status endpoint in modules/endpoints/src/test/scala/com/example/endpoints/contracts/MigrationApiContractTest.scala
- [ ] T039 [P] Create integration test for management endpoints in modules/endpoints/src/test/scala/com/example/endpoints/integration/MigrationApiIntegrationTest.scala

### Implementation for Management API

- [ ] T040 [P] Create migration DTOs in modules/endpoints/src/main/scala/com/example/endpoints/dto/MigrationDto.scala
- [ ] T041 Create MigrationRoutes in modules/endpoints/src/main/scala/com/example/endpoints/routes/MigrationRoutes.scala
- [ ] T042 Add migration management endpoints to existing Router
- [ ] T043 Add authentication/authorization for management endpoints
- [ ] T044 Update API documentation for migration endpoints

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T045 [P] Update documentation in README.md with Flyway setup instructions
- [ ] T046 Add performance monitoring for migration execution times
- [ ] T047 [P] Add additional unit tests for FlywayService components in modules/app/src/test/scala/com/example/app/database/FlywayServiceUnitTest.scala
- [ ] T048 Add production deployment guide for migrations
- [ ] T049 Run quickstart.md validation and update instructions
- [ ] T050 Code cleanup and optimization based on test results
- [ ] T051 Add migration linting and validation in CI/CD pipeline

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-5)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
- **Management API (Phase 6)**: Depends on User Stories 1-2 completion
- **Polish (Phase 7)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - Builds on US1 but independently testable
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) - Supports testing for all stories but independently testable

### Within Each User Story

- Tests MUST be written and FAIL before implementation (Constitutional requirement)
- Domain models before services
- Services before infrastructure
- Core implementation before integration
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Once Foundational phase completes, all user stories can start in parallel (if team capacity allows)
- All tests for a user story marked [P] can run in parallel
- Different user stories can be worked on in parallel by different team members

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together (required by Constitution):
Task: "Create integration test for clean database startup in modules/app/src/test/scala/com/example/app/database/FlywayIntegrationTest.scala"
Task: "Create test for existing database baseline in modules/app/src/test/scala/com/example/app/database/FlywayBaselineTest.scala"
Task: "Create test for schema history table verification in modules/app/src/test/scala/com/example/app/database/SchemaHistoryTest.scala"

# Launch implementation tasks for User Story 1:
Task: "Create initial V1 migration for employee tables in modules/app/src/main/resources/db/migration/V1__Create_initial_tables.sql"
Task: "Implement automatic migration execution in FlywayServiceLive.migrate method"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup
2. Complete Phase 2: Foundational (CRITICAL - blocks all stories)
3. Complete Phase 3: User Story 1
4. **STOP and VALIDATE**: Test User Story 1 independently with clean database
5. Deploy/demo automated schema initialization

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP!)
3. Add User Story 2 → Test independently → Deploy/Demo
4. Add User Story 3 → Test independently → Deploy/Demo
5. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together
2. Once Foundational is done:
   - Developer A: User Story 1 (P1 - critical path)
   - Developer B: User Story 2 (P2 - schema evolution)
   - Developer C: User Story 3 (P3 - testing infrastructure)
3. Stories complete and integrate independently

---

## Notes

- [P] tasks = different files, no dependencies
- [Story] label maps task to specific user story for traceability
- Each user story should be independently completable and testable
- **Constitutional requirement**: Verify tests fail before implementing
- Commit after each task or logical group
- Stop at any checkpoint to validate story independently
- Database migrations must preserve existing employee domain entities
- All ZIO functional patterns and Iron type safety must be maintained
- Performance requirement: migrations complete within 30 seconds

---

**Task Summary**:
- **Total Tasks**: 51
- **Setup**: 4 tasks
- **Foundational**: 7 tasks (blocks all stories)
- **User Story 1**: 9 tasks (6 tests + 3 implementation)
- **User Story 2**: 9 tasks (3 tests + 6 implementation)
- **User Story 3**: 8 tasks (3 tests + 5 implementation)
- **Management API**: 7 tasks (2 tests + 5 implementation)
- **Polish**: 7 tasks

**Parallel Opportunities**: 32 tasks marked [P] can be parallelized across team members
**Independent Test Coverage**: All 3 user stories have comprehensive test coverage as required by constitution
**MVP Scope**: User Story 1 (13 tasks total: 4 setup + 7 foundational + 2 implementation)