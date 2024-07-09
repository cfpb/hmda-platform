import sbt.Keys.{ resolvers, _ }
import sbt._
import sbtassembly.AssemblyPlugin.autoImport._

object BuildSettings {
  val buildOrganization = "cfpb"
  val buildVersion      = "2.0.0"
  val buildScalaVersion = "2.13.12"

  lazy val dockerPublishLocalSkipTestsCommand = Command.command("dockerPublishLocalSkipTests") { state =>
    var s = state
    s = Command.process("set test in Test := {}", s)
    s = Command.process("docker:publishLocal", s)
    s
  }

  val hmdaBuildSettings = Defaults.coreDefaultSettings ++
    Seq(
      organization := buildOrganization,
      version := buildVersion,
      scalaVersion := buildScalaVersion,
      scalacOptions ++= Seq("-Xlint", "-deprecation", "-unchecked", "-feature", "-Ymacro-annotations"),
      assembly / aggregate := false,
      Test / parallelExecution:= false,
      Test / fork := true,
      resolvers += "Akka library repository".at("https://repo.akka.io/maven"),
      commands += dockerPublishLocalSkipTestsCommand
    )
}