import Dependencies._

ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "FhirSchemaConverter",
    libraryDependencies += scalaTest % Test
  )
libraryDependencies += "com.typesafe.play" %% "play-json" % "2.7.2"
libraryDependencies += "org.postgresql" % "postgresql" % "42.2.5"
