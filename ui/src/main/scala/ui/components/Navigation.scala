package ui.components

import com.raquo.laminar.api.L.*

object Navigation:
  val $currentPage = Var("departments")

  def render = 
    div(
      cls := "nav-sidebar",
      ul(
        li(a("Departments", 
          cls <-- $currentPage.signal.map(p => if p == "departments" then "active" else ""),
          onClick.preventDefault --> { _ => $currentPage.set("departments") }
        )),
        li(a("Employees", 
          cls <-- $currentPage.signal.map(p => if p == "employees" then "active" else ""),
          onClick.preventDefault --> { _ => $currentPage.set("employees") }
        )),
        li(a("Phones", 
          cls <-- $currentPage.signal.map(p => if p == "phones" then "active" else ""),
          onClick.preventDefault --> { _ => $currentPage.set("phones") }
        ))
      )
    )
