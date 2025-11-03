package com.example.repository

import com.augustnagro.magnum.magzio.Transactor
import com.example.domain.Phone
import com.example.domain.PhoneId
import com.example.repository.testutils.TestContainerSupport
import io.github.iltotore.iron.*
import zio.*
import zio.test.*

object PhoneRepositoryLiveSpec extends ZIOSpecDefault {

  val testLayer: ZLayer[Any, Throwable, Transactor & PhoneRepository] =
    TestContainerSupport.testDbLayer >+> PhoneRepositoryLive.layer

  def spec = tests.provideShared(testLayer) @@ TestAspect.sequential
  private val tests = suite("PhoneRepositoryLiveSpec")(
    suite("create")(
      test("should create a phone and return its ID") {
        for {
          repo <- ZIO.service[PhoneRepository]
          phone = Phone(number = "1234567890".refineUnsafe)
          phoneId <- repo.create(phone)
          retrieved <- repo.retrieve(phoneId)
        } yield assertTrue(
          retrieved.isDefined,
          retrieved.get.number == phone.number
        )
      }
    ),
    suite("retrieve")(
      test("should return None when phone does not exist") {
        for {
          repo <- ZIO.service[PhoneRepository]
          retrieved <- repo.retrieve(PhoneId(9999))
        } yield assertTrue(retrieved.isEmpty)
      },
      test("should retrieve an existing phone by ID") {
        for {
          repo <- ZIO.service[PhoneRepository]
          phone = Phone(number = "9876543210".refineUnsafe)
          phoneId <- repo.create(phone)
          retrieved <- repo.retrieve(phoneId)
        } yield assertTrue(
          retrieved.isDefined,
          retrieved.get.number == phone.number
        )
      }
    ),
    suite("retrieveByNumber")(
      test("should return None when phone with number does not exist") {
        for {
          repo <- ZIO.service[PhoneRepository]
          retrieved <- repo.retrieveByNumber("0000000000".refineUnsafe)
        } yield assertTrue(retrieved.isEmpty)
      },
      test("should retrieve a phone by number") {
        for {
          repo <- ZIO.service[PhoneRepository]
          phone = Phone(number = "5555555555".refineUnsafe)
          phoneId <- repo.create(phone)
          retrieved <- repo.retrieveByNumber(phone.number)
        } yield assertTrue(
          retrieved.isDefined,
          retrieved.get.number == phone.number
        )
      }
    ),
    suite("update")(
      test("should update an existing phone") {
        for {
          repo <- ZIO.service[PhoneRepository]
          phone = Phone(number = "1111111111".refineUnsafe)
          phoneId <- repo.create(phone)
          updatedPhone = Phone(number = "2222222222".refineUnsafe)
          _ <- repo.update(phoneId, updatedPhone)
          retrieved <- repo.retrieve(phoneId)
        } yield assertTrue(
          retrieved.isDefined,
          retrieved.get.number == updatedPhone.number
        )
      },
      test("should handle update of non-existent phone gracefully") {
        for {
          repo <- ZIO.service[PhoneRepository]
          phone = Phone(number = "9999999999".refineUnsafe)
          _ <- repo.update(PhoneId(9999), phone)
          retrieved <- repo.retrieve(PhoneId(9999))
        } yield assertTrue(retrieved.isEmpty)
      }
    ),
    suite("delete")(
      test("should delete an existing phone") {
        for {
          repo <- ZIO.service[PhoneRepository]
          phone = Phone(number = "3333333333".refineUnsafe)
          phoneId <- repo.create(phone)
          _ <- repo.delete(phoneId)
          retrieved <- repo.retrieve(phoneId)
        } yield assertTrue(retrieved.isEmpty)
      },
      test("should be idempotent when deleting non-existent phone") {
        for {
          repo <- ZIO.service[PhoneRepository]
          _ <- repo.delete(PhoneId(9999))
          _ <- repo.delete(PhoneId(9999))
        } yield assertTrue(true)
      }
    )
  )
}
