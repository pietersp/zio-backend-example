package ui.components

import com.raquo.laminar.api.L.*
import ui.pages.*

object Navigation:
  val $currentPage = Var("departments")

  def render = 
    nav(
      cls := "nav-sidebar",
      ul(
        li(a("Departments", href := "#", onClick --> { _ => $currentPage.set("departments") })),
        li(a("Employees", href := "#", onClick --> { _ => $currentPage.set("employees") })),
        li(a("Phones", href := "#", onClick --> { _ => $currentPage.set("phones") }))
      )
    )
