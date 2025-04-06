package com.example.repository

import com.augustnagro.magnum.magzio.*
import com.example.domain.{Phone, PhoneId, PhoneNumber}
import com.example.tables
import com.example.util.db.given
import zio.*

trait PhoneRepository {
  def create(phone: Phone): UIO[PhoneId]
  def retrieve(phoneId: PhoneId): UIO[Option[Phone]]
  def retrieveByNumber(phoneNumber: PhoneNumber): UIO[Option[Phone]]
  def update(phoneId: PhoneId, phone: Phone): UIO[Unit]
  def delete(phoneId: PhoneId): UIO[Unit]
}

final case class PhoneRepositoryLive(xa: Transactor)
    extends Repo[Phone, tables.Phone, PhoneId]
    with PhoneRepository {

  override def create(phone: Phone): UIO[PhoneId] =
    xa.transact {
      insertReturning(phone).id
    }.orDie

  override def retrieve(phoneId: PhoneId): UIO[Option[Phone]] =
    xa.transact {
      findById(phoneId).map(_.toDomain)
    }.orDie

  override def retrieveByNumber(phoneNumber: PhoneNumber): UIO[Option[Phone]] =
    xa.transact {
      val spec = Spec[tables.Phone].where(
        sql"${tables.Phone.table.number} = $phoneNumber"
      )

      findAll(spec).headOption.map(_.toDomain)
    }.orDie

  override def update(phoneId: PhoneId, phone: Phone): UIO[Unit] =
    xa.transact {
      update(tables.Phone.fromDomain(phoneId, phone))
    }.orDie

  override def delete(phoneId: PhoneId): UIO[Unit] =
    xa.transact {
      deleteById(phoneId)
    }.orDie
}

object PhoneRepositoryLive {
  val layer: URLayer[Transactor, PhoneRepositoryLive] =
    ZLayer.fromFunction(PhoneRepositoryLive(_))
}
