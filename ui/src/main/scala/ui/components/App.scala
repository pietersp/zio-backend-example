package ui.components

import com.raquo.laminar.api.L.*
import ui.pages.*

object App:
  val component = 
    div(
      cls := "layout",
      Navigation.render,
      div(
        cls := "main-content",
        child <-- Navigation.$currentPage.signal.map:
          case "departments" => DepartmentsPage.render
          case "employees" => EmployeesPage.render
          case "phones" => PhonesPage.render
          case other => div(s"Unknown page: $other")
      )
    )
