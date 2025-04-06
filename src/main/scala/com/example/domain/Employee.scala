package com.example.domain

import zio.schema.*

case class Employee(name: String, age: Int, departmentId: Int) derives Schema
