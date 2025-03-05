import Dependencies.*

ThisBuild / scalaVersion     := "2.13.16"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.lucas"
ThisBuild / organizationName := "lucas"

lazy val root = (project in file("."))
  .settings(
    name := "finagle-assignment",
    libraryDependencies ++=
      compileDependencies ++ testDependencies,
    scalacOptions ++= Seq(
      "-encoding",
      "utf8",
      "-feature",
      "-language:implicitConversions",
      "-language:existentials",
      "-unchecked",
      "-Werror",
      "-Xlint"
    )
  )
