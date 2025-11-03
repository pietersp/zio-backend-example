package com.example.service

import com.example.domain.*
import com.example.error.AppError.{EmployeeNotFound, PhoneNotFound}
import com.example.repository.{
  EmployeePhoneRepository,
  EmployeeRepository,
  PhoneRepository
}
import io.github.iltotore.iron.*
import zio.*
import zio.test.*

object EmployeePhoneServiceSpec extends ZIOSpecDefault {

  // In-memory implementation of EmployeeRepository for testing
  case class InMemoryEmployeeRepository(
    employees: Ref[Map[EmployeeId, Employee]]
  ) extends EmployeeRepository {

    override def create(employee: Employee): UIO[EmployeeId] =
      for {
        id <- employees.get.map(_.keys.maxOption.getOrElse(0) + 1)
        employeeId = id.refineUnsafe[EmployeeIdDescription]
        _ <- employees.update(_ + (employeeId -> employee))
      } yield employeeId

    override def retrieve(employeeId: EmployeeId): UIO[Option[Employee]] =
      employees.get.map(_.get(employeeId))

    override def retrieveAll: UIO[Vector[Employee]] =
      employees.get.map(_.values.toVector)

    override def update(employeeId: EmployeeId, employee: Employee): UIO[Unit] =
      employees.update(_ + (employeeId -> employee))

    override def delete(employeeId: EmployeeId): UIO[Unit] =
      employees.update(_ - employeeId)
  }

  object InMemoryEmployeeRepository {
    val layer: ZLayer[Any, Nothing, EmployeeRepository] =
      ZLayer {
        Ref
          .make(Map.empty[EmployeeId, Employee])
          .map(InMemoryEmployeeRepository(_))
      }

    def withEmployees(
      employees: (EmployeeId, Employee)*
    ): ZLayer[Any, Nothing, EmployeeRepository] =
      ZLayer {
        Ref.make(employees.toMap).map(InMemoryEmployeeRepository(_))
      }
  }

  // In-memory implementation of PhoneRepository for testing
  case class InMemoryPhoneRepository(
    phones: Ref[Map[PhoneId, Phone]]
  ) extends PhoneRepository {

    override def create(phone: Phone): UIO[PhoneId] =
      for {
        id <- phones.get.map(_.keys.maxOption.getOrElse(0) + 1)
        phoneId = id.refineUnsafe[PhoneIdDescription]
        _ <- phones.update(_ + (phoneId -> phone))
      } yield phoneId

    override def retrieve(phoneId: PhoneId): UIO[Option[Phone]] =
      phones.get.map(_.get(phoneId))

    override def retrieveByNumber(
      phoneNumber: PhoneNumber
    ): UIO[Option[Phone]] =
      phones.get.map(_.values.find(_.number == phoneNumber))

    override def update(phoneId: PhoneId, phone: Phone): UIO[Unit] =
      phones.update(_ + (phoneId -> phone))

    override def delete(phoneId: PhoneId): UIO[Unit] =
      phones.update(_ - phoneId)
  }

  object InMemoryPhoneRepository {
    val layer: ZLayer[Any, Nothing, PhoneRepository] =
      ZLayer {
        Ref.make(Map.empty[PhoneId, Phone]).map(InMemoryPhoneRepository(_))
      }

    def withPhones(
      phones: (PhoneId, Phone)*
    ): ZLayer[Any, Nothing, PhoneRepository] =
      ZLayer {
        Ref.make(phones.toMap).map(InMemoryPhoneRepository(_))
      }
  }

  // In-memory implementation of EmployeePhoneRepository for testing
  case class InMemoryEmployeePhoneRepository(
    employeePhones: Ref[Map[EmployeeId, Vector[PhoneId]]],
    phoneRepo: PhoneRepository
  ) extends EmployeePhoneRepository {

    override def addPhoneToEmployee(
      phoneId: PhoneId,
      employeeId: EmployeeId
    ): UIO[Unit] =
      employeePhones.update { map =>
        val currentPhones = map.getOrElse(employeeId, Vector.empty)
        map + (employeeId -> (currentPhones :+ phoneId).distinct)
      }

    override def retrieveEmployeePhones(
      employeeId: EmployeeId
    ): UIO[Vector[Phone]] =
      for {
        phoneIds <- employeePhones.get.map(
          _.getOrElse(employeeId, Vector.empty)
        )
        result <- ZIO.foreach(phoneIds) { phoneId =>
          phoneRepo.retrieve(phoneId).map(_.get) // Assumes phone exists
        }
      } yield result

    override def removePhoneFromEmployee(
      phoneId: PhoneId,
      employeeId: EmployeeId
    ): UIO[Unit] =
      employeePhones.update { map =>
        val currentPhones = map.getOrElse(employeeId, Vector.empty)
        map + (employeeId -> currentPhones.filterNot(_ == phoneId))
      }
  }

  object InMemoryEmployeePhoneRepository {
    val layer: ZLayer[PhoneRepository, Nothing, EmployeePhoneRepository] =
      ZLayer {
        for {
          phoneRepo <- ZIO.service[PhoneRepository]
          ref <- Ref.make(Map.empty[EmployeeId, Vector[PhoneId]])
        } yield InMemoryEmployeePhoneRepository(ref, phoneRepo)
      }

    def withEmployeePhones(
      employeePhones: (EmployeeId, Vector[PhoneId])*
    ): ZLayer[PhoneRepository, Nothing, EmployeePhoneRepository] =
      ZLayer {
        for {
          phoneRepo <- ZIO.service[PhoneRepository]
          ref <- Ref.make(employeePhones.toMap)
        } yield InMemoryEmployeePhoneRepository(ref, phoneRepo)
      }
  }

  // Test layer composition
  val testLayer: ZLayer[Any, Nothing, EmployeePhoneService] =
    InMemoryEmployeeRepository.layer ++
      InMemoryPhoneRepository.layer >+>
      InMemoryEmployeePhoneRepository.layer >>>
      EmployeePhoneServiceLive.layer

  def spec = suite("EmployeePhoneServiceSpec")(
    suite("addPhoneToEmployee")(
      test("should add phone when both employee and phone exist") {
        val employeeId = 1.refineUnsafe[EmployeeIdDescription]
        val phoneId = 1.refineUnsafe[PhoneIdDescription]

        for {
          service <- ZIO.service[EmployeePhoneService]
          _ <- service.addPhoneToEmployee(phoneId, employeeId)
          phones <- service.retrieveEmployeePhones(employeeId)
        } yield assertTrue(
          phones.length == 1,
          phones.head.number == PhoneNumber("1234567890")
        )
      }.provide(
        InMemoryEmployeeRepository.withEmployees(
          1.refineUnsafe[EmployeeIdDescription] -> Employee(
            EmployeeName("JohnDoe"),
            Age(30),
            1.refineUnsafe[DepartmentIdDescription]
          )
        ) ++
          InMemoryPhoneRepository.withPhones(
            1.refineUnsafe[PhoneIdDescription] -> Phone(
              PhoneNumber("1234567890")
            )
          ) >+>
          InMemoryEmployeePhoneRepository.layer >>>
          EmployeePhoneServiceLive.layer
      ),
      test("should fail when phone doesn't exist") {
        val employeeId = 1.refineUnsafe[EmployeeIdDescription]
        val phoneId = 999.refineUnsafe[PhoneIdDescription]

        for {
          service <- ZIO.service[EmployeePhoneService]
          result <- service.addPhoneToEmployee(phoneId, employeeId).exit
        } yield assertTrue(result.isFailure) && assertTrue(
          result match {
            case Exit.Failure(cause) =>
              cause.failureOption.contains(PhoneNotFound)
            case _ => false
          }
        )
      }.provide(
        InMemoryEmployeeRepository.withEmployees(
          1.refineUnsafe[EmployeeIdDescription] -> Employee(
            EmployeeName("JohnDoe"),
            Age(30),
            1.refineUnsafe[DepartmentIdDescription]
          )
        ) ++
          InMemoryPhoneRepository.layer >+>
          InMemoryEmployeePhoneRepository.layer >>>
          EmployeePhoneServiceLive.layer
      ),
      test("should fail when employee doesn't exist") {
        val employeeId = 999.refineUnsafe[EmployeeIdDescription]
        val phoneId = 1.refineUnsafe[PhoneIdDescription]

        for {
          service <- ZIO.service[EmployeePhoneService]
          result <- service.addPhoneToEmployee(phoneId, employeeId).exit
        } yield assertTrue(result.isFailure) && assertTrue(
          result match {
            case Exit.Failure(cause) =>
              cause.failureOption.contains(EmployeeNotFound)
            case _ => false
          }
        )
      }.provide(
        InMemoryEmployeeRepository.layer ++
          InMemoryPhoneRepository.withPhones(
            1.refineUnsafe[PhoneIdDescription] -> Phone(
              PhoneNumber("1234567890")
            )
          ) >+>
          InMemoryEmployeePhoneRepository.layer >>>
          EmployeePhoneServiceLive.layer
      ),
      test("should allow adding multiple phones to same employee") {
        val employeeId = 1.refineUnsafe[EmployeeIdDescription]
        val phoneId1 = 1.refineUnsafe[PhoneIdDescription]
        val phoneId2 = 2.refineUnsafe[PhoneIdDescription]

        for {
          service <- ZIO.service[EmployeePhoneService]
          _ <- service.addPhoneToEmployee(phoneId1, employeeId)
          _ <- service.addPhoneToEmployee(phoneId2, employeeId)
          phones <- service.retrieveEmployeePhones(employeeId)
        } yield assertTrue(
          phones.length == 2,
          phones.exists(_.number == PhoneNumber("111111")),
          phones.exists(_.number == PhoneNumber("222222"))
        )
      }.provide(
        InMemoryEmployeeRepository.withEmployees(
          1.refineUnsafe[EmployeeIdDescription] -> Employee(
            EmployeeName("JohnDoe"),
            Age(30),
            1.refineUnsafe[DepartmentIdDescription]
          )
        ) ++
          InMemoryPhoneRepository.withPhones(
            1.refineUnsafe[PhoneIdDescription] -> Phone(PhoneNumber("111111")),
            2.refineUnsafe[PhoneIdDescription] -> Phone(PhoneNumber("222222"))
          ) >+>
          InMemoryEmployeePhoneRepository.layer >>>
          EmployeePhoneServiceLive.layer
      )
    ),
    suite("retrieveEmployeePhones")(
      test("should return empty vector when employee has no phones") {
        val employeeId = 1.refineUnsafe[EmployeeIdDescription]

        for {
          service <- ZIO.service[EmployeePhoneService]
          phones <- service.retrieveEmployeePhones(employeeId)
        } yield assertTrue(phones.isEmpty)
      }.provide(
        InMemoryEmployeeRepository.withEmployees(
          1.refineUnsafe[EmployeeIdDescription] -> Employee(
            EmployeeName("JohnDoe"),
            Age(30),
            1.refineUnsafe[DepartmentIdDescription]
          )
        ) ++
          InMemoryPhoneRepository.layer >+>
          InMemoryEmployeePhoneRepository.layer >>>
          EmployeePhoneServiceLive.layer
      ),
      test("should return all phones for an employee") {
        val employeeId = 1.refineUnsafe[EmployeeIdDescription]

        for {
          service <- ZIO.service[EmployeePhoneService]
          phones <- service.retrieveEmployeePhones(employeeId)
        } yield assertTrue(
          phones.length == 2,
          phones.exists(_.number == PhoneNumber("111111")),
          phones.exists(_.number == PhoneNumber("222222"))
        )
      }.provide(
        InMemoryEmployeeRepository.withEmployees(
          1.refineUnsafe[EmployeeIdDescription] -> Employee(
            EmployeeName("JohnDoe"),
            Age(30),
            1.refineUnsafe[DepartmentIdDescription]
          )
        ) ++
          InMemoryPhoneRepository.withPhones(
            1.refineUnsafe[PhoneIdDescription] -> Phone(PhoneNumber("111111")),
            2.refineUnsafe[PhoneIdDescription] -> Phone(PhoneNumber("222222"))
          ) >+>
          InMemoryEmployeePhoneRepository.withEmployeePhones(
            1.refineUnsafe[EmployeeIdDescription] -> Vector(
              1.refineUnsafe[PhoneIdDescription],
              2.refineUnsafe[PhoneIdDescription]
            )
          ) >>>
          EmployeePhoneServiceLive.layer
      ),
      test("should fail when employee doesn't exist") {
        val employeeId = 999.refineUnsafe[EmployeeIdDescription]

        for {
          service <- ZIO.service[EmployeePhoneService]
          result <- service.retrieveEmployeePhones(employeeId).exit
        } yield assertTrue(result.isFailure) && assertTrue(
          result match {
            case Exit.Failure(cause) =>
              cause.failureOption.contains(EmployeeNotFound)
            case _ => false
          }
        )
      }.provide(testLayer)
    ),
    suite("removePhoneFromEmployee")(
      test("should remove phone from employee successfully") {
        val employeeId = 1.refineUnsafe[EmployeeIdDescription]
        val phoneId = 1.refineUnsafe[PhoneIdDescription]

        for {
          service <- ZIO.service[EmployeePhoneService]
          phonesBefore <- service.retrieveEmployeePhones(employeeId)
          _ <- service.removePhoneFromEmployee(phoneId, employeeId)
          phonesAfter <- service.retrieveEmployeePhones(employeeId)
        } yield assertTrue(
          phonesBefore.length == 1,
          phonesAfter.isEmpty
        )
      }.provide(
        InMemoryEmployeeRepository.withEmployees(
          1.refineUnsafe[EmployeeIdDescription] -> Employee(
            EmployeeName("JohnDoe"),
            Age(30),
            1.refineUnsafe[DepartmentIdDescription]
          )
        ) ++
          InMemoryPhoneRepository.withPhones(
            1.refineUnsafe[PhoneIdDescription] -> Phone(
              PhoneNumber("1234567890")
            )
          ) >+>
          InMemoryEmployeePhoneRepository.withEmployeePhones(
            1.refineUnsafe[EmployeeIdDescription] -> Vector(
              1.refineUnsafe[PhoneIdDescription]
            )
          ) >>>
          EmployeePhoneServiceLive.layer
      ),
      test("should fail when phone doesn't exist") {
        val employeeId = 1.refineUnsafe[EmployeeIdDescription]
        val phoneId = 999.refineUnsafe[PhoneIdDescription]

        for {
          service <- ZIO.service[EmployeePhoneService]
          result <- service.removePhoneFromEmployee(phoneId, employeeId).exit
        } yield assertTrue(result.isFailure) && assertTrue(
          result match {
            case Exit.Failure(cause) =>
              cause.failureOption.contains(PhoneNotFound)
            case _ => false
          }
        )
      }.provide(
        InMemoryEmployeeRepository.withEmployees(
          1.refineUnsafe[EmployeeIdDescription] -> Employee(
            EmployeeName("JohnDoe"),
            Age(30),
            1.refineUnsafe[DepartmentIdDescription]
          )
        ) ++
          InMemoryPhoneRepository.layer >+>
          InMemoryEmployeePhoneRepository.layer >>>
          EmployeePhoneServiceLive.layer
      ),
      test("should fail when employee doesn't exist") {
        val employeeId = 999.refineUnsafe[EmployeeIdDescription]
        val phoneId = 1.refineUnsafe[PhoneIdDescription]

        for {
          service <- ZIO.service[EmployeePhoneService]
          result <- service.removePhoneFromEmployee(phoneId, employeeId).exit
        } yield assertTrue(result.isFailure) && assertTrue(
          result match {
            case Exit.Failure(cause) =>
              cause.failureOption.contains(EmployeeNotFound)
            case _ => false
          }
        )
      }.provide(
        InMemoryEmployeeRepository.layer ++
          InMemoryPhoneRepository.withPhones(
            1.refineUnsafe[PhoneIdDescription] -> Phone(
              PhoneNumber("1234567890")
            )
          ) >+>
          InMemoryEmployeePhoneRepository.layer >>>
          EmployeePhoneServiceLive.layer
      ),
      test(
        "should be idempotent (removing already removed phone doesn't error)"
      ) {
        val employeeId = 1.refineUnsafe[EmployeeIdDescription]
        val phoneId = 1.refineUnsafe[PhoneIdDescription]

        for {
          service <- ZIO.service[EmployeePhoneService]
          _ <- service.removePhoneFromEmployee(phoneId, employeeId)
          _ <- service.removePhoneFromEmployee(
            phoneId,
            employeeId
          ) // Second removal should not fail
          phones <- service.retrieveEmployeePhones(employeeId)
        } yield assertTrue(phones.isEmpty)
      }.provide(
        InMemoryEmployeeRepository.withEmployees(
          1.refineUnsafe[EmployeeIdDescription] -> Employee(
            EmployeeName("JohnDoe"),
            Age(30),
            1.refineUnsafe[DepartmentIdDescription]
          )
        ) ++
          InMemoryPhoneRepository.withPhones(
            1.refineUnsafe[PhoneIdDescription] -> Phone(
              PhoneNumber("1234567890")
            )
          ) >+>
          InMemoryEmployeePhoneRepository.layer >>>
          EmployeePhoneServiceLive.layer
      )
    )
  )
}
