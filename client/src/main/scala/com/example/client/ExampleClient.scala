package com.example.client

import zio.*
import com.example.api.endpoint.DepartmentEndpoints
import com.example.domain.{Department, DepartmentId}
import com.example.error.AppError.{DepartmentAlreadyExists, DepartmentNotFound}
import zio.http.{Client, URL}
import zio.http.endpoint.{EndpointExecutor, EndpointLocator}

final case class ExampleClient(
  client: Client,
  clientConfig: ExampleClient.MyConfig,
  departmentEndpoints: DepartmentEndpoints
) {
  private val locator =
    EndpointLocator.fromURL(URL.decode(clientConfig.url).toOption.get)
  private val executor: EndpointExecutor[Any, Unit, Scope] =
    EndpointExecutor(client, locator)

  def createDepartment(
    department: Department
  ): IO[DepartmentAlreadyExists, DepartmentId] =
    ZIO.scoped(executor(departmentEndpoints.createDepartment(department)))

  def getDepartments: UIO[Vector[Department]] =
    ZIO.scoped(executor(departmentEndpoints.getDepartments(())))

  def getDepartmentById(id: DepartmentId): IO[DepartmentNotFound, Department] =
    ZIO.scoped(executor(departmentEndpoints.getDepartmentById(id)))

  def updateDepartment(
    id: DepartmentId,
    department: Department
  ): IO[DepartmentNotFound, Unit] =
    ZIO.scoped(executor(departmentEndpoints.updateDepartment(id, department)))

  def deleteDepartment(id: DepartmentId): UIO[Unit] =
    ZIO.scoped(executor(departmentEndpoints.deleteDepartment(id)))


}

object ExampleClient {
  case class MyConfig(url: String, timeout: Int, retries: Int)

  object MyConfig {

    import zio.Config.*
    import zio.ConfigProvider
    val config: Config[MyConfig] =
      (string("url") ++ int("timeout") ++ int("retries"))
        .map(MyConfig(_, _, _))

    val defaultLayer: ULayer[IO[Error, MyConfig]] =
      ZLayer.succeed(
        ConfigProvider
          .fromMap(
            Map(
              "url" -> "http://localhost:8080",
              "timeout" -> "3",
              "retries" -> "5"
            )
          )
          .load(config)
      )
  }
}
