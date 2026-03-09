package ui.pages

import com.raquo.laminar.api.L.*
import ui.api.{Api, Employee, Phone}
import ui.components.Modal
import scala.concurrent.ExecutionContext.Implicits.global

object PhonesPage:
  val $phones = Var(List.empty[Phone])
  val $employees = Var(List.empty[Employee])
  val $searchQuery = Var("")
  val $editingPhone = Var[Option[Phone]](None)
  val $showModal = Var(false)
  val $formNumber = Var("")
  val $formEmpId = Var[Option[Long]](None)

  val _ = Api.getPhones.foreach { phones => $phones.set(phones) }
  val __ = Api.getEmployees.foreach { emps => $employees.set(emps) }

  def render = 
    div(
      h1("Phones"),
      input(
        typ := "search",
        placeholder := "Search phones...",
        onInput.map(_.target.value) --> $searchQuery
      ),
      button("Add Phone", onClick --> { _ => 
        $editingPhone.set(None); $formNumber.set(""); $formEmpId.set(None); $showModal.set(true) 
      }),
      table(
        thead(tr(th("ID"), th("Number"), th("Employee"), th("Actions"))),
        tbody(
          $phones.map(_.filter(p => $searchQuery.now.isEmpty || p.number.contains($searchQuery.now))).map: filtered =>
            filtered.map: phone =>
              tr(
                td(phone.id.toString),
                td(phone.number),
                td(phone.employeeId.flatMap(eid => $employees.find(_.id == eid)).map(_.name).getOrElse("Unassigned")),
                td(
                  button("Edit", onClick --> { _ => 
                    $editingPhone.set(Some(phone)); $formNumber.set(phone.number); $formEmpId.set(phone.employeeId); $showModal.set(true) 
                  }),
                  button("Delete", onClick --> { _ => Api.deletePhone(phone.id).foreach { _ => $phones.update(_.filter(_.id != phone.id)) } })
                )
              )
        )
      ),
      children <-- $showModal.map: show =>
        if show then Seq(Modal.render(
          title = if $editingPhone.now.isDefined then "Edit Phone" else "Add Phone",
          onClose = () => { $showModal.set(false); $formNumber.set(""); $formEmpId.set(None) },
          content = 
            div(
              input(
                typ := "text",
                placeholder := "Phone Number",
                value <-- $formNumber,
                onInput.map(_.target.value) --> $formNumber
              ),
              select(
                option("Unassigned", value := ""),
                children <-- $employees.map(emps => emps.map(emp => option(value := emp.id.toString, emp.name))),
                onChange.map(s => if s.target.value.isEmpty then None else s.target.value.toLongOption) --> $formEmpId
              ),
              button("Save", onClick --> { _ => 
                val number = $formNumber.now
                val empId = $formEmpId.now
                val future = $editingPhone.now match
                  case Some(phone) => Api.updatePhone(phone.id, number, empId)
                  case None => Api.createPhone(number, empId)
                future.foreach: newPhone =>
                  $phones.update: phones =>
                    $editingPhone.now match
                      case Some(_) => phones.map(p => if p.id == newPhone.id then newPhone else p)
                      case None => phones :+ newPhone
                  $showModal.set(false)
              })
            )
        )) else Seq.empty
    )
