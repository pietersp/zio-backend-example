package com.example.service

import com.example.domain.{Department, DepartmentId, DepartmentIdDescription, DepartmentName}
import com.example.error.AppError.{DepartmentAlreadyExists, DepartmentNotFound}
import com.example.repository.DepartmentRepository
import io.github.iltotore.iron.*
import zio.*
import zio.test.*
import zio.test.Assertion.*

object DepartmentServiceSpec extends ZIOSpecDefault {

  // Mock repository for testing
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

    override def update(departmentId: DepartmentId, department: Department): UIO[Unit] =
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
  }

  val testLayer: ZLayer[Any, Nothing, DepartmentService] =
    InMemoryDepartmentRepository.layer >>> DepartmentServiceLive.layer

  def spec = suite("DepartmentServiceSpec")(
    suite("create")(
      test("should create a new department") {
        val department = Department(DepartmentName("Engineering"))

        for {
          service <- ZIO.service[DepartmentService]
          departmentId <- service.create(department)
          retrieved <- service.retrieveById(departmentId)
        } yield assertTrue(
          retrieved.name == department.name
        )
      }.provide(testLayer),

      test("should fail when creating duplicate department") {
        val department = Department(DepartmentName("Engineering"))

        for {
          service <- ZIO.service[DepartmentService]
          _ <- service.create(department)
          result <- service.create(department).exit
        } yield assertTrue(result.isFailure) && assertTrue(
          result match {
            case Exit.Failure(cause) => cause.failureOption.contains(DepartmentAlreadyExists)
            case _ => false
          }
        )
      }.provide(testLayer),

      test("should allow creating departments with different names") {
        val dept1 = Department(DepartmentName("Engineering"))
        val dept2 = Department(DepartmentName("Marketing"))

        for {
          service <- ZIO.service[DepartmentService]
          id1 <- service.create(dept1)
          id2 <- service.create(dept2)
        } yield assertTrue(id1 != id2)
      }.provide(testLayer)
    ),

    suite("retrieveAll")(
      test("should return empty vector when no departments exist") {
        for {
          service <- ZIO.service[DepartmentService]
          departments <- service.retrieveAll
        } yield assertTrue(departments.isEmpty)
      }.provide(testLayer),

      test("should return all created departments") {
        val dept1 = Department(DepartmentName("Engineering"))
        val dept2 = Department(DepartmentName("Marketing"))
        val dept3 = Department(DepartmentName("Sales"))

        for {
          service <- ZIO.service[DepartmentService]
          _ <- service.create(dept1)
          _ <- service.create(dept2)
          _ <- service.create(dept3)
          departments <- service.retrieveAll
        } yield assertTrue(
          departments.size == 3,
          departments.exists(_.name == dept1.name),
          departments.exists(_.name == dept2.name),
          departments.exists(_.name == dept3.name)
        )
      }.provide(testLayer)
    ),

    suite("retrieveById")(
      test("should retrieve department by id") {
        val department = Department(DepartmentName("HR"))

        for {
          service <- ZIO.service[DepartmentService]
          departmentId <- service.create(department)
          retrieved <- service.retrieveById(departmentId)
        } yield assertTrue(retrieved.name == department.name)
      }.provide(testLayer),

      test("should fail when department does not exist") {
        for {
          service <- ZIO.service[DepartmentService]
          result <- service.retrieveById(999.refineUnsafe[DepartmentIdDescription]).exit
        } yield assertTrue(result.isFailure) && assertTrue(
          result match {
            case Exit.Failure(cause) => cause.failureOption.contains(DepartmentNotFound)
            case _ => false
          }
        )
      }.provide(testLayer)
    ),

    suite("update")(
      test("should update existing department") {
        val original = Department(DepartmentName("Engineering"))
        val updated = Department(DepartmentName("SoftwareEngineering"))

        for {
          service <- ZIO.service[DepartmentService]
          departmentId <- service.create(original)
          _ <- service.update(departmentId, updated)
          retrieved <- service.retrieveById(departmentId)
        } yield assertTrue(retrieved.name == updated.name)
      }.provide(testLayer),

      test("should fail when updating non-existent department") {
        val department = Department(DepartmentName("Nonexistent"))

        for {
          service <- ZIO.service[DepartmentService]
          result <- service.update(999.refineUnsafe[DepartmentIdDescription], department).exit
        } yield assertTrue(result.isFailure) && assertTrue(
          result match {
            case Exit.Failure(cause) => cause.failureOption.contains(DepartmentNotFound)
            case _ => false
          }
        )
      }.provide(testLayer)
    ),

    suite("delete")(
      test("should delete existing department") {
        val department = Department(DepartmentName("Temporary"))

        for {
          service <- ZIO.service[DepartmentService]
          departmentId <- service.create(department)
          _ <- service.delete(departmentId)
          result <- service.retrieveById(departmentId).exit
        } yield assertTrue(result.isFailure) && assertTrue(
          result match {
            case Exit.Failure(cause) => cause.failureOption.contains(DepartmentNotFound)
            case _ => false
          }
        )
      }.provide(testLayer),

      test("should succeed when deleting non-existent department") {
        for {
          service <- ZIO.service[DepartmentService]
          _ <- service.delete(999.refineUnsafe[DepartmentIdDescription])
        } yield assertTrue(true)
      }.provide(testLayer),

      test("should not affect other departments when deleting one") {
        val dept1 = Department(DepartmentName("Keep1"))
        val dept2 = Department(DepartmentName("Delete"))
        val dept3 = Department(DepartmentName("Keep2"))

        for {
          service <- ZIO.service[DepartmentService]
          id1 <- service.create(dept1)
          id2 <- service.create(dept2)
          id3 <- service.create(dept3)
          _ <- service.delete(id2)
          remaining <- service.retrieveAll
        } yield assertTrue(
          remaining.size == 2,
          remaining.exists(_.name == dept1.name),
          remaining.exists(_.name == dept3.name),
          !remaining.exists(_.name == dept2.name)
        )
      }.provide(testLayer)
    )
  )
}
