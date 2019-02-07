import sbt.Keys.{resolvers, _}
import sbt._
import sbtassembly.AssemblyPlugin.autoImport._

object BuildSettings {
  val buildOrganization = "cfpb"
  val buildVersion = "2.0.0"
  val buildScalaVersion = "2.12.7"

  val hmdaBuildSettings = Defaults.coreDefaultSettings ++
    Seq(
      organization := buildOrganization,
      version := buildVersion,
      scalaVersion := buildScalaVersion,
      scalacOptions ++= Seq("-Xlint",
                            "-deprecation",
                            "-unchecked",
                            "-feature",
                            "-Ypartial-unification"),
      aggregate in assembly := false,
      parallelExecution in Test := true,
      fork in Test := true,
      resolvers += Resolver.bintrayRepo("tanukkii007", "maven")
    )

}
