package com.example.repository

import com.augustnagro.magnum.magzio.Transactor
import com.example.domain.Age
import com.example.domain.Department
import com.example.domain.Employee
import com.example.domain.EmployeeId
import com.example.repository.testutils.TestContainerSupport
import io.github.iltotore.iron.*
import zio.*
import zio.test.*

object EmployeeRepositoryLiveSpec extends ZIOSpecDefault {

  val testLayer
    : ZLayer[Any, Throwable, Transactor & EmployeeRepository & DepartmentRepository] =
    TestContainerSupport.testDbLayer >+> (EmployeeRepositoryLive.layer ++ DepartmentRepositoryLive.layer)

  def spec = tests.provideShared(testLayer) @@ TestAspect.sequential
  private val tests = suite("EmployeeRepositoryLiveSpec")(
    suite("create")(
      test("should create an employee and return its ID") {
        for {
          deptRepo <- ZIO.service[DepartmentRepository]
          empRepo <- ZIO.service[EmployeeRepository]
          // Create a department first
          department = Department(name = "Engineering".refine)
          departmentId <- deptRepo.create(department)
          // Create employee
          employee = Employee(
            name = "John Doe".refine,
            age = Age(30),
            departmentId = departmentId
          )
          employeeId <- empRepo.create(employee)
          retrieved <- empRepo.retrieve(employeeId)
        } yield assertTrue(
          retrieved.isDefined,
          retrieved.get.name == employee.name,
          retrieved.get.age == employee.age,
          retrieved.get.departmentId == departmentId
        )
      }
    ),
    suite("retrieve")(
      test("should return None when employee does not exist") {
        for {
          empRepo <- ZIO.service[EmployeeRepository]
          retrieved <- empRepo.retrieve(EmployeeId(9999))
        } yield assertTrue(retrieved.isEmpty)
      },
      test("should retrieve an existing employee by ID") {
        for {
          deptRepo <- ZIO.service[DepartmentRepository]
          empRepo <- ZIO.service[EmployeeRepository]
          // Create a department first
          department = Department(name = "Sales".refine)
          departmentId <- deptRepo.create(department)
          // Create employee
          employee = Employee(
            name = "Jane Smith".refine,
            age = Age(28),
            departmentId = departmentId
          )
          employeeId <- empRepo.create(employee)
          retrieved <- empRepo.retrieve(employeeId)
        } yield assertTrue(
          retrieved.isDefined,
          retrieved.get.name == employee.name
        )
      }
    ),
    suite("retrieveAll")(
      test("should return empty vector when no employees exist") {
        for {
          empRepo <- ZIO.service[EmployeeRepository]
          all <- empRepo.retrieveAll
        } yield assertTrue(all.isEmpty)
      },
      test("should return all employees") {
        for {
          deptRepo <- ZIO.service[DepartmentRepository]
          empRepo <- ZIO.service[EmployeeRepository]
          // Create departments
          dept1Id <- deptRepo.create(Department(name = "IT".refine))
          dept2Id <- deptRepo.create(Department(name = "HR".refine))
          // Create employees
          emp1 = Employee(
            name = "Alice".refine,
            age = Age(25),
            departmentId = dept1Id
          )
          emp2 = Employee(
            name = "Bob".refine,
            age = Age(35),
            departmentId = dept2Id
          )
          _ <- empRepo.create(emp1)
          _ <- empRepo.create(emp2)
          all <- empRepo.retrieveAll
        } yield assertTrue(
          all.size == 2,
          all.map(_.name).toSet == Set(emp1.name, emp2.name)
        )
      }
    ),
    suite("update")(
      test("should update an existing employee") {
        for {
          deptRepo <- ZIO.service[DepartmentRepository]
          empRepo <- ZIO.service[EmployeeRepository]
          // Create department
          departmentId <- deptRepo.create(Department(name = "Legal".refine))
          // Create employee
          employee = Employee(
            name = "Charlie Brown".refine,
            age = Age(40),
            departmentId = departmentId
          )
          employeeId <- empRepo.create(employee)
          // Update employee
          updatedEmployee = Employee(
            name = "Charlie B. Brown".refine,
            age = Age(41),
            departmentId = departmentId
          )
          _ <- empRepo.update(employeeId, updatedEmployee)
          retrieved <- empRepo.retrieve(employeeId)
        } yield assertTrue(
          retrieved.isDefined,
          retrieved.get.name == updatedEmployee.name,
          retrieved.get.age == updatedEmployee.age
        )
      },
      test("should handle update of non-existent employee gracefully") {
        for {
          deptRepo <- ZIO.service[DepartmentRepository]
          empRepo <- ZIO.service[EmployeeRepository]
          departmentId <- deptRepo.create(Department(name = "Marketing".refine))
          employee = Employee(
            name = "Ghost Employee".refine,
            age = Age(99),
            departmentId = departmentId
          )
          _ <- empRepo.update(EmployeeId(9999), employee)
          retrieved <- empRepo.retrieve(EmployeeId(9999))
        } yield assertTrue(retrieved.isEmpty)
      }
    ),
    suite("delete")(
      test("should delete an existing employee") {
        for {
          deptRepo <- ZIO.service[DepartmentRepository]
          empRepo <- ZIO.service[EmployeeRepository]
          // Create department and employee
          departmentId <- deptRepo.create(Department(name = "Finance".refine))
          employee = Employee(
            name = "Diana Prince".refine,
            age = Age(32),
            departmentId = departmentId
          )
          employeeId <- empRepo.create(employee)
          _ <- empRepo.delete(employeeId)
          retrieved <- empRepo.retrieve(employeeId)
        } yield assertTrue(retrieved.isEmpty)
      },
      test("should be idempotent when deleting non-existent employee") {
        for {
          empRepo <- ZIO.service[EmployeeRepository]
          _ <- empRepo.delete(EmployeeId(9999))
          _ <- empRepo.delete(EmployeeId(9999))
        } yield assertTrue(true)
      }
    ),
    suite("foreign key constraints")(
      test("should cascade delete employees when department is deleted") {
        for {
          deptRepo <- ZIO.service[DepartmentRepository]
          empRepo <- ZIO.service[EmployeeRepository]
          // Create department and employees
          departmentId <- deptRepo.create(
            Department(name = "Operations".refine)
          )
          emp1Id <- empRepo.create(
            Employee(
              name = "Employee One".refine,
              age = Age(30),
              departmentId = departmentId
            )
          )
          emp2Id <- empRepo.create(
            Employee(
              name = "Employee Two".refine,
              age = Age(35),
              departmentId = departmentId
            )
          )
          // Delete the department
          _ <- deptRepo.delete(departmentId)
          // Employees should also be deleted due to CASCADE
          emp1Retrieved <- empRepo.retrieve(emp1Id)
          emp2Retrieved <- empRepo.retrieve(emp2Id)
        } yield assertTrue(
          emp1Retrieved.isEmpty,
          emp2Retrieved.isEmpty
        )
      }
    )
  ) @@ TestContainerSupport.cleanDb
}
