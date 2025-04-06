package com.example.tables

import com.example.domain
import com.example.domain.{DepartmentName, DepartmentId}
import com.augustnagro.magnum.magzio.*
import com.example.util.given

@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
case class Department(
  @Id id: DepartmentId,
  name: DepartmentName
) derives DbCodec {
  def toDomain: domain.Department = domain.Department(name)
}

object Department {
  val table = TableInfo[domain.Department, Department, Int]

  def fromDomain(
    departmentId: DepartmentId,
    department: domain.Department
  ): Department =
    Department(
      id = departmentId,
      name = department.name
    )
}
