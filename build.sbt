ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.6"

lazy val root = (project in file("."))
  .settings(
    name := "Zenith"
  )

libraryDependencies += ("com.github.scopt" %% "scopt" % "4.1.0")

libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.4.0"