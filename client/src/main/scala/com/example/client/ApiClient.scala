package com.example.client

import zio.*
import zio.http.{Client, URL}
import zio.http.endpoint.{EndpointExecutor, EndpointLocator}
import com.example.api.endpoint.*
import com.example.domain.*
import com.example.error.AppError

/** Comprehensive API client for all endpoints */
trait ApiClient {
  // Department operations
  def createDepartment(department: Department): IO[AppError.DepartmentAlreadyExists, DepartmentId]
  def getDepartments: UIO[Vector[Department]]
  def getDepartmentById(id: DepartmentId): IO[AppError.DepartmentNotFound, Department]
  def updateDepartment(id: DepartmentId, department: Department): IO[AppError.DepartmentNotFound, Unit]
  def deleteDepartment(id: DepartmentId): UIO[Unit]

  // Employee operations
  def createEmployee(employee: Employee): IO[AppError.DepartmentNotFound, EmployeeId]
  def getEmployees: UIO[Vector[Employee]]
  def getEmployeeById(id: EmployeeId): IO[AppError.EmployeeNotFound, Employee]
  def updateEmployee(id: EmployeeId, employee: Employee): IO[AppError.EmployeeNotFound, Unit]
  def deleteEmployee(id: EmployeeId): UIO[Unit]

  // Phone operations
  def createPhone(phone: Phone): IO[AppError.PhoneAlreadyExists, PhoneId]
  def getPhoneById(id: PhoneId): IO[AppError.PhoneNotFound, Phone]
  def updatePhone(id: PhoneId, phone: Phone): IO[AppError.PhoneNotFound, Unit]
  def deletePhone(id: PhoneId): UIO[Unit]

  // EmployeePhone operations
  def addPhoneToEmployee(employeeId: EmployeeId, phoneId: PhoneId): IO[AppError, Unit]
  def getEmployeePhones(employeeId: EmployeeId): IO[AppError.EmployeeNotFound, Vector[Phone]]
  def removePhoneFromEmployee(employeeId: EmployeeId, phoneId: PhoneId): IO[AppError, Unit]
}

final case class ApiClientLive(
  client: Client,
  baseUrl: URL
) extends ApiClient {
  
  private val locator = EndpointLocator.fromURL(baseUrl)
  private val executor = EndpointExecutor(client, locator)

  // Department operations
  override def createDepartment(department: Department): IO[AppError.DepartmentAlreadyExists, DepartmentId] =
    ZIO.scoped {
      executor(DepartmentEndpoints.createDepartment(department))
    }

  override def getDepartments: UIO[Vector[Department]] =
    ZIO.scoped {
      executor(DepartmentEndpoints.getDepartments(()))
    }

  override def getDepartmentById(id: DepartmentId): IO[AppError.DepartmentNotFound, Department] =
    ZIO.scoped {
      executor(DepartmentEndpoints.getDepartmentById(id))
    }

  override def updateDepartment(id: DepartmentId, department: Department): IO[AppError.DepartmentNotFound, Unit] =
    ZIO.scoped {
      executor(DepartmentEndpoints.updateDepartment(id, department))
    }

  override def deleteDepartment(id: DepartmentId): UIO[Unit] =
    ZIO.scoped {
      executor(DepartmentEndpoints.deleteDepartment(id))
    }

  // Employee operations
  override def createEmployee(employee: Employee): IO[AppError.DepartmentNotFound, EmployeeId] =
    ZIO.scoped {
      executor(EmployeeEndpoints.createEmployee(employee))
    }

  override def getEmployees: UIO[Vector[Employee]] =
    ZIO.scoped {
      executor(EmployeeEndpoints.getEmployees(()))
    }

  override def getEmployeeById(id: EmployeeId): IO[AppError.EmployeeNotFound, Employee] =
    ZIO.scoped {
      executor(EmployeeEndpoints.getEmployeeById(id))
    }

  override def updateEmployee(id: EmployeeId, employee: Employee): IO[AppError.EmployeeNotFound, Unit] =
    ZIO.scoped {
      executor(EmployeeEndpoints.updateEmployee(id, employee))
    }

  override def deleteEmployee(id: EmployeeId): UIO[Unit] =
    ZIO.scoped {
      executor(EmployeeEndpoints.deleteEmployee(id))
    }

  // Phone operations
  override def createPhone(phone: Phone): IO[AppError.PhoneAlreadyExists, PhoneId] =
    ZIO.scoped {
      executor(PhoneEndpoints.createPhone(phone))
    }

  override def getPhoneById(id: PhoneId): IO[AppError.PhoneNotFound, Phone] =
    ZIO.scoped {
      executor(PhoneEndpoints.getPhoneById(id))
    }

  override def updatePhone(id: PhoneId, phone: Phone): IO[AppError.PhoneNotFound, Unit] =
    ZIO.scoped {
      executor(PhoneEndpoints.updatePhone(id, phone))
    }

  override def deletePhone(id: PhoneId): UIO[Unit] =
    ZIO.scoped {
      executor(PhoneEndpoints.deletePhone(id))
    }

  // EmployeePhone operations
  override def addPhoneToEmployee(employeeId: EmployeeId, phoneId: PhoneId): IO[AppError, Unit] =
    ZIO.scoped {
      executor(EmployeePhoneEndpoints.addPhoneToEmployee(employeeId, phoneId))
    }

  override def getEmployeePhones(employeeId: EmployeeId): IO[AppError.EmployeeNotFound, Vector[Phone]] =
    ZIO.scoped {
      executor(EmployeePhoneEndpoints.retrieveEmployeePhones(employeeId))
    }

  override def removePhoneFromEmployee(employeeId: EmployeeId, phoneId: PhoneId): IO[AppError, Unit] =
    ZIO.scoped {
      executor(EmployeePhoneEndpoints.removePhoneFromEmployee(employeeId, phoneId))
    }
}

object ApiClientLive {
  val layer: ZLayer[Client & ApiClientConfig, Throwable, ApiClient] =
    ZLayer {
      for {
        client <- ZIO.service[Client]
        config <- ZIO.service[ApiClientConfig]
        baseUrl <- ZIO.fromEither(URL.decode(config.baseUrl))
          .mapError(error => new RuntimeException(s"Invalid base URL: ${config.baseUrl} - $error"))
      } yield ApiClientLive(client, baseUrl)
    }
}
