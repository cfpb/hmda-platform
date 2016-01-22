import sbt._
import sbt.Keys._

object BuildSettings {
  val buildOrganization = "cfpb"
  val buildVersion      = "1.0.0"
  val buildScalaVersion = "2.11.7"

  val buildSettings = Defaults.coreDefaultSettings ++
    Seq(
      organization := buildOrganization,
      version      := buildVersion,
      scalaVersion := buildScalaVersion,
      scalacOptions ++= Seq(
        "-Xlint",
        "-deprecation",
        "-unchecked",
        "-feature")
    )

}


object HMDABuild extends Build {
  import Dependencies._
  import BuildSettings._

  lazy val hmda = (project in file("."))
    .settings(buildSettings: _*)
    .aggregate(parser, validation, persistence, api)

  lazy val model = (project in file("model"))
    .settings(buildSettings: _*)

  lazy val parser = (project in file("parser"))
    .settings(buildSettings: _*)
    .dependsOn(model)

  lazy val validation = (project in file("validation"))
    .settings(buildSettings: _*)
    .dependsOn(model)

  lazy val persistence = (project in file("persistence"))
    .settings(buildSettings: _*)

  lazy val api = (project in file("api"))
    .settings(buildSettings: _*)
}
