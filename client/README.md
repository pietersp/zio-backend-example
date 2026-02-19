# API Client Module

This module provides a comprehensive, type-safe HTTP client for the ZIO backend example API.

## Architecture

### Components

- **`ApiClient`**: Trait defining all client operations
- **`ApiClientLive`**: Implementation with proper ZIO patterns
- **`ApiClientConfig`**: Configuration case class with multiple layer constructors
- **`ApiClientExample`**: Complete usage example demonstrating all operations


## Usage

### Basic Setup

```scala
import com.example.client.*
import zio.*
import zio.http.Client

val program: ZIO[ApiClient, Throwable, Unit] =
  for {
    client <- ZIO.service[ApiClient]
    dept <- client.createDepartment(Department(DepartmentName("Engineering")))
    _ <- Console.printLine(s"Created department: $dept")
  } yield ()

// Run with local configuration
program.provide(
  ApiClientLive.layer,
  ApiClientConfig.localLayer,  // Uses http://localhost:8080
  Client.default
)
```

### Configuration Options

#### 1. Local Development (Default)
```scala
ApiClientConfig.localLayer
```
Uses `http://localhost:8080` with 30-second timeout and 3 retries.

#### 2. Custom Base URL
```scala
ApiClientConfig.withBaseUrl("https://api.production.com")
```

#### 3. From Environment/Config
```scala
ApiClientConfig.layer
```
Reads from ZIO Config:
- `api.client.base-url`
- `api.client.timeout` (optional, defaults to 30s)
- `api.client.retries` (optional, defaults to 3)

### Available Operations

#### Department Operations
```scala
client.createDepartment(department: Department): IO[DepartmentAlreadyExists, DepartmentId]
client.getDepartments: UIO[Vector[Department]]
client.getDepartmentById(id: DepartmentId): IO[DepartmentNotFound, Department]
client.updateDepartment(id: DepartmentId, department: Department): IO[DepartmentNotFound, Unit]
client.deleteDepartment(id: DepartmentId): UIO[Unit]
```

#### Employee Operations
```scala
client.createEmployee(employee: Employee): IO[DepartmentNotFound, EmployeeId]
client.getEmployees: UIO[Vector[Employee]]
client.getEmployeeById(id: EmployeeId): IO[EmployeeNotFound, Employee]
client.updateEmployee(id: EmployeeId, employee: Employee): IO[EmployeeNotFound, Unit]
client.deleteEmployee(id: EmployeeId): UIO[Unit]
```

#### Phone Operations
```scala
client.createPhone(phone: Phone): IO[PhoneAlreadyExists, PhoneId]
client.getPhoneById(id: PhoneId): IO[PhoneNotFound, Phone]
client.updatePhone(id: PhoneId, phone: Phone): IO[PhoneNotFound, Unit]
client.deletePhone(id: PhoneId): UIO[Unit]
```

#### EmployeePhone Operations
```scala
client.addPhoneToEmployee(employeeId: EmployeeId, phoneId: PhoneId): IO[AppError, Unit]
client.getEmployeePhones(employeeId: EmployeeId): IO[EmployeeNotFound, Vector[Phone]]
client.removePhoneFromEmployee(employeeId: EmployeeId, phoneId: PhoneId): IO[AppError, Unit]
```

### Error Handling

All operations return properly typed errors:

```scala
client.createDepartment(dept)
  .catchAll {
    case AppError.DepartmentAlreadyExists => 
      Console.printLine("Department already exists")
  }
  .catchAllDefect(defect =>
    Console.printLine(s"Unexpected error: $defect")
  )
```

## Running the Example

```bash
# Start the server first
sbt app/run

# In another terminal, run the client example
sbt client/run
```

The example demonstrates:
- Creating departments, employees, and phones
- Associating phones with employees
- Retrieving data
- Error handling patterns

## Testing

To test against a running server:

```scala
test("create and retrieve department") {
  for {
    client <- ZIO.service[ApiClient]
    dept = Department(DepartmentName("Test"))
    deptId <- client.createDepartment(dept)
    retrieved <- client.getDepartmentById(deptId)
  } yield assertTrue(retrieved.name == dept.name)
}.provide(
  ApiClientLive.layer,
  ApiClientConfig.localLayer,
  Client.default
)
```

## Dependencies

The client module depends on:
- `domain`: For domain models (Department, Employee, etc.)
- `endpoints`: For endpoint definitions
- `zio-http`: For HTTP client capabilities