package com.example.tables

import com.example.domain
import com.augustnagro.magnum.magzio.*

@Table(PostgresDbType, SqlNameMapper.CamelToSnakeCase)
final case class Employee(
  @Id id: Int,
  name: String,
  age: Int,
  departmentId: Int
) derives DbCodec {
  val toDomain: domain.Employee = domain.Employee(name, age, departmentId)
}

object Employee {
  val table = TableInfo[domain.Employee, Employee, Int]
  
  def fromDomain(employeeId: Int, employee: domain.Employee): Employee =
    Employee(
      id = employeeId,
      name = employee.name,
      age = employee.age,
      departmentId = employee.departmentId
    )
}
