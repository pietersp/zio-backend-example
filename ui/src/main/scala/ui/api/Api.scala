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

  private def fetchJson(url: String, method: String = "GET", body: js.Any = null): Future[js.Any] =
    val options = js.Dynamic.literal()
    options.method = method
    if body != null then options.body = JSON.stringify(body)
    options.headers = js.Dynamic.literal("Content-Type" -> "application/json")
    
    fetch(url, options.asInstanceOf[RequestInit]).toFuture.flatMap { response =>
      if (response.ok) response.json().toFuture
      else response.text().toFuture.flatMap { text =>
        Future.failed(new Exception(s"HTTP ${response.status}: $text"))
      }
    }

  def getDepartments: Future[List[Department]] =
    fetchJson(s"$baseUrl/departments").map { result =>
      val arr = result.asInstanceOf[js.Array[js.Dynamic]]
      arr.zipWithIndex.map { case (d, idx) => Department(idx + 1, d.name.asInstanceOf[String]) }.toList
    }

  def getDepartment(id: Long): Future[Department] =
    fetchJson(s"$baseUrl/department/$id").map(d => Department(id, d.asInstanceOf[js.Dynamic].name.asInstanceOf[String]))

  def createDepartment(name: String): Future[Department] =
    fetchJson(s"$baseUrl/department", "POST", js.Dynamic.literal(name = name)).map { result =>
      val id = result.asInstanceOf[Double].toLong
      Department(id, name)
    }

  def updateDepartment(id: Long, name: String): Future[Unit] =
    fetchJson(s"$baseUrl/department/$id", "PUT", js.Dynamic.literal(name = name)).map(_ => ())

  def deleteDepartment(id: Long): Future[Unit] =
    fetchJson(s"$baseUrl/department/$id", "DELETE").map(_ => ())

  def getEmployees: Future[List[Employee]] =
    fetchJson(s"$baseUrl/employees").map { result =>
      val arr = result.asInstanceOf[js.Array[js.Dynamic]]
      arr.zipWithIndex.map { case (e, idx) => Employee(idx + 1, e.name.asInstanceOf[String], e.age.asInstanceOf[Int], e.departmentId.asInstanceOf[Double].toLong) }.toList
    }

  def getEmployee(id: Long): Future[Employee] =
    fetchJson(s"$baseUrl/employee/$id").map { e =>
      Employee(id, e.asInstanceOf[js.Dynamic].name.asInstanceOf[String], e.asInstanceOf[js.Dynamic].age.asInstanceOf[Int], e.asInstanceOf[js.Dynamic].departmentId.asInstanceOf[Double].toLong)
    }

  def createEmployee(name: String, age: Int, departmentId: Long): Future[Employee] =
    fetchJson(s"$baseUrl/employee", "POST", js.Dynamic.literal(name = name, age = age, departmentId = departmentId)).map { result =>
      val id = result.asInstanceOf[Double].toLong
      Employee(id, name, age, departmentId)
    }

  def updateEmployee(id: Long, name: String, age: Int, departmentId: Long): Future[Unit] =
    fetchJson(s"$baseUrl/employee/$id", "PUT", js.Dynamic.literal(name = name, age = age, departmentId = departmentId)).map(_ => ())

  def deleteEmployee(id: Long): Future[Unit] =
    fetchJson(s"$baseUrl/employee/$id", "DELETE").map(_ => ())

  def getPhones: Future[List[Phone]] =
    fetchJson(s"$baseUrl/phones").map { result =>
      val arr = result.asInstanceOf[js.Array[js.Dynamic]]
      arr.map(p => Phone(p.id.asInstanceOf[Long], p.number.asInstanceOf[String], Option(p.employeeId).map(_.asInstanceOf[Long]))).toList
    }

  def getPhone(id: Long): Future[Phone] =
    fetchJson(s"$baseUrl/phone/$id").map { p =>
      Phone(p.asInstanceOf[js.Dynamic].id.asInstanceOf[Long], p.asInstanceOf[js.Dynamic].number.asInstanceOf[String], Option(p.asInstanceOf[js.Dynamic].employeeId).map(_.asInstanceOf[Long]))
    }

  def createPhone(number: String, employeeId: Option[Long]): Future[Phone] =
    val empId: js.Any = if employeeId.isDefined then employeeId.get.asInstanceOf[js.Any] else js.undefined
    fetchJson(s"$baseUrl/phone", "POST", js.Dynamic.literal(number = number, employeeId = empId)).map { result =>
      val id = result.asInstanceOf[Double].toLong
      Phone(id, number, employeeId)
    }

  def updatePhone(id: Long, number: String, employeeId: Option[Long]): Future[Unit] =
    val empId: js.Any = if employeeId.isDefined then employeeId.get.asInstanceOf[js.Any] else js.undefined
    fetchJson(s"$baseUrl/phone/$id", "PUT", js.Dynamic.literal(number = number, employeeId = empId)).map(_ => ())

  def deletePhone(id: Long): Future[Unit] =
    fetchJson(s"$baseUrl/phone/$id", "DELETE").map(_ => ())

  def getEmployeePhones(employeeId: Long): Future[List[Phone]] =
    fetchJson(s"$baseUrl/employee/$employeeId/phone").map { result =>
      val arr = result.asInstanceOf[js.Array[js.Dynamic]]
      arr.map(p => Phone(p.id.asInstanceOf[Long], p.number.asInstanceOf[String], Option(p.employeeId).map(_.asInstanceOf[Long]))).toList
    }

  def addPhoneToEmployee(employeeId: Long, phoneId: Long): Future[Unit] =
    fetchJson(s"$baseUrl/employee/$employeeId/phone/$phoneId", "POST").map(_ => ())

  def removePhoneFromEmployee(employeeId: Long, phoneId: Long): Future[Unit] =
    fetchJson(s"$baseUrl/employee/$employeeId/phone/$phoneId", "DELETE").map(_ => ())
