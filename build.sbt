ThisBuild / version := "0.1.0-SNAPSHOT"
ThisBuild / scalaVersion := "3.6.4"
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

// Test framework configuration
ThisBuild / testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework")

lazy val projectName = "zio-backend-example"

// Common test dependencies
lazy val testDependencies = Seq(
  "dev.zio" %% "zio-test" % "2.1.14" % Test,
  "dev.zio" %% "zio-test-sbt" % "2.1.14" % Test,
  "dev.zio" %% "zio-test-magnolia" % "2.1.14" % Test
)

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
      "com.zaxxer" % "HikariCP" % "6.3.0",
      "org.testcontainers" % "postgresql" % "1.21.3" % Test,
      "org.testcontainers" % "testcontainers" % "2.0.1" % Test,
      // Flyway
      "org.flywaydb" % "flyway-core" % "11.15.0",
      "org.flywaydb" % "flyway-database-postgresql" % "11.15.0",
      // H2 for testing
      "com.h2database" % "h2" % "2.2.224" % Test,
      // Logging
      "ch.qos.logback" % "logback-classic" % "1.5.20"
    ) ++ testDependencies,
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
    ) ++ testDependencies
  )
  .dependsOn(domain, endpoints)

lazy val core = (project in file("core"))
  .settings(
    name := s"$projectName-core",
    scalacOptions ++= Seq(
      "-Wunused:imports"
    ),
    libraryDependencies ++= testDependencies
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
    ) ++ testDependencies
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
    ) ++ testDependencies
  )
  .dependsOn(domain)
