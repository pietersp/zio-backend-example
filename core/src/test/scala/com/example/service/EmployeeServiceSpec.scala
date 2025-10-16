package com.example.service

import com.example.domain.{
  Age,
  Department,
  DepartmentId,
  DepartmentIdDescription,
  DepartmentName,
  Employee,
  EmployeeId,
  EmployeeIdDescription,
  EmployeeName
}
import com.example.error.AppError.{DepartmentNotFound, EmployeeNotFound}
import com.example.repository.{DepartmentRepository, EmployeeRepository}
import io.github.iltotore.iron.*
import zio.*
import zio.test.*
import zio.test.Assertion.*

object EmployeeServiceSpec extends ZIOSpecDefault {

  // Mock repository implementations for testing
  case class InMemoryEmployeeRepository(
    employees: Ref[Map[EmployeeId, Employee]],
    nextId: Ref[Int]
  ) extends EmployeeRepository {

    override def create(employee: Employee): UIO[EmployeeId] =
      for {
        id <- nextId.getAndUpdate(_ + 1)
        employeeId = id.refineUnsafe[EmployeeIdDescription]
        _ <- employees.update(_ + (employeeId -> employee))
      } yield employeeId

    override def retrieve(employeeId: EmployeeId): UIO[Option[Employee]] =
      employees.get.map(_.get(employeeId))

    override def retrieveAll: UIO[Vector[Employee]] =
      employees.get.map(_.values.toVector)

    override def update(employeeId: EmployeeId, employee: Employee): UIO[Unit] =
      employees.update(_.updated(employeeId, employee))

    override def delete(employeeId: EmployeeId): UIO[Unit] =
      employees.update(_ - employeeId)
  }

  object InMemoryEmployeeRepository {
    val layer: ZLayer[Any, Nothing, EmployeeRepository] =
      ZLayer {
        for {
          employees <- Ref.make(Map.empty[EmployeeId, Employee])
          nextId <- Ref.make(1)
        } yield InMemoryEmployeeRepository(employees, nextId)
      }
  }

  case class InMemoryDepartmentRepository(
    departments: Ref[Map[DepartmentId, Department]],
    nextId: Ref[Int]
  ) extends DepartmentRepository {

    override def create(department: Department): UIO[DepartmentId] =
      for {
        id <- nextId.getAndUpdate(_ + 1)
        departmentId = id.refineUnsafe[DepartmentIdDescription]
        _ <- departments.update(_ + (departmentId -> department))
      } yield departmentId

    override def retrieve(departmentId: DepartmentId): UIO[Option[Department]] =
      departments.get.map(_.get(departmentId))

    override def retrieveByName(name: DepartmentName): UIO[Option[Department]] =
      departments.get.map(_.values.find(_.name == name))

    override def retrieveAll: UIO[Vector[Department]] =
      departments.get.map(_.values.toVector)

    override def update(
      departmentId: DepartmentId,
      department: Department
    ): UIO[Unit] =
      departments.update(_.updated(departmentId, department))

    override def delete(departmentId: DepartmentId): UIO[Unit] =
      departments.update(_ - departmentId)
  }

  object InMemoryDepartmentRepository {
    val layer: ZLayer[Any, Nothing, DepartmentRepository] =
      ZLayer {
        for {
          departments <- Ref.make(Map.empty[DepartmentId, Department])
          nextId <- Ref.make(1)
        } yield InMemoryDepartmentRepository(departments, nextId)
      }

    def withDepartments(
      deps: (DepartmentId, Department)*
    ): ZLayer[Any, Nothing, DepartmentRepository] =
      ZLayer {
        for {
          departments <- Ref.make(deps.toMap)
          nextId <- Ref.make(deps.map(_._1).maxOption.getOrElse(0) + 1)
        } yield InMemoryDepartmentRepository(departments, nextId)
      }
  }

  // Test layer that combines all dependencies
  val testLayer: ZLayer[Any, Nothing, EmployeeService] =
    InMemoryEmployeeRepository.layer ++
      InMemoryDepartmentRepository.layer >>>
      EmployeeServiceLive.layer

  def spec = suite("EmployeeServiceSpec")(
    suite("create")(
      test("should create employee when department exists") {
        val department = Department(DepartmentName("Engineering"))
        val employee = Employee(
          name = EmployeeName("JohnDoe"),
          age = Age(30),
          departmentId = 1.refineUnsafe[DepartmentIdDescription]
        )

        for {
          service <- ZIO.service[EmployeeService]
          employeeId <- service.create(employee)
          retrieved <- service.retrieveById(employeeId)
        } yield assertTrue(
          retrieved.name == employee.name,
          retrieved.age == employee.age,
          retrieved.departmentId == employee.departmentId
        )
      }.provide(
        InMemoryEmployeeRepository.layer,
        InMemoryDepartmentRepository.withDepartments(
          1.refineUnsafe[DepartmentIdDescription] -> Department(
            DepartmentName("Engineering")
          )
        ),
        EmployeeServiceLive.layer
      ),
      test("should fail to create employee when department does not exist") {
        val employee = Employee(
          name = EmployeeName("JaneDoe"),
          age = Age(25),
          departmentId = 999.refineUnsafe[DepartmentIdDescription]
        )

        for {
          service <- ZIO.service[EmployeeService]
          result <- service.create(employee).exit
        } yield assertTrue(result.isFailure) && assertTrue(
          result match {
            case Exit.Failure(cause) =>
              cause.failureOption.contains(DepartmentNotFound)
            case _ => false
          }
        )
      }.provide(testLayer)
    ),
    suite("retrieveAll")(
      test("should return empty vector when no employees exist") {
        for {
          service <- ZIO.service[EmployeeService]
          employees <- service.retrieveAll
        } yield assertTrue(employees.isEmpty)
      }.provide(testLayer),
      test("should return all employees") {
        val department = Department(DepartmentName("Engineering"))
        val employee1 = Employee(
          name = EmployeeName("Alice"),
          age = Age(28),
          departmentId = 1.refineUnsafe[DepartmentIdDescription]
        )
        val employee2 = Employee(
          name = EmployeeName("Bob"),
          age = Age(32),
          departmentId = 1.refineUnsafe[DepartmentIdDescription]
        )

        for {
          service <- ZIO.service[EmployeeService]
          _ <- service.create(employee1)
          _ <- service.create(employee2)
          employees <- service.retrieveAll
        } yield assertTrue(
          employees.size == 2,
          employees.exists(_.name == employee1.name),
          employees.exists(_.name == employee2.name)
        )
      }.provide(
        InMemoryEmployeeRepository.layer,
        InMemoryDepartmentRepository.withDepartments(
          1.refineUnsafe[DepartmentIdDescription] -> Department(
            DepartmentName("Engineering")
          )
        ),
        EmployeeServiceLive.layer
      )
    ),
    suite("retrieveById")(
      test("should retrieve employee by id") {
        val employee = Employee(
          name = EmployeeName("Charlie"),
          age = Age(35),
          departmentId = 1.refineUnsafe[DepartmentIdDescription]
        )

        for {
          service <- ZIO.service[EmployeeService]
          employeeId <- service.create(employee)
          retrieved <- service.retrieveById(employeeId)
        } yield assertTrue(
          retrieved.name == employee.name,
          retrieved.age == employee.age
        )
      }.provide(
        InMemoryEmployeeRepository.layer,
        InMemoryDepartmentRepository.withDepartments(
          1.refineUnsafe[DepartmentIdDescription] -> Department(
            DepartmentName("Engineering")
          )
        ),
        EmployeeServiceLive.layer
      ),
      test("should fail when employee does not exist") {
        for {
          service <- ZIO.service[EmployeeService]
          result <- service
            .retrieveById(999.refineUnsafe[EmployeeIdDescription])
            .exit
        } yield assertTrue(result.isFailure) && assertTrue(
          result match {
            case Exit.Failure(cause) =>
              cause.failureOption.contains(EmployeeNotFound)
            case _ => false
          }
        )
      }.provide(testLayer)
    ),
    suite("update")(
      test("should update existing employee") {
        val originalEmployee = Employee(
          name = EmployeeName("David"),
          age = Age(40),
          departmentId = 1.refineUnsafe[DepartmentIdDescription]
        )
        val updatedEmployee = Employee(
          name = EmployeeName("DavidUpdated"),
          age = Age(41),
          departmentId = 1.refineUnsafe[DepartmentIdDescription]
        )

        for {
          service <- ZIO.service[EmployeeService]
          employeeId <- service.create(originalEmployee)
          _ <- service.update(employeeId, updatedEmployee)
          retrieved <- service.retrieveById(employeeId)
        } yield assertTrue(
          retrieved.name == updatedEmployee.name,
          retrieved.age == updatedEmployee.age
        )
      }.provide(
        InMemoryEmployeeRepository.layer,
        InMemoryDepartmentRepository.withDepartments(
          1.refineUnsafe[DepartmentIdDescription] -> Department(
            DepartmentName("Engineering")
          )
        ),
        EmployeeServiceLive.layer
      ),
      test("should fail when updating non-existent employee") {
        val employee = Employee(
          name = EmployeeName("Eve"),
          age = Age(29),
          departmentId = 1.refineUnsafe[DepartmentIdDescription]
        )

        for {
          service <- ZIO.service[EmployeeService]
          result <- service
            .update(999.refineUnsafe[EmployeeIdDescription], employee)
            .exit
        } yield assertTrue(result.isFailure) && assertTrue(
          result match {
            case Exit.Failure(cause) =>
              cause.failureOption.contains(EmployeeNotFound)
            case _ => false
          }
        )
      }.provide(testLayer)
    ),
    suite("delete")(
      test("should delete existing employee") {
        val employee = Employee(
          name = EmployeeName("Frank"),
          age = Age(45),
          departmentId = 1.refineUnsafe[DepartmentIdDescription]
        )

        for {
          service <- ZIO.service[EmployeeService]
          employeeId <- service.create(employee)
          _ <- service.delete(employeeId)
          result <- service.retrieveById(employeeId).exit
        } yield assertTrue(result.isFailure) && assertTrue(
          result match {
            case Exit.Failure(cause) =>
              cause.failureOption.contains(EmployeeNotFound)
            case _ => false
          }
        )
      }.provide(
        InMemoryEmployeeRepository.layer,
        InMemoryDepartmentRepository.withDepartments(
          1.refineUnsafe[DepartmentIdDescription] -> Department(
            DepartmentName("Engineering")
          )
        ),
        EmployeeServiceLive.layer
      ),
      test("should succeed when deleting non-existent employee") {
        for {
          service <- ZIO.service[EmployeeService]
          _ <- service.delete(999.refineUnsafe[EmployeeIdDescription])
        } yield assertTrue(true)
      }.provide(testLayer)
    )
  )
}
