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

  def spec = tests.provideShared(testLayer) @@ TestAspect.sequential

  private val tests: Spec[Transactor & DepartmentRepository, Throwable] =
    suite("DepartmentRepositoryLiveSpec")(
      suite("create")(
        test("should create a department and return its ID") {
          for {
            repo <- ZIO.service[DepartmentRepository]
            department = Department(name = "Engineering".refineUnsafe)
            departmentId <- repo.create(department)
            retrieved <- repo.retrieve(departmentId)
          } yield assertTrue(
            retrieved.isDefined,
            retrieved.get.name == department.name
          )
        }
      ),
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
            department = Department(name = "Sales".refineUnsafe)
            departmentId <- repo.create(department)
            retrieved <- repo.retrieve(departmentId)
          } yield assertTrue(
            retrieved.isDefined,
            retrieved.get.name == department.name
          )
        }
      ),
      suite("retrieveByName")(
        test("should return None when department with name does not exist") {
          for {
            repo <- ZIO.service[DepartmentRepository]
            retrieved <- repo.retrieveByName("NonExistent".refineUnsafe)
          } yield assertTrue(retrieved.isEmpty)
        },
        test("should retrieve a department by name") {
          for {
            repo <- ZIO.service[DepartmentRepository]
            department = Department(name = "Marketing".refineUnsafe)
            departmentId <- repo.create(department)
            retrieved <- repo.retrieveByName(department.name)
          } yield assertTrue(
            retrieved.isDefined,
            retrieved.get.name == department.name
          )
        }
      ),
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
            dept1 = Department(name = "IT".refineUnsafe)
            dept2 = Department(name = "HR".refineUnsafe)
            _ <- repo.create(dept1)
            _ <- repo.create(dept2)
            all <- repo.retrieveAll
          } yield assertTrue(
            all.size == 2,
            all.map(_.name).toSet == Set(dept1.name, dept2.name)
          )
        }
      ),
      suite("update")(
        test("should update an existing department") {
          for {
            repo <- ZIO.service[DepartmentRepository]
            department = Department(name = "Legal".refineUnsafe)
            departmentId <- repo.create(department)
            updatedDepartment = Department(name = "LegalAffairs".refineUnsafe)
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
            department = Department(name = "GhostDepartment".refineUnsafe)
            _ <- repo.update(DepartmentId(9999), department)
            retrieved <- repo.retrieve(DepartmentId(9999))
          } yield assertTrue(retrieved.isEmpty)
        }
      ),
      suite("delete")(
        test("should delete an existing department") {
          for {
            repo <- ZIO.service[DepartmentRepository]
            department = Department(name = "Accounting".refineUnsafe)
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
      )
    ) @@ TestContainerSupport.cleanDb
}
