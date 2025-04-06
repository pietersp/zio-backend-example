package com.example.domain

import com.example.util.iron.given
import zio.schema.*

case class Employee(name: EmployeeName, age: Age, departmentId: DepartmentId)
    derives Schema
