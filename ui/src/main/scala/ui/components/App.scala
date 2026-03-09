package ui.components

import com.raquo.laminar.api.L.*
import ui.pages.*

object App:
  val component = 
    div(
      cls := "container-fluid",
      Navigation.render,
      div(
        cls := "main-content",
        child <-- Navigation.$currentPage.map:
          case "departments" => DepartmentsPage.render
          case "employees" => EmployeesPage.render
          case "phones" => PhonesPage.render
          case other => div(s"Unknown page: $other")
      )
    )
