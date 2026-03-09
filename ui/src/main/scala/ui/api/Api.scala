package ui.api

import scala.scalajs.js
import scala.scalajs.js.annotation.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.scalajs.dom.*

@js.native
@JSImport("...", JSImport.Default)
object fetch extends js.Function

case class Department(id: Long, name: String)
case class Employee(id: Long, name: String, age: Int, departmentId: Long)
case class Phone(id: Long, number: String, employeeId: Option[Long])

object Api:
  val baseUrl = "http://localhost:8080"

  def getDepartments: Future[List[Department]] =
    fetch(s"$baseUrl/departments").then(_.json()).map(_.asInstanceOf[js.Array[Department]].toList)

  def getDepartment(id: Long): Future[Department] =
    fetch(s"$baseUrl/departments/$id").then(_.json()).map(_.asInstanceOf[Department])

  def createDepartment(name: String): Future[Department] =
    fetch(s"$baseUrl/departments", 
      RequestInit(method = "POST", body = s"""{"name":"$name"}""", headers = Headers("Content-Type" -> "application/json"))
    ).then(_.json()).map(_.asInstanceOf[Department])

  def updateDepartment(id: Long, name: String): Future[Department] =
    fetch(s"$baseUrl/departments/$id",
      RequestInit(method = "PUT", body = s"""{"name":"$name"}""", headers = Headers("Content-Type" -> "application/json"))
    ).then(_.json()).map(_.asInstanceOf[Department])

  def deleteDepartment(id: Long): Future[Unit] =
    fetch(s"$baseUrl/departments/$id", RequestInit(method = "DELETE")).then(_ => ())

  def getEmployees: Future[List[Employee]] =
    fetch(s"$baseUrl/employees").then(_.json()).map(_.asInstanceOf[js.Array[Employee]].toList)

  def getEmployee(id: Long): Future[Employee] =
    fetch(s"$baseUrl/employees/$id").then(_.json()).map(_.asInstanceOf[Employee])

  def createEmployee(name: String, age: Int, departmentId: Long): Future[Employee] =
    fetch(s"$baseUrl/employees",
      RequestInit(method = "POST", body = s"""{"name":"$name","age":$age,"departmentId":$departmentId}""", headers = Headers("Content-Type" -> "application/json"))
    ).then(_.json()).map(_.asInstanceOf[Employee])

  def updateEmployee(id: Long, name: String, age: Int, departmentId: Long): Future[Employee] =
    fetch(s"$baseUrl/employees/$id",
      RequestInit(method = "PUT", body = s"""{"name":"$name","age":$age,"departmentId":$departmentId}""", headers = Headers("Content-Type" -> "application/json"))
    ).then(_.json()).map(_.asInstanceOf[Employee])

  def deleteEmployee(id: Long): Future[Unit] =
    fetch(s"$baseUrl/employees/$id", RequestInit(method = "DELETE")).then(_ => ())

  def getPhones: Future[List[Phone]] =
    fetch(s"$baseUrl/phones").then(_.json()).map(_.asInstanceOf[js.Array[Phone]].toList)

  def getPhone(id: Long): Future[Phone] =
    fetch(s"$baseUrl/phones/$id").then(_.json()).map(_.asInstanceOf[Phone])

  def createPhone(number: String, employeeId: Option[Long]): Future[Phone] =
    fetch(s"$baseUrl/phones",
      RequestInit(method = "POST", body = s"""{"number":"$number","employeeId":${employeeId.map(_.toString).getOrElse("null")}}""", headers = Headers("Content-Type" -> "application/json"))
    ).then(_.json()).map(_.asInstanceOf[Phone])

  def updatePhone(id: Long, number: String, employeeId: Option[Long]): Future[Phone] =
    fetch(s"$baseUrl/phones/$id",
      RequestInit(method = "PUT", body = s"""{"number":"$number","employeeId":${employeeId.map(_.toString).getOrElse("null")}}""", headers = Headers("Content-Type" -> "application/json"))
    ).then(_.json()).map(_.asInstanceOf[Phone])

  def deletePhone(id: Long): Future[Unit] =
    fetch(s"$baseUrl/phones/$id", RequestInit(method = "DELETE")).then(_ => ())

  def getEmployeePhones(employeeId: Long): Future[List[Phone]] =
    fetch(s"$baseUrl/employee-phones/$employeeId").then(_.json()).map(_.asInstanceOf[js.Array[Phone]].toList)

  def addPhoneToEmployee(employeeId: Long, phoneId: Long): Future[Unit] =
    fetch(s"$baseUrl/employee-phones",
      RequestInit(method = "POST", body = s"""{"employeeId":$employeeId,"phoneId":$phoneId}""", headers = Headers("Content-Type" -> "application/json"))
    ).then(_ => ())

  def removePhoneFromEmployee(employeeId: Long, phoneId: Long): Future[Unit] =
    fetch(s"$baseUrl/employee-phones?employeeId=$employeeId&phoneId=$phoneId", RequestInit(method = "DELETE")).then(_ => ())
