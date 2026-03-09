package ui.pages

import com.raquo.laminar.api.L.*
import ui.api.{Api, Department}
import ui.components.Modal
import scala.concurrent.ExecutionContext.Implicits.global

object DepartmentsPage:
  val $departments = Var(List.empty[Department])
  val $searchQuery = Var("")
  val $showModal = Var(false)
  val $formName = Var("")
  val $error = Var(Option.empty[String])
  val $loading = Var(true)

  val _ = Api.getDepartments.onComplete {
    case scala.util.Success(depts) => 
      $departments.set(depts)
      $loading.set(false)
    case scala.util.Failure(ex) => 
      $error.set(Some(ex.getMessage))
      $loading.set(false)
  }

  def refresh() = 
    $loading.set(true)
    Api.getDepartments.onComplete {
      case scala.util.Success(depts) => 
        $departments.set(depts)
        $loading.set(false)
      case scala.util.Failure(ex) => 
        $error.set(Some(ex.getMessage))
        $loading.set(false)
    }

  private def filteredDepartments: Signal[List[Department]] =
    $departments.signal.combineWith($searchQuery.signal).map { (depts, query) =>
      if query.isEmpty then depts
      else depts.filter(_.name.toLowerCase.contains(query.toLowerCase))
    }

  private def errorMessage: Signal[String] = $error.signal.map(_.getOrElse(""))

  def render = 
    div(
      div(
        cls := "page-header",
        h1("Departments"),
        div(
          cls := "actions-bar",
          input(
            cls := "search-bar",
            typ := "search",
            placeholder := "Search departments...",
            onInput.mapToValue --> $searchQuery
          ),
          button("Add Department", onClick --> { _ => $formName.set(""); $showModal.set(true) }),
          span(child.text <-- errorMessage, cls := "state-message state-error", display <-- $error.signal.map(e => if e.isDefined then "flex" else "none")),
          span(child.text <-- $loading.signal.map(loading => if loading then "Loading..." else ""), cls := "state-message state-loading", display <-- $loading.signal.map(l => if l then "flex" else "none"))
        )
      ),
      div(
        cls := "table-container",
        table(
          thead(tr(th("Name"), th("Actions"))),
          tbody(
            children <-- filteredDepartments.map: depts =>
              depts.map: dept =>
                tr(
                  td(dept.name),
                  td(
                    cls := "td-actions",
                    button(cls := "icon-btn outline", "Edit", onClick --> { _ => $formName.set(dept.name); $showModal.set(true) }),
                    button(cls := "icon-btn danger", "Delete", onClick --> { _ => 
                      Api.deleteDepartment(dept.id).onComplete {
                        case scala.util.Success(_) => refresh()
                        case scala.util.Failure(ex) => $error.set(Some(ex.getMessage))
                      }
                    })
                  )
                )
          )
        )
      ),
      children <-- $showModal.signal.map: show =>
        if show then Seq(Modal.render(
          title = "Add Department",
          onClose = () => { $showModal.set(false); $formName.set(""); () },
          content = 
            div(
              div(
                cls := "form-group",
                input(
                  typ := "text",
                  placeholder := "Department Name",
                  value <-- $formName,
                  onInput.mapToValue --> $formName
                )
              ),
              button("Save", onClick --> { _ => 
                val name = $formName.now()
                Api.createDepartment(name).onComplete {
                  case scala.util.Success(_) =>
                    $showModal.set(false)
                    $formName.set("")
                    refresh()
                  case scala.util.Failure(ex) =>
                    $error.set(Some(ex.getMessage))
                }
              })
            )
        )) else Seq.empty
    )
