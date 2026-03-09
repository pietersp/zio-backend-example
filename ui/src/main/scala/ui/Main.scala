package ui

import org.scalajs.dom
import com.raquo.laminar.api.L.*

@main
def main(): Unit =
  val appContainer = dom.document.querySelector("#app")
  appContainer.innerHTML = ""
  appContainer.innerHTML = "<h1>Loading...</h1>"
