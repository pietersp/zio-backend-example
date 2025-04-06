package com.example.domain

import zio.schema.*
import com.example.util.given

case class Employee(name: EmployeeName, age: Age, departmentId: DepartmentId)
    derives Schema
