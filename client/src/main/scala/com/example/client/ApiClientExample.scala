package com.example.client

import zio.*
import zio.http.Client
import com.example.domain.*

/**
 * Example usage of the ApiClient.
 * 
 * This demonstrates how to:
 * 1. Set up the client with configuration
 * 2. Make API calls to various endpoints
 * 3. Handle errors properly
 */
object ApiClientExample extends ZIOAppDefault {

  val exampleProgram: ZIO[ApiClient, Throwable, Unit] =
    for {
      client <- ZIO.service[ApiClient]
      
      // Create a department
      _ <- Console.printLine("Creating department...")
      departmentId <- client.createDepartment(
        Department(DepartmentName("Engineering"))
      ).catchAll(error => 
        Console.printLine(s"Failed to create department: $error") *> ZIO.fail(new RuntimeException("Department creation failed"))
      )
      _ <- Console.printLine(s"Created department with ID: $departmentId")
      
      // Get all departments
      _ <- Console.printLine("\nFetching all departments...")
      departments <- client.getDepartments
      _ <- Console.printLine(s"Found ${departments.size} departments")
      
      // Create an employee
      _ <- Console.printLine("\nCreating employee...")
      employeeId <- client.createEmployee(
        Employee(
          name = EmployeeName("JohnDoe"),
          age = Age(30),
          departmentId = departmentId
        )
      ).catchAll(error =>
        Console.printLine(s"Failed to create employee: $error") *> ZIO.fail(new RuntimeException("Employee creation failed"))
      )
      _ <- Console.printLine(s"Created employee with ID: $employeeId")
      
      // Get employee by ID
      _ <- Console.printLine("\nFetching employee by ID...")
      employee <- client.getEmployeeById(employeeId).catchAll(error =>
        Console.printLine(s"Employee not found: $error") *> ZIO.fail(new RuntimeException("Employee fetch failed"))
      )
      _ <- Console.printLine(s"Employee: ${employee.name}")
      
      // Create a phone
      _ <- Console.printLine("\nCreating phone...")
      phoneId <- client.createPhone(
        Phone(PhoneNumber("1234567890"))
      ).catchAll(error =>
        Console.printLine(s"Failed to create phone: $error") *> ZIO.fail(new RuntimeException("Phone creation failed"))
      )
      _ <- Console.printLine(s"Created phone with ID: $phoneId")
      
      // Associate phone with employee
      _ <- Console.printLine("\nAdding phone to employee...")
      _ <- client.addPhoneToEmployee(employeeId, phoneId).catchAll(error =>
        Console.printLine(s"Failed to add phone to employee: $error") *> ZIO.fail(new RuntimeException("Phone association failed"))
      )
      _ <- Console.printLine("Phone added to employee successfully")
      
      // Get employee phones
      _ <- Console.printLine("\nFetching employee phones...")
      phones <- client.getEmployeePhones(employeeId).catchAll(error =>
        Console.printLine(s"Failed to fetch phones: $error") *> ZIO.fail(new RuntimeException("Phone fetch failed"))
      )
      _ <- Console.printLine(s"Employee has ${phones.size} phone(s)")
      
      _ <- Console.printLine("\n✓ Example completed successfully!")
    } yield ()

  override def run: ZIO[Any, Any, Unit] =
    exampleProgram.provide(
      ApiClientLive.layer,
      ApiClientConfig.localLayer,
      Client.default
    ).catchAll(error =>
      Console.printLine(s"Error: ${error.getMessage}").orDie
    )
}
