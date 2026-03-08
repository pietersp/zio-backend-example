# Employee Directory API

## Purpose

Describe the current HTTP capabilities exposed by the service for departments, employees, phones, and employee-phone relationships.

## Requirements

### Requirement: Department management supports CRUD and listing

The system SHALL provide HTTP endpoints that allow clients to create departments, list departments, fetch a department by id, update a department by id, and delete a department by id.

#### Scenario: Creating a department with a duplicate identity

- GIVEN a client submits a department that already exists according to service rules
- WHEN the create department endpoint is invoked
- THEN the request is rejected with a conflict error

#### Scenario: Fetching an unknown department

- GIVEN a department id that does not exist
- WHEN the get department by id endpoint is invoked
- THEN the request is rejected with a not found error

### Requirement: Employee management supports CRUD and listing

The system SHALL provide HTTP endpoints that allow clients to create employees, list employees, fetch an employee by id, update an employee by id, and delete an employee by id.

#### Scenario: Creating an employee for a missing department

- GIVEN a client submits an employee that references a department id that does not exist
- WHEN the create employee endpoint is invoked
- THEN the request is rejected with a not found error for the missing department

#### Scenario: Fetching an unknown employee

- GIVEN an employee id that does not exist
- WHEN the get employee by id endpoint is invoked
- THEN the request is rejected with a not found error

### Requirement: Phone management supports create and id-based maintenance

The system SHALL provide HTTP endpoints that allow clients to create a phone record, fetch a phone by id, update a phone by id, and delete a phone by id.

#### Scenario: Creating a duplicate phone number

- GIVEN a client submits a phone number that already exists according to service rules
- WHEN the create phone endpoint is invoked
- THEN the request is rejected with a conflict error

#### Scenario: Updating an unknown phone

- GIVEN a phone id that does not exist
- WHEN the update phone endpoint is invoked
- THEN the request is rejected with a not found error

### Requirement: Employee-phone relationships are managed per employee

The system SHALL provide HTTP endpoints that allow clients to attach a phone to an employee, list phones for an employee, and remove a phone from an employee.

#### Scenario: Listing phones for an unknown employee

- GIVEN an employee id that does not exist
- WHEN the list employee phones endpoint is invoked
- THEN the request is rejected with a not found error

#### Scenario: Linking an employee and phone when either side is missing

- GIVEN an employee id or phone id that does not exist
- WHEN the add phone to employee endpoint is invoked
- THEN the request is rejected with an application error mapped to a not found response

### Requirement: API documentation is generated from endpoint contracts

The system SHALL generate OpenAPI documentation and Swagger UI from the shared endpoint definitions so the published documentation stays aligned with the implemented routes.

#### Scenario: Adding a new endpoint

- GIVEN a new endpoint is added to the service
- WHEN the application router is updated
- THEN the route is included in the generated OpenAPI set used by Swagger UI
