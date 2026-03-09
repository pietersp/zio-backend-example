package ui.components

import com.raquo.laminar.api.L.*
import ui.pages.*

object Navigation:
  val $currentPage = Var("departments")

  def render = 
    nav(
      cls := "nav-sidebar",
      ul(
        li(a("Departments", onClick.preventDefault --> { _ => $currentPage.set("departments") })),
        li(a("Employees", onClick.preventDefault --> { _ => $currentPage.set("employees") })),
        li(a("Phones", onClick.preventDefault --> { _ => $currentPage.set("phones") }))
      )
    )
