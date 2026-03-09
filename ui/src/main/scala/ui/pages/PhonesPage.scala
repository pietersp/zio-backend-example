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
  val $error = Var(Option.empty[String])
  val $loading = Var(true)

  private def loadPhones() = Api.getPhones.onComplete {
    case scala.util.Success(phones) => 
      $phones.set(phones)
      $loading.set(false)
    case scala.util.Failure(ex) => 
      $error.set(Some(ex.getMessage))
      $loading.set(false)
  }

  private def loadEmployees() = Api.getEmployees.onComplete {
    case scala.util.Success(emps) => $employees.set(emps)
    case scala.util.Failure(ex) => $error.set(Some(ex.getMessage))
  }

  val _ = loadPhones()
  val __ = loadEmployees()

  private def filteredPhones: Signal[List[Phone]] =
    $phones.signal.combineWith($searchQuery.signal).map { (phones, query) =>
      if query.isEmpty then phones
      else phones.filter(_.number.contains(query))
    }

  private def employeeName(id: Long): Signal[String] =
    $employees.signal.map(_.find(_.id == id).map(_.name).getOrElse("Unassigned"))

  private def errorMessage: Signal[String] = $error.signal.map(_.getOrElse(""))

  def render = 
    div(
      div(
        cls := "page-header",
        h1("Phones"),
        div(
          cls := "actions-bar",
          input(
            cls := "search-bar",
            typ := "search",
            placeholder := "Search phones...",
            onInput.mapToValue --> $searchQuery
          ),
          button("Add Phone", onClick --> { _ => 
            $editingPhone.set(None); $formNumber.set(""); $formEmpId.set(None); $showModal.set(true) 
          }),
          span(child.text <-- errorMessage, cls := "state-message state-error", display <-- $error.signal.map(e => if e.isDefined then "flex" else "none")),
          span(child.text <-- $loading.signal.map(loading => if loading then "Loading..." else ""), cls := "state-message state-loading", display <-- $loading.signal.map(l => if l then "flex" else "none"))
        )
      ),
      div(
        cls := "table-container",
        table(
          thead(tr(th("ID"), th("Number"), th("Employee"), th("Actions"))),
          tbody(
            children <-- filteredPhones.map: phones =>
              phones.map: phone =>
                tr(
                  td(phone.id.toString),
                  td(phone.number),
                  td(child.text <-- employeeName(phone.employeeId.getOrElse(0))),
                  td(
                    cls := "td-actions",
                    button(cls := "icon-btn outline", "Edit", onClick --> { _ => 
                      $editingPhone.set(Some(phone)); $formNumber.set(phone.number); $formEmpId.set(phone.employeeId); $showModal.set(true) 
                    }),
                    button(cls := "icon-btn danger", "Delete", onClick --> { _ => 
                      Api.deletePhone(phone.id).onComplete {
                        case scala.util.Success(_) => $phones.update(_.filter(_.id != phone.id))
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
          title = if $editingPhone.now().isDefined then "Edit Phone" else "Add Phone",
          onClose = () => { $showModal.set(false); $formNumber.set(""); $formEmpId.set(None); () },
          content = 
            div(
              div(
                cls := "form-group",
                input(
                  typ := "text",
                  placeholder := "Phone Number",
                  value <-- $formNumber,
                  onInput.mapToValue --> $formNumber
                )
              ),
              div(
                cls := "form-group",
                select(
                  option("Unassigned", value := ""),
                  children <-- $employees.signal.map(emps => emps.map(emp => option(value := emp.id.toString, emp.name))),
                  onChange.mapToValue.map(v => if v.isEmpty then None else v.toLongOption) --> $formEmpId
                )
              ),
              button("Save", onClick --> { _ => 
                val number = $formNumber.now()
                val empId = $formEmpId.now()
                $editingPhone.now() match
                  case Some(phone) =>
                    Api.updatePhone(phone.id, number, empId).onComplete {
                      case scala.util.Success(_) =>
                        $phones.update(_.map(p => if p.id == phone.id then p.copy(number = number, employeeId = empId) else p))
                        $showModal.set(false)
                      case scala.util.Failure(ex) => $error.set(Some(ex.getMessage))
                    }
                  case None =>
                    Api.createPhone(number, empId).onComplete {
                      case scala.util.Success(newPhone) =>
                        $phones.update(_ :+ newPhone)
                        $showModal.set(false)
                      case scala.util.Failure(ex) => $error.set(Some(ex.getMessage))
                    }
              })
            )
        )) else Seq.empty
    )
