package com.example.repository

import com.example.domain.{Employee, EmployeeId}
import zio.*

trait EmployeeRepository {
  def create(employee: Employee): UIO[EmployeeId]
  def retrieve(employeeId: EmployeeId): UIO[Option[Employee]]
  def retrieveAll: UIO[Vector[Employee]]
  def update(employeeId: EmployeeId, employee: Employee): UIO[Unit]
  def delete(employeeId: EmployeeId): UIO[Unit]
}
