package ui.pages

import com.raquo.laminar.api.L.*
import ui.api.{Api, Department, Employee}
import ui.components.Modal
import scala.concurrent.ExecutionContext.Implicits.global

object EmployeesPage:
  val $employees = Var(List.empty[Employee])
  val $departments = Var(List.empty[Department])
  val $searchQuery = Var("")
  val $editingEmp = Var[Option[Employee]](None)
  val $showModal = Var(false)
  val $formName = Var("")
  val $formAge = Var(0)
  val $formDeptId = Var(0L)
  val $error = Var(Option.empty[String])
  val $loading = Var(true)

  private def loadEmployees() = Api.getEmployees.onComplete {
    case scala.util.Success(emps) => 
      $employees.set(emps)
      $loading.set(false)
    case scala.util.Failure(ex) => 
      $error.set(Some(ex.getMessage))
      $loading.set(false)
  }

  private def loadDepartments() = Api.getDepartments.onComplete {
    case scala.util.Success(depts) => $departments.set(depts)
    case scala.util.Failure(ex) => $error.set(Some(ex.getMessage))
  }

  val _ = loadEmployees()
  val __ = loadDepartments()

  private def filteredEmployees: Signal[List[Employee]] =
    $employees.signal.combineWith($searchQuery.signal).map { (emps, query) =>
      if query.isEmpty then emps
      else emps.filter(_.name.toLowerCase.contains(query.toLowerCase))
    }

  private def departmentName(id: Long): Signal[String] =
    $departments.signal.map(_.find(_.id == id).map(_.name).getOrElse(""))

  private def errorMessage: Signal[String] = $error.signal.map(_.getOrElse(""))

  def render = 
    div(
      div(
        cls := "page-header",
        h1("Employees"),
        div(
          cls := "actions-bar",
          input(
            cls := "search-bar",
            typ := "search",
            placeholder := "Search employees...",
            onInput.mapToValue --> $searchQuery
          ),
          button("Add Employee", onClick --> { _ => 
            $editingEmp.set(None); $formName.set(""); $formAge.set(0); $formDeptId.set(0L); $showModal.set(true) 
          }),
          span(child.text <-- errorMessage, cls := "state-message state-error", display <-- $error.signal.map(e => if e.isDefined then "flex" else "none")),
          span(child.text <-- $loading.signal.map(loading => if loading then "Loading..." else ""), cls := "state-message state-loading", display <-- $loading.signal.map(l => if l then "flex" else "none"))
        )
      ),
      div(
        cls := "table-container",
        table(
          thead(tr(th("ID"), th("Name"), th("Age"), th("Department"), th("Actions"))),
          tbody(
            children <-- filteredEmployees.map: emps =>
              emps.map: emp =>
                tr(
                  td(emp.id.toString),
                  td(emp.name),
                  td(emp.age.toString),
                  td(child.text <-- departmentName(emp.departmentId)),
                  td(
                    cls := "td-actions",
                    button(cls := "icon-btn outline", "Edit", onClick --> { _ => 
                      $editingEmp.set(Some(emp)); $formName.set(emp.name); $formAge.set(emp.age); $formDeptId.set(emp.departmentId); $showModal.set(true) 
                    }),
                    button(cls := "icon-btn danger", "Delete", onClick --> { _ => 
                      Api.deleteEmployee(emp.id).onComplete {
                        case scala.util.Success(_) => $employees.update(_.filter(_.id != emp.id))
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
          title = if $editingEmp.now().isDefined then "Edit Employee" else "Add Employee",
          onClose = () => { $showModal.set(false); $formName.set(""); $formAge.set(0); $formDeptId.set(0L); () },
          content = 
            div(
              div(
                cls := "form-group",
                input(
                  typ := "text",
                  placeholder := "Employee Name",
                  value <-- $formName,
                  onInput.mapToValue --> $formName
                )
              ),
              div(
                cls := "form-group",
                input(
                  typ := "number",
                  placeholder := "Age",
                  defaultValue := $formAge.now().toString,
                  onInput.mapToValue.map(_.toIntOption.getOrElse(0)) --> $formAge
                )
              ),
              div(
                cls := "form-group",
                select(
                  option("Select Department", disabled := true, selected := true),
                  children <-- $departments.signal.map(depts => depts.map(dept => option(value := dept.id.toString, dept.name))),
                  onChange.mapToValue.map(_.toLongOption.getOrElse(0L)) --> $formDeptId
                )
              ),
              button("Save", onClick --> { _ => 
                val name = $formName.now()
                val age = $formAge.now()
                val deptId = $formDeptId.now()
                $editingEmp.now() match
                  case Some(emp) => 
                    Api.updateEmployee(emp.id, name, age, deptId).onComplete {
                      case scala.util.Success(_) =>
                        $employees.update(_.map(e => if e.id == emp.id then e.copy(name = name, age = age, departmentId = deptId) else e))
                        $showModal.set(false)
                      case scala.util.Failure(ex) => $error.set(Some(ex.getMessage))
                    }
                  case None =>
                    Api.createEmployee(name, age, deptId).onComplete {
                      case scala.util.Success(newEmp) =>
                        $employees.update(_ :+ newEmp)
                        $showModal.set(false)
                      case scala.util.Failure(ex) => $error.set(Some(ex.getMessage))
                    }
              })
            )
        )) else Seq.empty
    )
