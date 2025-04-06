package com.example.tables
import com.augustnagro.magnum.magzio.*
import com.example.domain
import com.example.domain.{PhoneId, PhoneNumber}
import com.example.util.db.given

@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
case class Phone(
  @Id id: PhoneId,
  number: PhoneNumber
) derives DbCodec {
  def toDomain: domain.Phone = domain.Phone(number)
}

object Phone {
  val table = TableInfo[domain.Phone, Phone, Int]

  def fromDomain(phoneId: PhoneId, phone: domain.Phone): Phone =
    Phone(
      id = phoneId,
      number = phone.number
    )
}
