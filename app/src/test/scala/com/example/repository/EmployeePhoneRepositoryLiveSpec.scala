package com.example.repository

import com.augustnagro.magnum.magzio.Transactor
import com.example.domain.{Age, Department, Employee, Phone}
import com.example.repository.testutils.TestContainerSupport
import io.github.iltotore.iron.*
import zio.*
import zio.test.*

object EmployeePhoneRepositoryLiveSpec extends ZIOSpecDefault {

  val testLayer: ZLayer[
    Any,
    Throwable,
    Transactor & EmployeePhoneRepository & PhoneRepository &
      EmployeeRepository & DepartmentRepository
  ] =
    TestContainerSupport.testDbLayer >+> (
      EmployeePhoneRepositoryLive.layer ++
        PhoneRepositoryLive.layer ++
        EmployeeRepositoryLive.layer ++
        DepartmentRepositoryLive.layer
    )

  def spec = tests.provideShared(testLayer) @@ TestAspect.sequential

  private val tests = suite("EmployeePhoneRepositoryLiveSpec")(
    suite("addPhoneToEmployee")(
      test("should add a phone to an employee") {
        for {
          deptRepo <- ZIO.service[DepartmentRepository]
          empRepo <- ZIO.service[EmployeeRepository]
          phoneRepo <- ZIO.service[PhoneRepository]
          empPhoneRepo <- ZIO.service[EmployeePhoneRepository]
          // Create department, employee, and phone
          departmentId <- deptRepo.create(
            Department(name = "Engineering".refineUnsafe)
          )
          employeeId <- empRepo.create(
            Employee(
              name = "John Doe".refineUnsafe,
              age = Age(30),
              departmentId = departmentId
            )
          )
          phoneId <- phoneRepo.create(Phone(number = "1234567890".refineUnsafe))
          // Add phone to employee
          _ <- empPhoneRepo.addPhoneToEmployee(phoneId, employeeId)
          phones <- empPhoneRepo.retrieveEmployeePhones(employeeId)
          phone <- phoneRepo.retrieve(phoneId)
        } yield assertTrue(
          phones.size == 1,
          phones.head.number == phone.get.number
        )
      }
    ),
    suite("retrieveEmployeePhones")(
      test("should return empty vector when employee has no phones") {
        for {
          deptRepo <- ZIO.service[DepartmentRepository]
          empRepo <- ZIO.service[EmployeeRepository]
          empPhoneRepo <- ZIO.service[EmployeePhoneRepository]
          // Create department and employee without phones
          departmentId <- deptRepo.create(
            Department(name = "Sales".refineUnsafe)
          )
          employeeId <- empRepo.create(
            Employee(
              name = "Jane Smith".refineUnsafe,
              age = Age(28),
              departmentId = departmentId
            )
          )
          phones <- empPhoneRepo.retrieveEmployeePhones(employeeId)
        } yield assertTrue(phones.isEmpty)
      },
      test("should retrieve multiple phones for an employee") {
        for {
          deptRepo <- ZIO.service[DepartmentRepository]
          empRepo <- ZIO.service[EmployeeRepository]
          phoneRepo <- ZIO.service[PhoneRepository]
          empPhoneRepo <- ZIO.service[EmployeePhoneRepository]
          // Create department, employee, and multiple phones
          departmentId <- deptRepo.create(Department(name = "IT".refineUnsafe))
          employeeId <- empRepo.create(
            Employee(
              name = "Alice Johnson".refineUnsafe,
              age = Age(35),
              departmentId = departmentId
            )
          )
          phone1Id <- phoneRepo.create(
            Phone(number = "1111111111".refineUnsafe)
          )
          phone2Id <- phoneRepo.create(
            Phone(number = "2222222222".refineUnsafe)
          )
          phone3Id <- phoneRepo.create(
            Phone(number = "3333333333".refineUnsafe)
          )
          // Add phones to employee
          _ <- empPhoneRepo.addPhoneToEmployee(phone1Id, employeeId)
          _ <- empPhoneRepo.addPhoneToEmployee(phone2Id, employeeId)
          _ <- empPhoneRepo.addPhoneToEmployee(phone3Id, employeeId)
          phones <- empPhoneRepo.retrieveEmployeePhones(employeeId)
          phone1 <- phoneRepo.retrieve(phone1Id)
          phone2 <- phoneRepo.retrieve(phone2Id)
          phone3 <- phoneRepo.retrieve(phone3Id)
        } yield assertTrue(
          phones.size == 3,
          phones.map(_.number).toSet == Set(
            phone1.get.number,
            phone2.get.number,
            phone3.get.number
          )
        )
      }
    ),
    suite("removePhoneFromEmployee")(
      test("should remove a phone from an employee") {
        for {
          deptRepo <- ZIO.service[DepartmentRepository]
          empRepo <- ZIO.service[EmployeeRepository]
          phoneRepo <- ZIO.service[PhoneRepository]
          empPhoneRepo <- ZIO.service[EmployeePhoneRepository]
          // Create department, employee, and phones
          departmentId <- deptRepo.create(Department(name = "HR".refineUnsafe))
          employeeId <- empRepo.create(
            Employee(
              name = "Bob Williams".refineUnsafe,
              age = Age(40),
              departmentId = departmentId
            )
          )
          phone1Id <- phoneRepo.create(
            Phone(number = "4444444444".refineUnsafe)
          )
          phone2Id <- phoneRepo.create(
            Phone(number = "5555555555".refineUnsafe)
          )
          // Add phones to employee
          _ <- empPhoneRepo.addPhoneToEmployee(phone1Id, employeeId)
          _ <- empPhoneRepo.addPhoneToEmployee(phone2Id, employeeId)
          // Remove one phone
          _ <- empPhoneRepo.removePhoneFromEmployee(phone1Id, employeeId)
          phones <- empPhoneRepo.retrieveEmployeePhones(employeeId)
          phone2 <- phoneRepo.retrieve(phone2Id)
        } yield assertTrue(
          phones.size == 1,
          phones.head.number == phone2.get.number
        )
      },
      test("should be idempotent when removing non-existent association") {
        for {
          deptRepo <- ZIO.service[DepartmentRepository]
          empRepo <- ZIO.service[EmployeeRepository]
          phoneRepo <- ZIO.service[PhoneRepository]
          empPhoneRepo <- ZIO.service[EmployeePhoneRepository]
          // Create department, employee, and phone
          departmentId <- deptRepo.create(
            Department(name = "Legal".refineUnsafe)
          )
          employeeId <- empRepo.create(
            Employee(
              name = "Charlie Brown".refineUnsafe,
              age = Age(32),
              departmentId = departmentId
            )
          )
          phoneId <- phoneRepo.create(Phone(number = "6666666666".refineUnsafe))
          // Remove phone that was never added
          _ <- empPhoneRepo.removePhoneFromEmployee(phoneId, employeeId)
          _ <- empPhoneRepo.removePhoneFromEmployee(phoneId, employeeId)
        } yield assertTrue(true)
      }
    ),
    suite("cascade delete scenarios")(
      test("should remove associations when employee is deleted") {
        for {
          deptRepo <- ZIO.service[DepartmentRepository]
          empRepo <- ZIO.service[EmployeeRepository]
          phoneRepo <- ZIO.service[PhoneRepository]
          empPhoneRepo <- ZIO.service[EmployeePhoneRepository]
          // Create department, employee, and phone
          departmentId <- deptRepo.create(
            Department(name = "Marketing".refineUnsafe)
          )
          employeeId <- empRepo.create(
            Employee(
              name = "Diana Prince".refineUnsafe,
              age = Age(29),
              departmentId = departmentId
            )
          )
          phoneId <- phoneRepo.create(Phone(number = "7777777777".refineUnsafe))
          // Add phone to employee
          _ <- empPhoneRepo.addPhoneToEmployee(phoneId, employeeId)
          // Delete employee
          _ <- empRepo.delete(employeeId)
          // Phone should still exist, but association should be gone
          phone <- phoneRepo.retrieve(phoneId)
          phones <- empPhoneRepo.retrieveEmployeePhones(employeeId)
        } yield assertTrue(
          phone.isDefined,
          phones.isEmpty
        )
      },
      test("should remove associations when phone is deleted") {
        for {
          deptRepo <- ZIO.service[DepartmentRepository]
          empRepo <- ZIO.service[EmployeeRepository]
          phoneRepo <- ZIO.service[PhoneRepository]
          empPhoneRepo <- ZIO.service[EmployeePhoneRepository]
          // Create department, employee, and phones
          departmentId <- deptRepo.create(
            Department(name = "Finance".refineUnsafe)
          )
          employeeId <- empRepo.create(
            Employee(
              name = "Eve Davis".refineUnsafe,
              age = Age(33),
              departmentId = departmentId
            )
          )
          phone1Id <- phoneRepo.create(
            Phone(number = "8888888888".refineUnsafe)
          )
          phone2Id <- phoneRepo.create(
            Phone(number = "9999999999".refineUnsafe)
          )
          // Add phones to employee
          _ <- empPhoneRepo.addPhoneToEmployee(phone1Id, employeeId)
          _ <- empPhoneRepo.addPhoneToEmployee(phone2Id, employeeId)
          // Delete one phone
          _ <- phoneRepo.delete(phone1Id)
          // Employee should still exist with only one phone
          phones <- empPhoneRepo.retrieveEmployeePhones(employeeId)
          phone2 <- phoneRepo.retrieve(phone2Id)
        } yield assertTrue(
          phones.size == 1,
          phones.head.number == phone2.get.number
        )
      },
      test(
        "should cascade delete all associations when department is deleted"
      ) {
        for {
          deptRepo <- ZIO.service[DepartmentRepository]
          empRepo <- ZIO.service[EmployeeRepository]
          phoneRepo <- ZIO.service[PhoneRepository]
          empPhoneRepo <- ZIO.service[EmployeePhoneRepository]
          // Create department, employee, and phone
          departmentId <- deptRepo.create(
            Department(name = "Operations".refineUnsafe)
          )
          employeeId <- empRepo.create(
            Employee(
              name = "Frank Miller".refineUnsafe,
              age = Age(45),
              departmentId = departmentId
            )
          )
          phoneId <- phoneRepo.create(Phone(number = "1010101010".refineUnsafe))
          // Add phone to employee
          _ <- empPhoneRepo.addPhoneToEmployee(phoneId, employeeId)
          // Delete department (should cascade to employee and then to employee_phone)
          _ <- deptRepo.delete(departmentId)
          // Phone should still exist, but employee and associations should be gone
          phone <- phoneRepo.retrieve(phoneId)
          employee <- empRepo.retrieve(employeeId)
          phones <- empPhoneRepo.retrieveEmployeePhones(employeeId)
        } yield assertTrue(
          phone.isDefined,
          employee.isEmpty,
          phones.isEmpty
        )
      }
    )
  ) @@ TestContainerSupport.cleanDb
}
