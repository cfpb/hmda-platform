import sbt._
import sbt.Keys._
import sbtassembly.AssemblyPlugin.autoImport._
import scoverage.ScoverageSbtPlugin
import spray.revolver.RevolverPlugin.autoImport.Revolver

object BuildSettings {
  val buildOrganization = "cfpb"
  val buildVersion      = "1.0.0"
  val buildScalaVersion = "2.11.8"

  val buildSettings = Defaults.coreDefaultSettings ++
    Seq(
      organization := buildOrganization,
      version      := buildVersion,
      scalaVersion := buildScalaVersion,
      scalacOptions ++= Seq(
        "-Xlint",
        "-deprecation",
        "-unchecked",
        "-feature"),
      aggregate in assembly := false,
      parallelExecution in Test := false
    )

}


object HMDABuild extends Build {
  import Dependencies._
  import BuildSettings._

  val commonDeps = Seq(logback, scalaTest, scalaCheck)

  val akkaDeps = commonDeps ++ Seq(akka, akkaSlf4J, akkaStream)

  val akkaPersistenceDeps = akkaDeps ++ Seq(akkaPersistence, akkaStream, leveldb, leveldbjni, akkaPersistenceQuery)

  val httpDeps = akkaDeps ++ Seq(akkaHttp, akkaHttpJson, akkaHttpTestkit)

  val scalazDeps = Seq(scalaz)

  val configDeps = Seq(config)

  lazy val hmda = (project in file("."))
    .settings(buildSettings:_*)
    .settings(Revolver.settings:_*)
    .settings(
      Seq(
        assemblyJarName in assembly := {s"${name.value}.jar"},
        mainClass in assembly := Some("hmda.api.HmdaApi"),
        assemblyMergeStrategy in assembly := {
          case "application.conf" => MergeStrategy.concat
          case "JS_DEPENDENCIES" => MergeStrategy.concat
          case x =>
            val oldStrategy = (assemblyMergeStrategy in assembly).value
            oldStrategy(x)
        }
      )
    ).dependsOn(api)
    .aggregate(
      model,
      parser,
      persistence,
      api,
      platformTest,
      validation)

  lazy val model = (project in file("model"))
    .settings(buildSettings: _*)
    .settings(
      Seq(
        libraryDependencies ++= commonDeps
      )
    )
    .disablePlugins(ScoverageSbtPlugin)


  lazy val parser = (project in file("parser"))
    .settings(buildSettings: _*)
      .settings(
        Seq(
          libraryDependencies ++= commonDeps ++ scalazDeps
        )
      )
    .dependsOn(model % "compile->compile;test->test")

  lazy val validation = (project in file("validation"))
    .settings(buildSettings: _*)
    .settings(
      libraryDependencies ++= commonDeps ++ scalazDeps ++ configDeps
    ).dependsOn(parser % "compile->compile;test->test")


  lazy val persistence = (project in file("persistence"))
      .settings(buildSettings:_*)
    .settings(
      Seq(
        assemblyMergeStrategy in assembly := {
          case "application.conf" => MergeStrategy.concat
          case x =>
            val oldStrategy = (assemblyMergeStrategy in assembly).value
            oldStrategy(x)
        },
        libraryDependencies ++= akkaPersistenceDeps
      )
    ).dependsOn(model % "compile->compile;test->test")


  lazy val api = (project in file("api"))
    .settings(buildSettings: _*)
    .settings(Revolver.settings:_*)
    .settings(
      Seq(
        scoverage.ScoverageKeys.coverageExcludedPackages := "hmda.api.HmdaApi",
        assemblyJarName in assembly := {s"${name.value}.jar"},
        mainClass in assembly := Some("hmda.api.HmdaApi"),
        assemblyMergeStrategy in assembly := {
          case "application.conf" => MergeStrategy.concat
          case x =>
            val oldStrategy = (assemblyMergeStrategy in assembly).value
            oldStrategy(x)
        },
        libraryDependencies ++= httpDeps
      )
    ).dependsOn(parser, validation, persistence % "compile->compile;test->test")


  lazy val platformTest = (project in file("platform-test"))
    .settings(buildSettings: _*)
    .settings(
      Seq(
        libraryDependencies ++= akkaDeps
      )
    )
    .disablePlugins(ScoverageSbtPlugin)
    .dependsOn(parser)


}
