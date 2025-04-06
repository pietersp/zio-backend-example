package com.example.repository

import com.example.domain.{Department, DepartmentId, DepartmentName}
import zio.*

trait DepartmentRepository {
  def create(department: Department): UIO[DepartmentId]
  def retrieve(departmentId: DepartmentId): UIO[Option[Department]]
  def retrieveByName(departmentName: DepartmentName): UIO[Option[Department]]
  def retrieveAll: UIO[Vector[Department]]
  def update(departmentId: DepartmentId, department: Department): UIO[Unit]
  def delete(departmentId: DepartmentId): UIO[Unit]
}
