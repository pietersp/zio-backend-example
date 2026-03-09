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

  val _ = Api.getEmployees.foreach { emps => $employees.set(emps) }
  val __ = Api.getDepartments.foreach { depts => $departments.set(depts) }

  def render = 
    div(
      h1("Employees"),
      input(
        typ := "search",
        placeholder := "Search employees...",
        onInput.map(_.target.value) --> $searchQuery
      ),
      button("Add Employee", onClick --> { _ => 
        $editingEmp.set(None); $formName.set(""); $formAge.set(0); $formDeptId.set(0L); $showModal.set(true) 
      }),
      table(
        thead(tr(th("ID"), th("Name"), th("Age"), th("Department"), th("Actions"))),
        tbody(
          $employees.map(_.filter(e => $searchQuery.now.isEmpty || e.name.toLowerCase.contains($searchQuery.now.toLowerCase()))).map: filtered =>
            filtered.map: emp =>
              tr(
                td(emp.id.toString),
                td(emp.name),
                td(emp.age.toString),
                td($departments.find(_.id == emp.departmentId).map(_.name).getOrElse("")),
                td(
                  button("Edit", onClick --> { _ => 
                    $editingEmp.set(Some(emp)); $formName.set(emp.name); $formAge.set(emp.age); $formDeptId.set(emp.departmentId); $showModal.set(true) 
                  }),
                  button("Delete", onClick --> { _ => Api.deleteEmployee(emp.id).foreach { _ => $employees.update(_.filter(_.id != emp.id)) } })
                )
              )
        )
      ),
      children <-- $showModal.map: show =>
        if show then Seq(Modal.render(
          title = if $editingEmp.now.isDefined then "Edit Employee" else "Add Employee",
          onClose = () => $showModal.set(false),
          content = 
            div(
              input(
                typ := "text",
                placeholder := "Employee Name",
                value <-- $formName,
                onInput.map(_.target.value) --> $formName
              ),
              input(
                typ := "number",
                placeholder := "Age",
                value <-- $formAge.map(_.toString),
                onInput.map(_.target.value.toIntOption.getOrElse(0)) --> $formAge
              ),
              select(
                option("Select Department", disabled := true, selected := true),
                $departments.now.map(dept => option(value := dept.id.toString, dept.name)),
                onChange.map(_.target.value.toLongOption.getOrElse(0L)) --> $formDeptId
              ),
              button("Save", onClick --> { _ => 
                val name = $formName.now
                val age = $formAge.now
                val deptId = $formDeptId.now
                val future = $editingEmp.now match
                  case Some(emp) => Api.updateEmployee(emp.id, name, age, deptId)
                  case None => Api.createEmployee(name, age, deptId)
                future.foreach: newEmp =>
                  $employees.update: emps =>
                    $editingEmp.now match
                      case Some(_) => emps.map(e => if e.id == newEmp.id then newEmp else e)
                      case None => emps :+ newEmp
                  $showModal.set(false)
              })
            )
        )) else Seq.empty
    )
