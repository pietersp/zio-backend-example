package com.example.api

import com.example.api.endpoint.{DepartmentEndpoints, EmployeeEndpoints, EmployeePhoneEndpoints, PhoneEndpoints}
import com.example.api.handler.{DepartmentHandlers, EmployeeHandlers, EmployeePhoneHandlers, PhoneHandlers}
import com.example.service.{DepartmentService, EmployeePhoneService, EmployeeService, PhoneService}
import zio.http.endpoint.openapi.{OpenAPIGen, SwaggerUI}
import zio.http.{Routes, *}

trait Router
    extends DepartmentEndpoints
    with DepartmentHandlers
    with EmployeeEndpoints
    with EmployeeHandlers
    with PhoneEndpoints
    with PhoneHandlers
    with EmployeePhoneEndpoints
    with EmployeePhoneHandlers {

  private val departmentRoutes: Routes[DepartmentService, Nothing] =
    Routes(
      createDepartment.implementHandler(handler(createDepartmentHandler)),
      getDepartments.implementHandler(handler(getDepartmentsHandler)),
      getDepartmentById.implementHandler(handler(getDepartmentHandler)),
      updateDepartment.implementHandler(handler(updateDepartmentHandler)),
      deleteDepartment.implementHandler(handler(deleteDepartmentHandler))
    )

  private val employeeRoutes: Routes[EmployeeService, Nothing] =
    Routes(
      createEmployee.implementHandler(handler(createEmployeeHandler)),
      getEmployees.implementHandler(handler(getEmployeesHandler)),
      getEmployeeById.implementHandler(handler(getEmployeeHandler)),
      updateEmployee.implementHandler(handler(updateEmployeeHandler)),
      deleteEmployee.implementHandler(handler(deleteEmployeeHandler))
    )

  private val phoneRoutes =
    Routes(
      createPhone.implementHandler(handler(createPhoneHandler)),
      getPhoneById.implementHandler(handler(getPhoneHandler)),
      updatePhone.implementHandler(handler(updatePhoneHandler)),
      deletePhone.implementHandler(handler(deletePhoneHandler))
    )

  private val employeePhoneRoutes =
    Routes(
      addPhoneToEmployee.implementHandler(handler(addPhoneToEmployeeHandler)),
      retrieveEmployeePhones.implementHandler(
        handler(retrieveEmployeePhonesHandler)
      ),
      removePhoneFromEmployee.implementHandler(
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
        createDepartment,
        getDepartments,
        getDepartmentById,
        updateDepartment,
        deleteDepartment,
        createEmployee,
        getEmployeeById,
        updateEmployee,
        deleteEmployee,
        createPhone,
        getPhoneById,
        updatePhone,
        deletePhone,
        addPhoneToEmployee,
        retrieveEmployeePhones,
        removePhoneFromEmployee
      )
    )

}
