package com.example.domain

import com.example.util.iron.given
import zio.schema.*

case class Department(name: DepartmentName) derives Schema
