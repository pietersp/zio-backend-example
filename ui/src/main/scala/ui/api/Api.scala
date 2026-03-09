package ui.api

import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.scalajs.dom.*

case class Department(id: Long, name: String)
case class Employee(id: Long, name: String, age: Int, departmentId: Long)
case class Phone(id: Long, number: String, employeeId: Option[Long])

object Api:
  val baseUrl = "http://localhost:8080"

  private def checkResponse(response: Response): Future[Response] =
    if (response.ok) Future.successful(response)
    else Future.failed(new Exception(s"HTTP ${response.status}: ${response.statusText}"))

  private def fetchJson(url: String, method: String = "GET", body: js.Any = null): Future[js.Any] =
    val init = new RequestInit()
    init.method = method
    if body != null then init.body = JSON.stringify(body)
    init.headers = js.Dictionary("Content-Type" -> "application/json")
    
    fetch(url, init).toFuture.flatMap(checkResponse).flatMap(_.json().toFuture)

  def getDepartments: Future[List[Department]] =
    fetchJson(s"$baseUrl/departments").map(_.asInstanceOf[js.Array[Department]].toList)

  def getDepartment(id: Long): Future[Department] =
    fetchJson(s"$baseUrl/departments/$id").map(_.asInstanceOf[Department])

  def createDepartment(name: String): Future[Department] =
    fetchJson(s"$baseUrl/departments", "POST", js.Dynamic.literal(name = name)).map(_.asInstanceOf[Department])

  def updateDepartment(id: Long, name: String): Future[Department] =
    fetchJson(s"$baseUrl/departments/$id", "PUT", js.Dynamic.literal(name = name)).map(_.asInstanceOf[Department])

  def deleteDepartment(id: Long): Future[Unit] =
    fetchJson(s"$baseUrl/departments/$id", "DELETE").map(_ => ())

  def getEmployees: Future[List[Employee]] =
    fetchJson(s"$baseUrl/employees").map(_.asInstanceOf[js.Array[Employee]].toList)

  def getEmployee(id: Long): Future[Employee] =
    fetchJson(s"$baseUrl/employees/$id").map(_.asInstanceOf[Employee])

  def createEmployee(name: String, age: Int, departmentId: Long): Future[Employee] =
    fetchJson(s"$baseUrl/employees", "POST", js.Dynamic.literal(name = name, age = age, departmentId = departmentId)).map(_.asInstanceOf[Employee])

  def updateEmployee(id: Long, name: String, age: Int, departmentId: Long): Future[Employee] =
    fetchJson(s"$baseUrl/employees/$id", "PUT", js.Dynamic.literal(name = name, age = age, departmentId = departmentId)).map(_.asInstanceOf[Employee])

  def deleteEmployee(id: Long): Future[Unit] =
    fetchJson(s"$baseUrl/employees/$id", "DELETE").map(_ => ())

  def getPhones: Future[List[Phone]] =
    fetchJson(s"$baseUrl/phones").map(_.asInstanceOf[js.Array[Phone]].toList)

  def getPhone(id: Long): Future[Phone] =
    fetchJson(s"$baseUrl/phones/$id").map(_.asInstanceOf[Phone])

  def createPhone(number: String, employeeId: Option[Long]): Future[Phone] =
    fetchJson(s"$baseUrl/phones", "POST", js.Dynamic.literal(number = number, employeeId = employeeId.orNull)).map(_.asInstanceOf[Phone])

  def updatePhone(id: Long, number: String, employeeId: Option[Long]): Future[Phone] =
    fetchJson(s"$baseUrl/phones/$id", "PUT", js.Dynamic.literal(number = number, employeeId = employeeId.orNull)).map(_.asInstanceOf[Phone])

  def deletePhone(id: Long): Future[Unit] =
    fetchJson(s"$baseUrl/phones/$id", "DELETE").map(_ => ())

  def getEmployeePhones(employeeId: Long): Future[List[Phone]] =
    fetchJson(s"$baseUrl/employee-phones/$employeeId").map(_.asInstanceOf[js.Array[Phone]].toList)

  def addPhoneToEmployee(employeeId: Long, phoneId: Long): Future[Unit] =
    fetchJson(s"$baseUrl/employee-phones", "POST", js.Dynamic.literal(employeeId = employeeId, phoneId = phoneId)).map(_ => ())

  def removePhoneFromEmployee(employeeId: Long, phoneId: Long): Future[Unit] =
    fetchJson(s"$baseUrl/employee-phones?employeeId=$employeeId&phoneId=$phoneId", "DELETE").map(_ => ())
