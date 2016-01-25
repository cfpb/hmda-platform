import sbt._
import sbt.Keys._
import spray.revolver.RevolverPlugin._
import sbtassembly.AssemblyPlugin.autoImport._

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

  val commonDeps = Seq(logback, scalaTest, scalaCheck)

  val akkaDeps = commonDeps ++ Seq(akka, akkaSlf4J)

  val httpDeps = akkaDeps ++ Seq(akkaHttp, akkaHttpJson, akkaHttpTestkit)

  lazy val hmda = (project in file("."))
    .settings(buildSettings: _*)
    .settings(
      Seq(
        assemblyJarName in assembly := {s"${name.value}.jar"}  
      )  
    )
    .aggregate(parser, validation, persistence, api)

  lazy val model = (project in file("model"))
    .settings(buildSettings: _*)
    .settings(
      Seq(
        assemblyJarName in assembly := {s"hmda-${name.value}.jar"}  
      )  
    )

  lazy val parser = (project in file("parser"))
    .settings(buildSettings: _*)
    .settings(
      Seq(
        assemblyJarName in assembly := {s"hmda-${name.value}.jar"}  
      )  
    )
    .dependsOn(model)

  lazy val validation = (project in file("validation"))
    .settings(buildSettings: _*)
    .settings(
      Seq(
        assemblyJarName in assembly := {s"hmda-${name.value}.jar"}  
      )  
    ) 
    .dependsOn(model)

  lazy val persistence = (project in file("persistence"))
    .settings(buildSettings: _*)
    .settings(
      Seq(
        assemblyJarName in assembly := {s"hmda-${name.value}.jar"}  
      )  
    )

  lazy val api = (project in file("api"))
    .settings(buildSettings: _*)
    .settings(
      Seq(
        assemblyJarName in assembly := {s"hmda-${name.value}.jar"},
        assemblyMergeStrategy in assembly := {
          case "application.conf" => MergeStrategy.concat
          case x =>
            val oldStrategy = (assemblyMergeStrategy in assembly).value
            oldStrategy(x)
        },
        libraryDependencies ++= httpDeps,
        resolvers ++= repos  
      )  
    )
    
}
