ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.3.6"

lazy val root = (project in file("."))
  .settings(
    name := "Zenith",
    mainClass := Some("cli.Runner"),
    assembly / mainClass := Some("cli.Runner"),
    assembly / assemblyJarName := "zenith.jar"
  )
libraryDependencies += ("com.github.scopt" %% "scopt" % "4.1.0")

libraryDependencies += "org.scala-lang.modules" %% "scala-parser-combinators" % "2.4.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.18" % Test