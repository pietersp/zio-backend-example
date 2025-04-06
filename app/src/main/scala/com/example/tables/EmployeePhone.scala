package com.example.tables

import com.augustnagro.magnum.magzio.*
import com.example.domain.{EmployeeId, PhoneId}
import com.example.util.db.given

@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
case class EmployeePhone(
  employeeId: EmployeeId,
  phoneId: PhoneId
) derives DbCodec

object EmployeePhone {
  val table = TableInfo[EmployeePhone, EmployeePhone, Null]
}
