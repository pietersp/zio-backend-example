package com.example.service

import com.example.domain.{Phone, PhoneId, PhoneIdDescription, PhoneNumber}
import com.example.error.AppError.{PhoneAlreadyExists, PhoneNotFound}
import com.example.repository.PhoneRepository
import io.github.iltotore.iron.*
import zio.*
import zio.test.*

object PhoneServiceSpec extends ZIOSpecDefault {

  // In-memory implementation of PhoneRepository for testing
  case class InMemoryPhoneRepository(
    phones: Ref[Map[PhoneId, Phone]],
    nextId: Ref[Int]
  ) extends PhoneRepository {

    override def create(phone: Phone): UIO[PhoneId] =
      for {
        id <- nextId.getAndUpdate(_ + 1)
        phoneId = id.refineUnsafe[PhoneIdDescription]
        _ <- phones.update(_ + (phoneId -> phone))
      } yield phoneId

    override def retrieve(phoneId: PhoneId): UIO[Option[Phone]] =
      phones.get.map(_.get(phoneId))

    override def retrieveByNumber(phoneNumber: PhoneNumber): UIO[Option[Phone]] =
      phones.get.map(_.values.find(_.number == phoneNumber))

    override def update(phoneId: PhoneId, phone: Phone): UIO[Unit] =
      phones.update(_ + (phoneId -> phone))

    override def delete(phoneId: PhoneId): UIO[Unit] =
      phones.update(_ - phoneId)
  }

  object InMemoryPhoneRepository {
    val layer: ZLayer[Any, Nothing, PhoneRepository] =
      ZLayer {
        for {
          phones <- Ref.make(Map.empty[PhoneId, Phone])
          nextId <- Ref.make(1)
        } yield InMemoryPhoneRepository(phones, nextId)
      }

    def withPhones(phones: (PhoneId, Phone)*): ZLayer[Any, Nothing, PhoneRepository] =
      ZLayer {
        for {
          phonesRef <- Ref.make(phones.toMap)
          nextId <- Ref.make(phones.map(_._1).maxOption.getOrElse(0) + 1)
        } yield InMemoryPhoneRepository(phonesRef, nextId)
      }
  }

  // Test layer composition
  val testLayer: ZLayer[Any, Nothing, PhoneService] =
    InMemoryPhoneRepository.layer >>> PhoneServiceLive.layer

  def spec = suite("PhoneServiceSpec")(
    suite("create")(
      test("should create phone with unique number") {
        val phone = Phone(PhoneNumber("1234567890"))

        for {
          service <- ZIO.service[PhoneService]
          phoneId <- service.create(phone)
          retrieved <- service.retrieveById(phoneId)
        } yield assertTrue(
          retrieved.number == phone.number
        )
      }.provide(testLayer),

      test("should fail when phone number already exists") {
        val existingPhone = Phone(PhoneNumber("1234567890"))
        val duplicatePhone = Phone(PhoneNumber("1234567890"))

        for {
          service <- ZIO.service[PhoneService]
          _ <- service.create(existingPhone)
          result <- service.create(duplicatePhone).exit
        } yield assertTrue(result.isFailure) && assertTrue(
          result match {
            case Exit.Failure(cause) =>
              cause.failureOption.contains(PhoneAlreadyExists)
            case _ => false
          }
        )
      }.provide(testLayer),

      test("should allow creating multiple phones with different numbers") {
        val phone1 = Phone(PhoneNumber("111111"))
        val phone2 = Phone(PhoneNumber("222222"))

        for {
          service <- ZIO.service[PhoneService]
          id1 <- service.create(phone1)
          id2 <- service.create(phone2)
          retrieved1 <- service.retrieveById(id1)
          retrieved2 <- service.retrieveById(id2)
        } yield assertTrue(
          retrieved1.number == phone1.number,
          retrieved2.number == phone2.number,
          id1 != id2
        )
      }.provide(testLayer)
    ),

    suite("retrieveById")(
      test("should retrieve phone by ID when it exists") {
        val phoneId = 1.refineUnsafe[PhoneIdDescription]
        val phone = Phone(PhoneNumber("1234567890"))

        for {
          service <- ZIO.service[PhoneService]
          retrieved <- service.retrieveById(phoneId)
        } yield assertTrue(
          retrieved.number == phone.number
        )
      }.provide(
        InMemoryPhoneRepository.withPhones(1.refineUnsafe[PhoneIdDescription] -> Phone(PhoneNumber("1234567890"))) >>> 
          PhoneServiceLive.layer
      ),

      test("should fail when phone doesn't exist") {
        val phoneId = 999.refineUnsafe[PhoneIdDescription]

        for {
          service <- ZIO.service[PhoneService]
          result <- service.retrieveById(phoneId).exit
        } yield assertTrue(result.isFailure) && assertTrue(
          result match {
            case Exit.Failure(cause) =>
              cause.failureOption.contains(PhoneNotFound)
            case _ => false
          }
        )
      }.provide(testLayer)
    ),

    suite("update")(
      test("should update existing phone") {
        val phoneId = 1.refineUnsafe[PhoneIdDescription]
        val originalPhone = Phone(PhoneNumber("111111"))
        val updatedPhone = Phone(PhoneNumber("222222"))

        for {
          service <- ZIO.service[PhoneService]
          _ <- service.update(phoneId, updatedPhone)
          retrieved <- service.retrieveById(phoneId)
        } yield assertTrue(
          retrieved.number == updatedPhone.number
        )
      }.provide(
        InMemoryPhoneRepository.withPhones(1.refineUnsafe[PhoneIdDescription] -> Phone(PhoneNumber("111111"))) >>> 
          PhoneServiceLive.layer
      ),

      test("should fail to update non-existent phone") {
        val phoneId = 999.refineUnsafe[PhoneIdDescription]
        val phone = Phone(PhoneNumber("1234567890"))

        for {
          service <- ZIO.service[PhoneService]
          result <- service.update(phoneId, phone).exit
        } yield assertTrue(result.isFailure) && assertTrue(
          result match {
            case Exit.Failure(cause) =>
              cause.failureOption.contains(PhoneNotFound)
            case _ => false
          }
        )
      }.provide(testLayer)
    ),

    suite("delete")(
      test("should delete phone successfully") {
        val phoneId = 1.refineUnsafe[PhoneIdDescription]
        val phone = Phone(PhoneNumber("1234567890"))

        for {
          service <- ZIO.service[PhoneService]
          _ <- service.delete(phoneId)
          result <- service.retrieveById(phoneId).exit
        } yield assertTrue(result.isFailure)
      }.provide(
        InMemoryPhoneRepository.withPhones(1.refineUnsafe[PhoneIdDescription] -> Phone(PhoneNumber("1234567890"))) >>> 
          PhoneServiceLive.layer
      ),

      test("should be idempotent (deleting non-existent phone doesn't error)") {
        val phoneId = 999.refineUnsafe[PhoneIdDescription]

        for {
          service <- ZIO.service[PhoneService]
          _ <- service.delete(phoneId)
          _ <- service.delete(phoneId) // Second delete should not fail
        } yield assertTrue(true)
      }.provide(testLayer)
    )
  )
}
