ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.6.4"
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

lazy val projectName = "zio-backend-example"

lazy val root = (project in file("."))
  .settings(
    name := projectName,
    addCommandAlias("run", "app/run")
  )
  .aggregate(app, client, core, domain, endpoints)

lazy val app = (project in file("app"))
  .settings(
    name := s"$projectName-app",
    scalacOptions ++= Seq(
      "-Wunused:imports"
    ),
    libraryDependencies ++= Seq(
      // ZIO HTTP
      "dev.zio" %% "zio-http" % "3.3.3",
      // Database
      "com.augustnagro" %% "magnumzio" % "2.0.0-M1",
      "org.postgresql" % "postgresql" % "42.7.6",
      "org.testcontainers" % "testcontainers" % "1.21.1",
      "org.testcontainers" % "postgresql" % "1.21.1",
      "com.zaxxer" % "HikariCP" % "6.3.0",
      // Logging
      "dev.zio" %% "zio-logging-jul-bridge" % "2.5.0"
    ),
    Compile / mainClass := Some("com.example.Main")
  )
  .dependsOn(core, domain, endpoints)

lazy val client = (project in file("client"))
  .settings(
    name := s"$projectName-client",
    scalacOptions ++= Seq(
      "-Wunused:imports"
    ),
    libraryDependencies ++= Seq(
      // ZIO HTTP
      "dev.zio" %% "zio-http" % "3.3.3",
      // Iron
      "io.github.iltotore" %% "iron" % "3.0.1"
    )
  )
  .dependsOn(domain, endpoints)

lazy val core = (project in file("core"))
  .settings(
    name := s"$projectName-core",
    scalacOptions ++= Seq(
      "-Wunused:imports"
    )
  )
  .dependsOn(domain)

lazy val domain = (project in file("domain"))
  .settings(
    name := s"$projectName-domain",
    scalacOptions ++= Seq(
      "-Wunused:imports"
    ),
    libraryDependencies ++= Seq(
      // ZIO HTTP
      "dev.zio" %% "zio-http" % "3.3.3",
      // Iron
      "io.github.iltotore" %% "iron" % "3.0.1"
    )
  )

lazy val endpoints = (project in file("endpoints"))
  .settings(
    name := s"$projectName-endpoints",
    scalacOptions ++= Seq(
      "-Wunused:imports"
    ),
    libraryDependencies ++= Seq(
      // ZIO HTTP
      "dev.zio" %% "zio-http" % "3.3.3",
      // Iron
      "io.github.iltotore" %% "iron" % "3.0.1"
    )
  )
  .dependsOn(domain)
