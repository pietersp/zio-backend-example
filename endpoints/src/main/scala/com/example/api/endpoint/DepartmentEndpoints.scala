package com.example.api.endpoint

import com.example.domain.{Department, DepartmentId, DepartmentIdDescription}
import com.example.error.AppError.{DepartmentAlreadyExists, DepartmentNotFound}
import com.example.util.iron.given
import zio.http.*
import zio.http.codec.*
import zio.http.endpoint.Endpoint

object DepartmentEndpoints extends Codecs {
  val createDepartment =
    Endpoint(Method.POST / "department")
      .in[Department](Doc.p("Department to be created"))
      .out[DepartmentId](Doc.p("ID of the created department"))
      .outError[DepartmentAlreadyExists](
        Status.Conflict,
        Doc.p("The department already exists")
      )
      ?? Doc.p("Create a new department")

  val getDepartments =
    Endpoint(Method.GET / "departments")
      .out[Vector[Department]](Doc.p("List of all departments"))
      ?? Doc.p("Obtain a list of all departments")

  val getDepartmentById =
    Endpoint(Method.GET / "department" / idCodec[DepartmentIdDescription]())
      .out[Department](Doc.p("Department"))
      .outError[DepartmentNotFound](
        Status.NotFound,
        Doc.p("The department was not found")
      )
      ?? Doc.p("Obtain the department with the given `id`")

  val updateDepartment =
    Endpoint(Method.PUT / "department" / idCodec[DepartmentIdDescription]())
      .in[Department](Doc.p("Department to be updated"))
      .out[Unit]
      .outError[DepartmentNotFound](
        Status.NotFound,
        Doc.p("The department was not found")
      )
      ?? Doc.p("Update the department with the given `id`")

  val deleteDepartment =
    Endpoint(Method.DELETE / "department" / idCodec[DepartmentIdDescription]())
      .out[Unit]
      ?? Doc.p("Delete the department with the given `id`")
}
