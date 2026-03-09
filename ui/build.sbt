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
      "laminar" -> "20.0.0-2"
    )
  )
