package com.example.tables

import com.example.domain
import com.augustnagro.magnum.magzio.*

@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
case class Department(
  @Id id: Int,
  name: String
) derives DbCodec {
  def toDomain: domain.Department = domain.Department(name)
}

object Department {
  val table = TableInfo[domain.Department, Department, Int]
  
  def fromDomain(departmentId: Int, department: domain.Department): Department =
    Department(
      id = departmentId,
      name = department.name
    )
}
