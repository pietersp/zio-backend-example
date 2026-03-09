package ui.pages

import com.raquo.laminar.api.L.*
import ui.api.{Api, Department}
import ui.components.Modal
import scala.concurrent.ExecutionContext.Implicits.global

object DepartmentsPage:
  val $departments = Var(List.empty[Department])
  val $searchQuery = Var("")
  val $editingDept = Var[Option[Department]](None)
  val $showModal = Var(false)
  val $formName = Var("")

  // Load departments on mount
  val _ = Api.getDepartments.foreach { depts => $departments.set(depts) }

  def render = 
    div(
      h1("Departments"),
      input(
        typ := "search",
        placeholder := "Search departments...",
        onInput.map(_.target.value) --> $searchQuery
      ),
      button("Add Department", onClick --> { _ => $editingDept.set(None); $formName.set(""); $showModal.set(true) }),
      table(
        thead(tr(th("ID"), th("Name"), th("Actions"))),
        tbody(
          $departments.map(_.filter(d => $searchQuery.now.isEmpty || d.name.toLowerCase.contains($searchQuery.now.toLowerCase))).map: filtered =>
            filtered.map: dept =>
              tr(
                td(dept.id.toString),
                td(dept.name),
                td(
                  button("Edit", onClick --> { _ => $editingDept.set(Some(dept)); $formName.set(dept.name); $showModal.set(true) }),
                  button("Delete", onClick --> { _ => Api.deleteDepartment(dept.id).foreach { _ => $departments.update(_.filter(_.id != dept.id)) } })
                )
              )
        )
      ),
      children <-- $showModal.map: show =>
        if show then Seq(Modal.render(
          title = if $editingDept.now.isDefined then "Edit Department" else "Add Department",
          onClose = () => { $showModal.set(false); $formName.set("") },
          content = 
            div(
              input(
                typ := "text",
                placeholder := "Department Name",
                value <-- $formName,
                onInput.map(_.target.value) --> $formName
              ),
              button("Save", onClick --> { _ => 
                val name = $formName.now
                val future = $editingDept.now match
                  case Some(dept) => Api.updateDepartment(dept.id, name)
                  case None => Api.createDepartment(name)
                future.foreach: newDept =>
                  $departments.update: deps =>
                    $editingDept.now match
                      case Some(_) => deps.map(d => if d.id == newDept.id then newDept else d)
                      case None => deps :+ newDept
                  $showModal.set(false)
              })
            )
        )) else Seq.empty
    )
