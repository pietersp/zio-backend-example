package com.example.repository

import com.augustnagro.magnum.magzio.Transactor
import com.example.domain.Department
import com.example.domain.DepartmentId
import com.example.repository.testutils.TestContainerSupport
import io.github.iltotore.iron.*
import zio.*
import zio.test.*

object DepartmentRepositoryLiveSpec extends ZIOSpecDefault {

  val testLayer: ZLayer[Any, Throwable, DepartmentRepository & Transactor] =
    TestContainerSupport.testDbLayer >+> DepartmentRepositoryLive.layer

  def spec = suite("DepartmentRepositoryLiveSpec")(
    suite("create")(
      test("should create a department and return its ID") {
        for {
          repo <- ZIO.service[DepartmentRepository]
          department = Department(name = "Engineering".refine)
          departmentId <- repo.create(department)
          retrieved <- repo.retrieve(departmentId)
        } yield assertTrue(
          retrieved.isDefined,
          retrieved.get.name == department.name
        )
      }
    ) @@ TestContainerSupport.cleanDb,
    suite("retrieve")(
      test("should return None when department does not exist") {
        for {
          repo <- ZIO.service[DepartmentRepository]
          retrieved <- repo.retrieve(DepartmentId(9999))
        } yield assertTrue(retrieved.isEmpty)
      },
      test("should retrieve an existing department by ID") {
        for {
          repo <- ZIO.service[DepartmentRepository]
          department = Department(name = "Sales".refine)
          departmentId <- repo.create(department)
          retrieved <- repo.retrieve(departmentId)
        } yield assertTrue(
          retrieved.isDefined,
          retrieved.get.name == department.name
        )
      }
    ) @@ TestContainerSupport.cleanDb,
    suite("retrieveByName")(
      test("should return None when department with name does not exist") {
        for {
          repo <- ZIO.service[DepartmentRepository]
          retrieved <- repo.retrieveByName("NonExistent".refine)
        } yield assertTrue(retrieved.isEmpty)
      },
      test("should retrieve a department by name") {
        for {
          repo <- ZIO.service[DepartmentRepository]
          department = Department(name = "Marketing".refine)
          departmentId <- repo.create(department)
          retrieved <- repo.retrieveByName(department.name)
        } yield assertTrue(
          retrieved.isDefined,
          retrieved.get.name == department.name
        )
      }
    ) @@ TestContainerSupport.cleanDb,
    suite("retrieveAll")(
      test("should return empty vector when no departments exist") {
        for {
          repo <- ZIO.service[DepartmentRepository]
          all <- repo.retrieveAll
        } yield assertTrue(all.isEmpty)
      },
      test("should return all departments") {
        for {
          repo <- ZIO.service[DepartmentRepository]
          dept1 = Department(name = "IT".refine)
          dept2 = Department(name = "HR".refine)
          _ <- repo.create(dept1)
          _ <- repo.create(dept2)
          all <- repo.retrieveAll
        } yield assertTrue(
          all.size == 2,
          all.map(_.name).toSet == Set(dept1.name, dept2.name)
        )
      }
    ) @@ TestContainerSupport.cleanDb,
    suite("update")(
      test("should update an existing department") {
        for {
          repo <- ZIO.service[DepartmentRepository]
          department = Department(name = "Legal".refine)
          departmentId <- repo.create(department)
          updatedDepartment = Department(name = "LegalAffairs".refine)
          _ <- repo.update(departmentId, updatedDepartment)
          retrieved <- repo.retrieve(departmentId)
        } yield assertTrue(
          retrieved.isDefined,
          retrieved.get.name == updatedDepartment.name
        )
      },
      test("should handle update of non-existent department gracefully") {
        for {
          repo <- ZIO.service[DepartmentRepository]
          department = Department(name = "GhostDepartment".refine)
          _ <- repo.update(DepartmentId(9999), department)
          retrieved <- repo.retrieve(DepartmentId(9999))
        } yield assertTrue(retrieved.isEmpty)
      }
    ) @@ TestContainerSupport.cleanDb,
    suite("delete")(
      test("should delete an existing department") {
        for {
          repo <- ZIO.service[DepartmentRepository]
          department = Department(name = "Accounting".refine)
          departmentId <- repo.create(department)
          _ <- repo.delete(departmentId)
          retrieved <- repo.retrieve(departmentId)
        } yield assertTrue(retrieved.isEmpty)
      },
      test("should be idempotent when deleting non-existent department") {
        for {
          repo <- ZIO.service[DepartmentRepository]
          _ <- repo.delete(DepartmentId(9999))
          _ <- repo.delete(DepartmentId(9999))
        } yield assertTrue(true)
      }
    ) @@ TestContainerSupport.cleanDb
  ).provideShared(testLayer) @@ TestAspect.sequential
}
