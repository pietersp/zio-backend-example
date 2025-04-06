package com.example.api.endpoint

import com.example.domain.{
  EmployeeId,
  EmployeeIdDescription,
  Phone,
  PhoneId,
  PhoneIdDescription
}
import com.example.error.AppError
import com.example.error.AppError.EmployeeNotFound
import zio.http.*
import zio.http.codec.*
import zio.http.endpoint.AuthType.None
import zio.http.endpoint.Endpoint

trait EmployeePhoneEndpoints extends Codecs {
  val addPhoneToEmployee: Endpoint[
    (EmployeeId, PhoneId),
    (EmployeeId, PhoneId),
    AppError,
    Unit,
    None
  ] =
    Endpoint(
      Method.POST
        / "employee" / idCodec[EmployeeIdDescription]()
        / "phone" / idCodec[PhoneIdDescription]()
    )
      .out[Unit]
      .outError[AppError](
        Status.NotFound,
        Doc.p("The employee/phone was not found")
      )
      ?? Doc.p("Add a phone to an employee")

  val retrieveEmployeePhones
    : Endpoint[EmployeeId, EmployeeId, EmployeeNotFound, Vector[Phone], None] =
    Endpoint(
      Method.GET / "employee" / idCodec[EmployeeIdDescription]() / "phone"
    )
      .out[Vector[Phone]](Doc.p("List of employee's phones"))
      .outError[EmployeeNotFound](
        Status.NotFound,
        Doc.p("The employee was not found")
      )
      ?? Doc.p("Obtain a list of the employee's phones")

  val removePhoneFromEmployee: Endpoint[
    (EmployeeId, PhoneId),
    (EmployeeId, PhoneId),
    AppError,
    Unit,
    None
  ] =
    Endpoint(
      Method.DELETE
        / "employee" / idCodec[EmployeeIdDescription]()
        / "phone" / idCodec[PhoneIdDescription]()
    )
      .out[Unit]
      .outError[AppError](
        Status.NotFound,
        Doc.p("The employee/phone was not found")
      )
      ?? Doc.p("Remove a phone from an employee")
}
