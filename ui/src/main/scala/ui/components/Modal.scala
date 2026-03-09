package ui.components

import com.raquo.laminar.api.L.*

object Modal:
  def render(title: String, onClose: => Unit, content: HtmlElement): HtmlElement =
    div(
      cls := "modal is-active",
      div(
        cls := "modal-background",
        onClick --> { _ => onClose }
      ),
      div(
        cls := "modal-card",
        header(cls := "modal-card-head",
          p(cls := "modal-card-title", title),
          button(cls := "delete", ariaLabel := "close", onClick --> { _ => onClose })
        ),
        section(cls := "modal-card-body", content),
        footer(cls := "modal-card-foot")
      )
    )
