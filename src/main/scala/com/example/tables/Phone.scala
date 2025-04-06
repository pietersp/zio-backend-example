package com.example.tables
import com.augustnagro.magnum.magzio.*
import com.example.domain

@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
case class Phone(
  @Id id: Int,
  number: String
) derives DbCodec {
  def toDomain: domain.Phone = domain.Phone(number)
}

object Phone {
  val table = TableInfo[domain.Phone, Phone, Int]

  def fromDomain(phoneId: Int, phone: domain.Phone): Phone =
    Phone(
      id = phoneId,
      number = phone.number
    )
}
