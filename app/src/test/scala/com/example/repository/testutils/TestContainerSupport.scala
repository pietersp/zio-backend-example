package com.example.repository.testutils

import com.augustnagro.magnum.magzio.*
import com.example.tables
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import org.testcontainers.containers.PostgreSQLContainer
import zio.*
import zio.test.{Spec, TestAspect, suite}

object TestContainerSupport {

  /** Starts a PostgreSQL testcontainer and returns it as a scoped resource */
  val startPostgresContainer: ZIO[Scope, Throwable, PostgreSQLContainer[?]] =
    ZIO.fromAutoCloseable {
      ZIO.attemptBlockingIO {
        val container = PostgreSQLContainer("postgres:13.18-alpine3.20")
        container.withDatabaseName("test")
        container.withUsername("test")
        container.withPassword("test")
        container.start()
        container
      }
    }

  /** Creates a HikariCP datasource from connection details */
  def createDataSource(
    jdbcUrl: String,
    username: String,
    password: String
  ): ZIO[Scope, Throwable, HikariDataSource] =
    ZIO.fromAutoCloseable {
      ZIO.attemptBlockingIO {
        val config = HikariConfig()
        config.setJdbcUrl(jdbcUrl)
        config.setUsername(username)
        config.setPassword(password)
        HikariDataSource(config)
      }
    }

  /** Creates all database tables needed for testing */
  def createTables(xa: Transactor): Task[Unit] =
    xa.transact {
      val departmentTable =
        sql"""
           CREATE TABLE ${tables.Department.table}(
             ${tables.Department.table.id}   SERIAL      NOT NULL,
             ${tables.Department.table.name} VARCHAR(50) NOT NULL,
             PRIMARY KEY(${tables.Department.table.id})
           )
       """

      val employeeTable =
        sql"""
           CREATE TABLE ${tables.Employee.table}(
             ${tables.Employee.table.id}            SERIAL       NOT NULL,
             ${tables.Employee.table.name}          VARCHAR(100) NOT NULL,
             ${tables.Employee.table.age}           INT          NOT NULL,
             ${tables.Employee.table.departmentId}  INT          NOT NULL,
             PRIMARY KEY (${tables.Employee.table.id}),
             FOREIGN KEY (${tables.Employee.table.departmentId})
               REFERENCES ${tables.Department.table}(${tables.Department.table.id})
                 ON DELETE CASCADE
           )
       """

      val phoneTable =
        sql"""
           CREATE TABLE ${tables.Phone.table}(
             ${tables.Phone.table.id}    SERIAL      NOT NULL,
             ${tables.Phone.table.number} VARCHAR(15) NOT NULL,
             PRIMARY KEY(${tables.Phone.table.id})
           )
       """

      val employeePhoneTable =
        sql"""
           CREATE TABLE ${tables.EmployeePhone.table}(
             ${tables.EmployeePhone.table.employeeId} INT NOT NULL,
             ${tables.EmployeePhone.table.phoneId}    INT NOT NULL,
             PRIMARY KEY (${tables.EmployeePhone.table.employeeId}, ${tables.EmployeePhone.table.phoneId}),
             FOREIGN KEY (${tables.EmployeePhone.table.employeeId})
               REFERENCES ${tables.Employee.table}(${tables.Employee.table.id})
                 ON DELETE CASCADE,
             FOREIGN KEY (${tables.EmployeePhone.table.phoneId})
               REFERENCES ${tables.Phone.table}(${tables.Phone.table.id})
                 ON DELETE CASCADE
           )
       """

      departmentTable.update.run()
      employeeTable.update.run()
      phoneTable.update.run()
      employeePhoneTable.update.run()
    }

  /** Truncates all tables to leave a clean state for the next test */
  def cleanupTables(xa: Transactor): Task[Unit] =
    xa.transact(
      sql"""
         TRUNCATE TABLE
           ${tables.EmployeePhone.table},
           ${tables.Employee.table},
           ${tables.Phone.table},
           ${tables.Department.table}
         RESTART IDENTITY CASCADE
       """.update.run()
    )

  /** A ZIO Test aspect that cleans the database before each test */
  val cleanDb: TestAspect[Nothing, Transactor, Throwable, Any] = 
    TestAspect.before(ZIO.serviceWithZIO[Transactor](xa => cleanupTables(xa)))

  /** Creates a test database layer with testcontainer and schema setup */
  val testDbLayer: ZLayer[Any, Throwable, Transactor] = {
    val dataSourceLayer = ZLayer.scoped {
      for {
        postgresContainer <- startPostgresContainer
        dataSource <- createDataSource(
          postgresContainer.getJdbcUrl,
          postgresContainer.getUsername,
          postgresContainer.getPassword
        )
      } yield dataSource
    }

    for {
      dataSource <- dataSourceLayer
      xa <- Transactor.layer(dataSource.get)
      _ <- ZLayer.fromZIO(createTables(xa.get))
    } yield xa
  }
}
