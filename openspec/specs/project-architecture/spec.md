# Project Architecture

## Purpose

Define the module boundaries and layering rules that changes in this repository are expected to preserve.

## Requirements

### Requirement: Shared domain types remain isolated

The system SHALL keep shared business entities, refined value types, codec-friendly schemas, and shared application errors in the `domain` module so they can be reused across the server, endpoint contracts, and client code without pulling in runtime infrastructure.

#### Scenario: Adding a new shared business concept

- GIVEN a change introduces a new entity, identifier, or refined value type
- WHEN that concept is required by the API contract, business logic, or client
- THEN the shared type is defined in `domain`
- AND `domain` remains independent of `core`, `app`, and `client`

### Requirement: HTTP contracts stay separate from HTTP handling

The system SHALL define `zio-http` endpoint contracts and codecs in the `endpoints` module while implementing request handling in the `app` module.

#### Scenario: Updating an existing route

- GIVEN a route shape, request body, response body, or documented error changes
- WHEN the change is implemented
- THEN the endpoint declaration is updated in `endpoints`
- AND handler logic is updated in `app`
- AND the handler delegates business decisions to `core` services rather than embedding them in the contract layer

### Requirement: Business logic stays independent of persistence details

The system SHALL keep repository interfaces and business services in the `core` module, with concrete database implementations living in the `app` module.

#### Scenario: Adding a persistence-backed business rule

- GIVEN a feature requires new reads or writes
- WHEN the implementation is planned
- THEN repository traits and service behavior are added or updated in `core`
- AND repository live implementations are added or updated in `app`
- AND `core` does not gain direct database, HTTP server, or Flyway dependencies

### Requirement: Runtime wiring belongs at the application edge

The system SHALL assemble ZLayer wiring, server startup, Swagger generation, configuration loading, and migration startup behavior in the `app` module.

#### Scenario: Changing application startup behavior

- GIVEN a change affects server startup, dependency wiring, or environment-specific runtime behavior
- WHEN the change is implemented
- THEN the wiring is updated in `app`
- AND shared modules remain focused on contracts and domain logic

### Requirement: Client access is built from shared contracts

The system SHALL keep the `client` module aligned with shared domain and endpoint contracts so downstream consumers can call the service without depending on server implementation details.

#### Scenario: Extending the API for consumers

- GIVEN a new or changed API capability must be exposed to callers
- WHEN client support is needed
- THEN the client implementation is updated against the shared `domain` and `endpoints` modules
- AND the client does not depend on `app` internals
