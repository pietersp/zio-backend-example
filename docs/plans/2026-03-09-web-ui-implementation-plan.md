# Web UI Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add a Scala.js + Laminar web UI for full CRUD management of Departments, Employees, and Phones.

**Architecture:** Separate `ui/` project that depends on `domain` JAR. Browser calls REST API via fetch. Backend serves static assets and has CORS enabled.

**Tech Stack:** Scala.js 1.x, Laminar, Vite (bundler), ZIO HTTP backend

---

## Task 1: Add UI project to build.sbt

**Files:**
- Modify: `build.sbt`

**Step 1: Add UI project definition**

In `build.sbt` after line 103 (endpoints definition), add:

```scala
lazy val ui = (project in file("ui"))
  .settings(
    name := s"$projectName-ui",
    scalaVersion := "3.6.4",
    scalacOptions ++= Seq(
      "-Wunused:imports"
    ),
    libraryDependencies ++= Seq(
      "org.scala-js" %% "scalajs-dom" % "1.2.0",
      "com.raquo" %% "laminar" % "20.0.0-2"
    ),
    scalaJSUseMainModuleInitializer := true
  )
  .enablePlugins(ScalaJSPlugin)
```

After line 23 (root aggregate), add `ui` to aggregate:
```scala
.aggregate(app, client, core, domain, endpoints, ui)
```

**Step 2: Commit**

```bash
git add build.sbt
git commit -m "feat: add ui project to build.sbt"
```

---

## Task 2: Create UI project structure

**Files:**
- Create: `ui/build.sbt`
- Create: `ui/src/main/scala/ui/Main.scala`
- Create: `ui/src/main/scala/ui/LaminarApp.scala`
- Create: `ui/src/main/scala/ui/api/Api.scala`
- Create: `ui/src/main/scala/ui/components/App.scala`
- Create: `ui/src/main/scala/ui/components/Navigation.scala`
- Create: `ui/src/main/scala/ui/pages/DepartmentsPage.scala`
- Create: `ui/src/main/scala/ui/pages/EmployeesPage.scala`
- Create: `ui/src/main/scala/ui/pages/PhonesPage.scala`
- Create: `ui/src/main/scala/ui/components/DepartmentTable.scala`
- Create: `ui/src/main/scala/ui/components/EmployeeTable.scala`
- Create: `ui/src/main/scala/ui/components/PhoneTable.scala`
- Create: `ui/src/main/scala/ui/components/Modal.scala`
- Create: `ui/src/main/resources/index.html`

**Step 1: Create ui/build.sbt**

```scala
import org.scalajs.linker.interface.*

ThisBuild / organization := "com.example"
ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.6.4"

lazy val root = (project in file("."))
  .settings(
    name := "zio-backend-example-ui",
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig := ModuleInitializerMainMethod("ui.Main", "main").toJSLinkerConfig,
    ArtifactSnapshot / scalaJSStage := FastOptStage
  )
  .enablePlugins(ScalaJSPlugin)
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(
    npmDependencies ++= Seq(
      "laminar" -> "20.0.0-2",
      "vite" -> "^6.0.0"
    )
  )
```

**Step 2: Create ui/src/main/resources/index.html**

```html
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Admin Dashboard</title>
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/@picocss/pico@2/css/pico.min.css">
  <style>
    body { margin: 0; padding: 0; }
    .nav-sidebar { position: fixed; left: 0; top: 0; width: 200px; height: 100vh; background: #f8f9fa; padding: 1rem; }
    .main-content { margin-left: 200px; padding: 1rem; }
  </style>
</head>
<body>
  <div id="app"></div>
  <script type="module" src="./target/scala-3.6.4/zio-backend-example-ui-fastopt/main.js"></script>
</body>
</html>
```

**Step 3: Create domain types shared (from domain project)**

First, publish domain locally:
```bash
sbt publishLocal
```

**Step 4: Create ui/src/main/scala/ui/Main.scala**

```scala
package ui

import org.scalajs.dom
import com.raquo.laminar.api.L.*
import ui.components.App

@main
def main(): Unit =
  val appContainer = dom.document.querySelector("#app")
  appContainer.innerHTML = ""
  render(appContainer, App.component)
```

**Step 5: Commit**

```bash
git add ui/
git commit -m "feat: create ui project structure"
```

---

## Task 3: Create API client layer

**Files:**
- Create: `ui/src/main/scala/ui/api/Api.scala`

**Step 1: Create Api.scala with fetch calls**

```scala
package ui.api

import scala.scalajs.js
import scala.scalajs.js.annotation.*
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.scalajs.dom.*

@js.native
@JSImport("...", JSImport.Default)
object fetch extends js.Function

case class Department(id: Long, name: String)
case class Employee(id: Long, name: String, age: Int, departmentId: Long)
case class Phone(id: Long, number: String, employeeId: Option[Long])

object Api:
  val baseUrl = "http://localhost:8080"

  def getDepartments: Future[List[Department]] =
    fetch(s"$baseUrl/departments").then(_.json()).map(_.asInstanceOf[js.Array[Department]].toList)

  def getDepartment(id: Long): Future[Department] =
    fetch(s"$baseUrl/departments/$id").then(_.json()).map(_.asInstanceOf[Department])

  def createDepartment(name: String): Future[Department] =
    fetch(s"$baseUrl/departments", 
      RequestInit(method = "POST", body = s"""{"name":"$name"}""", headers = Headers("Content-Type" -> "application/json"))
    ).then(_.json()).map(_.asInstanceOf[Department])

  def updateDepartment(id: Long, name: String): Future[Department] =
    fetch(s"$baseUrl/departments/$id",
      RequestInit(method = "PUT", body = s"""{"name":"$name"}""", headers = Headers("Content-Type" -> "application/json"))
    ).then(_.json()).map(_.asInstanceOf[Department])

  def deleteDepartment(id: Long): Future[Unit] =
    fetch(s"$baseUrl/departments/$id", RequestInit(method = "DELETE")).then(_ => ())

  def getEmployees: Future[List[Employee]] =
    fetch(s"$baseUrl/employees").then(_.json()).map(_.asInstanceOf[js.Array[Employee]].toList)

  def getEmployee(id: Long): Future[Employee] =
    fetch(s"$baseUrl/employees/$id").then(_.json()).map(_.asInstanceOf[Employee])

  def createEmployee(name: String, age: Int, departmentId: Long): Future[Employee] =
    fetch(s"$baseUrl/employees",
      RequestInit(method = "POST", body = s"""{"name":"$name","age":$age,"departmentId":$departmentId}""", headers = Headers("Content-Type" -> "application/json"))
    ).then(_.json()).map(_.asInstanceOf[Employee])

  def updateEmployee(id: Long, name: String, age: Int, departmentId: Long): Future[Employee] =
    fetch(s"$baseUrl/employees/$id",
      RequestInit(method = "PUT", body = s"""{"name":"$name","age":$age,"departmentId":$departmentId}""", headers = Headers("Content-Type" -> "application/json"))
    ).then(_.json()).map(_.asInstanceOf[Employee])

  def deleteEmployee(id: Long): Future[Unit] =
    fetch(s"$baseUrl/employees/$id", RequestInit(method = "DELETE")).then(_ => ())

  def getPhones: Future[List[Phone]] =
    fetch(s"$baseUrl/phones").then(_.json()).map(_.asInstanceOf[js.Array[Phone]].toList)

  def getPhone(id: Long): Future[Phone] =
    fetch(s"$baseUrl/phones/$id").then(_.json()).map(_.asInstanceOf[Phone])

  def createPhone(number: String, employeeId: Option[Long]): Future[Phone] =
    fetch(s"$baseUrl/phones",
      RequestInit(method = "POST", body = s"""{"number":"$number","employeeId":${employeeId.map(_.toString).getOrElse("null")}}""", headers = Headers("Content-Type" -> "application/json"))
    ).then(_.json()).map(_.asInstanceOf[Phone])

  def updatePhone(id: Long, number: String, employeeId: Option[Long]): Future[Phone] =
    fetch(s"$baseUrl/phones/$id",
      RequestInit(method = "PUT", body = s"""{"number":"$number","employeeId":${employeeId.map(_.toString).getOrElse("null")}}""", headers = Headers("Content-Type" -> "application/json"))
    ).then(_.json()).map(_.asInstanceOf[Phone])

  def deletePhone(id: Long): Future[Unit] =
    fetch(s"$baseUrl/phones/$id", RequestInit(method = "DELETE")).then(_ => ())

  def getEmployeePhones(employeeId: Long): Future[List[Phone]] =
    fetch(s"$baseUrl/employee-phones/$employeeId").then(_.json()).map(_.asInstanceOf[js.Array[Phone]].toList)

  def addPhoneToEmployee(employeeId: Long, phoneId: Long): Future[Unit] =
    fetch(s"$baseUrl/employee-phones",
      RequestInit(method = "POST", body = s"""{"employeeId":$employeeId,"phoneId":$phoneId}""", headers = Headers("Content-Type" -> "application/json"))
    ).then(_ => ())

  def removePhoneFromEmployee(employeeId: Long, phoneId: Long): Future[Unit] =
    fetch(s"$baseUrl/employee-phones?employeeId=$employeeId&phoneId=$phoneId", RequestInit(method = "DELETE")).then(_ => ())
```

**Step 2: Commit**

```bash
git add ui/src/main/scala/ui/api/Api.scala
git commit -m "feat: add API client layer"
```

---

## Task 4: Create Navigation and App shell

**Files:**
- Create: `ui/src/main/scala/ui/components/Navigation.scala`
- Create: `ui/src/main/scala/ui/components/App.scala`

**Step 1: Create Navigation.scala**

```scala
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
```

**Step 2: Create App.scala**

```scala
package ui.components

import com.raquo.laminar.api.L.*
import ui.pages.*

object App:
  val component = 
    div(
      cls := "container-fluid",
      Navigation.render,
      div(
        cls := "main-content",
        Navigation.$currentPage.map:
          case "departments" => DepartmentsPage.render
          case "employees" => EmployeesPage.render
          case "phones" => PhonesPage.render
      )
    )
```

**Step 3: Commit**

```bash
git add ui/src/main/scala/ui/components/
git commit -m "feat: add navigation and app shell"
```

---

## Task 5: Create DepartmentsPage with CRUD

**Files:**
- Create: `ui/src/main/scala/ui/pages/DepartmentsPage.scala`

**Step 1: Create DepartmentsPage.scala**

```scala
package ui.pages

import com.raquo.laminar.api.L.*
import ui.api.Api
import ui.components.Modal
import scala.concurrent.ExecutionContext.Implicits.global

object DepartmentsPage:
  val $departments = Var(List.empty[Department])
  val $searchQuery = Var("")
  val $editingDept = Var[Option[Department]](None)
  val $showModal = Var(false)

  def render = 
    div(
      h1("Departments"),
      input(
        typ := "search",
        placeholder := "Search departments...",
        onInput.map(_.target.value) --> $searchQuery
      ),
      button("Add Department", onClick --> { _ => $editingDept.set(None); $showModal.set(true) }),
      table(
        thead(tr(th("ID"), th("Name"), th("Actions"))),
        tbody(
          $departments.map(_.filter(d => $searchQuery.now.isEmpty || d.name.toLowerCase.contains($searchQuery.now.toLowerCase))).map: filtered =>
            filtered.map: dept =>
              tr(
                td(dept.id.toString),
                td(dept.name),
                td(
                  button("Edit", onClick --> { _ => $editingDept.set(Some(dept)); $showModal.set(true) }),
                  button("Delete", onClick --> { _ => Api.deleteDepartment(dept.id).foreach { _ => $departments.update(_.filter(_.id != dept.id)) } })
                )
              )
        )
      ),
      $showModal.map: show =>
        if show then Modal.render(
          title = $editingDept.map(_.map("Edit Department").getOrElse("Add Department")),
          onClose = () => $showModal.set(false),
          content = 
            div(
              input(
                typ := "text",
                placeholder := "Department Name",
                value := $editingDept.map(_.map(_.name).getOrElse(""))
              ),
              button("Save", onClick --> { _ => 
                val name = // get input value
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
        )
    )
```

**Step 2: Commit**

```bash
git add ui/src/main/scala/ui/pages/DepartmentsPage.scala
git commit -m "feat: add departments page"
```

---

## Task 6: Create EmployeesPage with CRUD

**Files:**
- Create: `ui/src/main/scala/ui/pages/EmployeesPage.scala`

Similar structure to DepartmentsPage but with Employee fields (name, age, departmentId).

**Step 1: Create EmployeesPage.scala**

```scala
package ui.pages

import com.raquo.laminar.api.L.*
import ui.api.Api
import ui.components.Modal
import scala.concurrent.ExecutionContext.Implicits.global

object EmployeesPage:
  val $employees = Var(List.empty[Employee])
  val $departments = Var(List.empty[Department])
  val $searchQuery = Var("")
  val $editingEmp = Var[Option[Employee]](None)
  val $showModal = Var(false)

  def render = 
    div(
      h1("Employees"),
      input(
        typ := "search",
        placeholder := "Search employees...",
        onInput.map(_.target.value) --> $searchQuery
      ),
      button("Add Employee", onClick --> { _ => $editingEmp.set(None); $showModal.set(true) }),
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
                  button("Edit", onClick --> { _ => $editingEmp.set(Some(emp)); $showModal.set(true) }),
                  button("Delete", onClick --> { _ => Api.deleteEmployee(emp.id).foreach { _ => $employees.update(_.filter(_.id != emp.id)) } })
                )
              )
        )
      ),
      // Modal similar to DepartmentsPage
    )
```

**Step 2: Commit**

```bash
git add ui/src/main/scala/ui/pages/EmployeesPage.scala
git commit -m "feat: add employees page"
```

---

## Task 7: Create PhonesPage with CRUD

**Files:**
- Create: `ui/src/main/scala/ui/pages/PhonesPage.scala`

**Step 1: Create PhonesPage.scala**

```scala
package ui.pages

import com.raquo.laminar.api.L.*
import ui.api.Api
import ui.components.Modal
import scala.concurrent.ExecutionContext.Implicits.global

object PhonesPage:
  val $phones = Var(List.empty[Phone])
  val $employees = Var(List.empty[Employee])
  val $searchQuery = Var("")
  val $editingPhone = Var[Option[Phone]](None)
  val $showModal = Var(false)

  def render = 
    div(
      h1("Phones"),
      input(
        typ := "search",
        placeholder := "Search phones...",
        onInput.map(_.target.value) --> $searchQuery
      ),
      button("Add Phone", onClick --> { _ => $editingPhone.set(None); $showModal.set(true) }),
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
                  button("Edit", onClick --> { _ => $editingPhone.set(Some(phone)); $showModal.set(true) }),
                  button("Delete", onClick --> { _ => Api.deletePhone(phone.id).foreach { _ => $phones.update(_.filter(_.id != phone.id)) } })
                )
              )
        )
      ),
      // Modal similar to other pages
    )
```

**Step 2: Commit**

```bash
git add ui/src/main/scala/ui/pages/PhonesPage.scala
git commit -m "feat: add phones page"
```

---

## Task 8: Create Modal component

**Files:**
- Create: `ui/src/main/scala/ui/components/Modal.scala`

**Step 1: Create Modal.scala**

```scala
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
```

**Step 2: Commit**

```bash
git add ui/src/main/scala/ui/components/Modal.scala
git commit -m "feat: add modal component"
```

---

## Task 9: Add CORS to backend

**Files:**
- Modify: `app/src/main/scala/com/example/Main.scala`

**Step 1: Add CORS middleware**

In `Main.scala`, modify line 110:
```scala
Server.serve(routes ++ swaggerRoutes).withMiddleware(CORS.default)
```

**Step 2: Import CORS**

Add to imports:
```scala
import zio.http.Middleware.cors
```

**Step 3: Commit**

```bash
git add app/src/main/scala/com/example/Main.scala
git commit -m "feat: add CORS for UI development"
```

---

## Task 10: Serve static UI assets

**Files:**
- Modify: `app/src/main/scala/com/example/api/Router.scala`

**Step 1: Add static file serving**

In Router.scala, add:
```scala
import zio.http.FilePath
import zio.http.Method.*

private val staticRoutes = Routes(
  GET / "ui" / "index.html" -> Http.fromFile("ui/target/public/index.html"),
  GET / "ui" / "assets" / trailing -> Http.fromFile(???) // Serve vite output
)
```

Update routes in Main.scala:
```scala
Server.serve(routes ++ swaggerRoutes ++ staticRoutes)
```

**Step 2: Commit**

```bash
git add app/src/main/scala/com/example/api/Router.scala
git commit -m "feat: serve static UI assets"
```

---

## Task 11: Build and test UI

**Step 1: Build UI**

```bash
cd ui
sbt fastOptJS
```

**Step 2: Run backend**

```bash
sbt run
```

**Step 3: Open browser**

Navigate to http://localhost:8080/ui/

**Step 4: Test CRUD operations**

- Add a department
- Add another department
- Edit a department
- Delete a department
- Repeat for Employees and Phones

**Step 5: Commit**

```bash
git commit -m "feat: complete UI build and integration"
```

---

**Plan complete and saved to `docs/plans/2026-03-09-web-ui-design.md`**

Two execution options:

1. **Subagent-Driven (this session)** - I dispatch fresh subagent per task, review between tasks, fast iteration

2. **Parallel Session (separate)** - Open new session with executing-plans, batch execution with checkpoints

Which approach?
