# ZIO Testing Guide

This guide documents the testing strategy and best practices for this ZIO backend project.

## Table of Contents

1. [Overview](#overview)
2. [Test Structure](#test-structure)
3. [Dependencies](#dependencies)
4. [Running Tests](#running-tests)
5. [Testing Patterns](#testing-patterns)
6. [Best Practices](#best-practices)
7. [Examples](#examples)

## Overview

This project uses **ZIO Test**, a zero-dependency testing library designed specifically for testing effectful programs. ZIO Test treats tests as first-class values, making it natural to test ZIO effects.

### Key Benefits

- **Tests as Values**: Tests are immutable ZIO effects that compose naturally
- **Type-Safe**: Leverages Scala's type system for compile-time safety
- **No Mocking Libraries Needed**: Mock implementations are just alternative ZLayer providers
- **Built-in Assertions**: Rich set of composable assertions
- **Deterministic**: TestClock, TestRandom, etc. provide predictable behavior

## Test Structure

Tests are organized by module and layer:

```
module/src/test/scala/com/example/
├── service/          # Service layer tests (business logic)
├── repository/       # Repository tests (data access)
├── handler/          # Handler tests (HTTP layer)
└── endpoint/         # Endpoint tests (route definitions)
```

### Testing Layers

1. **Service Layer** (Priority 1) - Test business logic in isolation
2. **Repository Layer** (Priority 2) - Integration or mock tests
3. **Handler Layer** (Priority 3) - HTTP request/response handling
4. **Endpoint Layer** (Priority 4) - Route definitions and validation

## Dependencies

All modules have ZIO Test dependencies configured in `build.sbt`:

```scala
lazy val testDependencies = Seq(
  "dev.zio" %% "zio-test" % "2.1.14" % Test,
  "dev.zio" %% "zio-test-sbt" % "2.1.14" % Test,
  "dev.zio" %% "zio-test-magnolia" % "2.1.14" % Test
)
```

The test framework is configured globally:

```scala
ThisBuild / testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")
```

## Running Tests

### Run All Tests
```bash
sbt test
```

### Run Tests for Specific Module
```bash
sbt core/test
sbt app/test
```

### Run Specific Test Suite
```bash
sbt "core/testOnly com.example.service.EmployeeServiceSpec"
```

### Run Tests Matching Pattern
```bash
sbt "testOnly *ServiceSpec"
```

## Testing Patterns

### 1. Basic Test Structure

All test specs extend `ZIOSpecDefault`:

```scala
import zio.test.*

object MyServiceSpec extends ZIOSpecDefault {
  def spec = suite("MyServiceSpec")(
    test("should do something") {
      for {
        service <- ZIO.service[MyService]
        result <- service.doSomething()
      } yield assertTrue(result == expected)
    }.provide(testLayer)
  )
}
```

### 2. Using Smart Assertions

ZIO Test provides `assertTrue` for simple, expressive assertions:

```scala
test("example") {
  for {
    result <- someEffect
  } yield assertTrue(
    result.id == expectedId,
    result.name == expectedName,
    result.age > 18
  )
}
```

### 3. Testing Failures

Use `.exit` to test error cases:

```scala
test("should fail with NotFound") {
  for {
    service <- ZIO.service[MyService]
    result <- service.get(invalidId).exit
  } yield assertTrue(result.isFailure) && assertTrue(
    result match {
      case Exit.Failure(cause) => cause.failureOption.contains(NotFound)
      case _ => false
    }
  )
}
```

### 4. Mock Dependencies with ZLayer

Create in-memory implementations of repositories:

```scala
case class InMemoryRepository(
  data: Ref[Map[Id, Entity]]
) extends Repository {
  override def create(entity: Entity): UIO[Id] =
    // Implementation using Ref
}

object InMemoryRepository {
  val layer: ZLayer[Any, Nothing, Repository] =
    ZLayer {
      for {
        data <- Ref.make(Map.empty[Id, Entity])
      } yield InMemoryRepository(data)
    }
}
```

### 5. Combining Layers

Combine mock layers to provide dependencies:

```scala
val testLayer: ZLayer[Any, Nothing, MyService] =
  InMemoryRepository.layer ++
    InMemoryOtherDependency.layer >>>
    MyServiceLive.layer
```

### 6. Pre-seeding Test Data

Create layers with initial data:

```scala
object InMemoryRepository {
  def withData(items: (Id, Entity)*): ZLayer[Any, Nothing, Repository] =
    ZLayer {
      for {
        data <- Ref.make(items.toMap)
      } yield InMemoryRepository(data)
    }
}

// Usage
test("example") {
  // test code
}.provide(
  InMemoryRepository.withData(
    Id(1) -> Entity("test")
  ),
  ServiceLive.layer
)
```

## Best Practices

### 1. Test Isolation

Each test should be isolated and not depend on other tests:

```scala
// Good - each test provides its own layer
test("test 1") {
  // test code
}.provide(testLayer)

test("test 2") {
  // test code  
}.provide(testLayer)
```

### 2. Descriptive Test Names

Use descriptive names that explain what is being tested:

```scala
suite("create")(
  test("should create employee when department exists") { ... },
  test("should fail to create employee when department does not exist") { ... }
)
```

### 3. Group Related Tests

Use nested `suite` to organize related tests:

```scala
suite("EmployeeServiceSpec")(
  suite("create")( /* creation tests */ ),
  suite("update")( /* update tests */ ),
  suite("delete")( /* deletion tests */ )
)
```

### 4. Test Both Success and Failure Cases

Always test both happy paths and error scenarios:

```scala
suite("retrieveById")(
  test("should retrieve existing entity") { /* success case */ },
  test("should fail when entity does not exist") { /* error case */ }
)
```

### 5. Use Meaningful Assertions

Prefer multiple specific assertions over generic ones:

```scala
// Good
assertTrue(
  result.name == expected.name,
  result.age == expected.age,
  result.department == expected.department
)

// Less informative
assertTrue(result == expected)
```

### 6. Keep Tests Simple

Tests should be easy to understand and maintain:

```scala
// Good - clear and simple
test("should return empty list when no items exist") {
  for {
    service <- ZIO.service[MyService]
    items <- service.getAll
  } yield assertTrue(items.isEmpty)
}
```

### 7. Mock at the Right Level

- **Service tests**: Mock repositories
- **Repository tests**: Use TestContainers or in-memory DB
- **Handler tests**: Mock services
- **Endpoint tests**: Test route definitions only

## Examples

### Example 1: Service Test with Mock Repository

See `core/src/test/scala/com/example/service/EmployeeServiceSpec.scala` for a complete example showing:

- In-memory repository implementation
- Testing CRUD operations
- Error handling tests
- Pre-seeded test data

### Example 2: Testing Business Logic

```scala
test("should validate employee age is within range") {
  val employee = Employee(
    name = EmployeeName("John"),
    age = Age(25),  // Valid age
    departmentId = DepartmentId(1)
  )
  
  for {
    service <- ZIO.service[EmployeeService]
    result <- service.create(employee)
  } yield assertTrue(result.isInstanceOf[EmployeeId])
}.provide(testLayer)
```

### Example 3: Testing with Multiple Dependencies

```scala
val testLayer: ZLayer[Any, Nothing, EmployeeService] =
  InMemoryEmployeeRepository.layer ++
    InMemoryDepartmentRepository.layer ++
    InMemoryAuditLog.layer >>>
    EmployeeServiceLive.layer
```

## Testing Anti-Patterns to Avoid

### ❌ Don't Use unsafeRun in Tests

```scala
// Bad
test("example") {
  val result = Unsafe.unsafe { implicit u =>
    Runtime.default.unsafe.run(myEffect).getOrThrow()
  }
  assertTrue(result == expected)
}

// Good
test("example") {
  for {
    result <- myEffect
  } yield assertTrue(result == expected)
}
```

### ❌ Don't Share Mutable State Between Tests

```scala
// Bad - shared state
var counter = 0

test("test 1") { counter += 1; ... }
test("test 2") { counter += 1; ... } // Depends on test 1!

// Good - use Ref for state within test
test("test 1") {
  for {
    counter <- Ref.make(0)
    _ <- counter.update(_ + 1)
  } yield ...
}
```

### ❌ Don't Test Implementation Details

```scala
// Bad - testing internal implementation
test("should call repository.find exactly once") { ... }

// Good - testing behavior
test("should return user when user exists") { ... }
```

## Next Steps

1. **Complete Service Tests**: Add tests for PhoneService and EmployeePhoneService
2. **Add Repository Tests**: Test database layer with TestContainers
3. **Add Handler Tests**: Test HTTP handlers with mock services
4. **Add Integration Tests**: End-to-end tests with real database

## Resources

- [ZIO Test Documentation](https://zio.dev/reference/test/)
- [ZIO Test Assertions](https://zio.dev/reference/test/assertions/)
- [ZIO Test Services](https://zio.dev/reference/test/services/)
- [Testing Best Practices](https://zio.dev/reference/test/why-zio-test/)
