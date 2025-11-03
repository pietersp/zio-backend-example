package com.example.api

import com.example.api.endpoint.*
import com.example.api.handler.{
  DepartmentHandlers,
  EmployeeHandlers,
  EmployeePhoneHandlers,
  PhoneHandlers
}
import com.example.service.{
  DepartmentService,
  EmployeePhoneService,
  EmployeeService,
  PhoneService
}
import zio.http.endpoint.openapi.{OpenAPIGen, SwaggerUI}
import zio.http.{Routes, *}

trait Router
    extends DepartmentHandlers
    with EmployeeHandlers
    with PhoneHandlers
    with EmployeePhoneHandlers {

  private val departmentRoutes =
    Routes(
      DepartmentEndpoints.createDepartment.implementHandler(
        handler(createDepartmentHandler)
      ),
      DepartmentEndpoints.getDepartments.implementHandler(
        handler(getDepartmentsHandler)
      ),
      DepartmentEndpoints.getDepartmentById.implementHandler(
        handler(getDepartmentHandler)
      ),
      DepartmentEndpoints.updateDepartment.implementHandler(
        handler(updateDepartmentHandler)
      ),
      DepartmentEndpoints.deleteDepartment.implementHandler(
        handler(deleteDepartmentHandler)
      )
    )

  private val employeeRoutes =
    Routes(
      EmployeeEndpoints.createEmployee.implementHandler(
        handler(createEmployeeHandler)
      ),
      EmployeeEndpoints.getEmployees.implementHandler(
        handler(getEmployeesHandler)
      ),
      EmployeeEndpoints.getEmployeeById.implementHandler(
        handler(getEmployeeHandler)
      ),
      EmployeeEndpoints.updateEmployee.implementHandler(
        handler(updateEmployeeHandler)
      ),
      EmployeeEndpoints.deleteEmployee.implementHandler(
        handler(deleteEmployeeHandler)
      )
    )

  private val phoneRoutes =
    Routes(
      PhoneEndpoints.createPhone.implementHandler(handler(createPhoneHandler)),
      PhoneEndpoints.getPhoneById.implementHandler(handler(getPhoneHandler)),
      PhoneEndpoints.updatePhone.implementHandler(handler(updatePhoneHandler)),
      PhoneEndpoints.deletePhone.implementHandler(handler(deletePhoneHandler))
    )

  private val employeePhoneRoutes =
    Routes(
      EmployeePhoneEndpoints.addPhoneToEmployee.implementHandler(
        handler(addPhoneToEmployeeHandler)
      ),
      EmployeePhoneEndpoints.retrieveEmployeePhones.implementHandler(
        handler(retrieveEmployeePhonesHandler)
      ),
      EmployeePhoneEndpoints.removePhoneFromEmployee.implementHandler(
        handler(removePhoneFromEmployeeHandler)
      )
    )
  val routes: Routes[
    EmployeePhoneService & PhoneService & EmployeeService & DepartmentService,
    Nothing
  ] =
    departmentRoutes ++ employeeRoutes ++ phoneRoutes ++ employeePhoneRoutes

  val swaggerRoutes =
    SwaggerUI.routes(
      "docs",
      OpenAPIGen.fromEndpoints(
        DepartmentEndpoints.createDepartment,
        DepartmentEndpoints.getDepartments,
        DepartmentEndpoints.getDepartmentById,
        DepartmentEndpoints.updateDepartment,
        DepartmentEndpoints.deleteDepartment,
        EmployeeEndpoints.createEmployee,
        EmployeeEndpoints.getEmployeeById,
        EmployeeEndpoints.updateEmployee,
        EmployeeEndpoints.deleteEmployee,
        PhoneEndpoints.createPhone,
        PhoneEndpoints.getPhoneById,
        PhoneEndpoints.updatePhone,
        PhoneEndpoints.deletePhone,
        EmployeePhoneEndpoints.addPhoneToEmployee,
        EmployeePhoneEndpoints.retrieveEmployeePhones,
        EmployeePhoneEndpoints.removePhoneFromEmployee
      )
    )

}
