package ui

import org.scalajs.dom
import com.raquo.laminar.api.L.*
import ui.components.App

@main
def main(): Unit =
  val appContainer = dom.document.querySelector("#app")
  appContainer.innerHTML = ""
  render(appContainer, App.component)
