package com.example.repository

import com.example.domain.{EmployeeId, Phone, PhoneId}
import zio.*

trait EmployeePhoneRepository {
  def addPhoneToEmployee(phoneId: PhoneId, employeeId: EmployeeId): UIO[Unit]
  def retrieveEmployeePhones(employeeId: EmployeeId): UIO[Vector[Phone]]
  def removePhoneFromEmployee(
    phoneId: PhoneId,
    employeeId: EmployeeId
  ): UIO[Unit]
}
