package com.example.domain

import zio.schema.*
import com.example.util.given

case class Department(name: DepartmentName) derives Schema
