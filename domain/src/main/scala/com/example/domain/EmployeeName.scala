package com.example.domain

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*

type EmployeeNameDescription =
  DescribedAs[
    Match["^[\\p{L}\\p{M}\\s'.-]+$"] & Not[Empty] & MaxLength[100],
    "Employee's name should contain only letters, spaces, hyphens, apostrophes, periods and have a maximum length of 100"
  ]
type EmployeeName = String :| EmployeeNameDescription

object EmployeeName extends RefinedType[String, EmployeeNameDescription]
