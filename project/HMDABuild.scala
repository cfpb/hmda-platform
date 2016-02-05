import sbt._
import sbt.Keys._
import sbtassembly.AssemblyPlugin.autoImport._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import spray.revolver.RevolverPlugin.autoImport.Revolver

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
        "-feature"),
      aggregate in assembly := false
    )

}


object HMDABuild extends Build {
  import Dependencies._
  import BuildSettings._

  val commonDeps = Seq(logback, scalaTest, scalaCheck)

  val akkaDeps = commonDeps ++ Seq(akka, akkaSlf4J)

  val httpDeps = akkaDeps ++ Seq(akkaHttp, akkaHttpJson, akkaHttpTestkit)

  lazy val hmda = (project in file("."))
    .settings(buildSettings:_*)
    .settings(Revolver.settings:_*)
    .settings(
      Seq(
        assemblyJarName in assembly := {s"${name.value}.jar"},
        mainClass in assembly := Some("hmda.Hmda"),
        assemblyMergeStrategy in assembly := {
          case "application.conf" => MergeStrategy.concat
          case x =>
            val oldStrategy = (assemblyMergeStrategy in assembly).value
            oldStrategy(x)
        },
        libraryDependencies ++= httpDeps
      )
    ).dependsOn(parserJVM)
    .aggregate(parserJVM, parserJS)

  lazy val model = (crossProject in file("model"))
    .settings(buildSettings: _*)
    .jvmSettings(
      assemblyJarName in assembly := {s"hmda-${name.value}.jar"}
    )

  lazy val modelJS = model.js
  lazy val modelJVM = model.jvm

  lazy val parser = (crossProject in file("parser"))
    .settings(buildSettings: _*)
    .jvmSettings(
      libraryDependencies ++= commonDeps ++ Seq(
        "org.scala-js" %% "scalajs-stubs" % scalaJSVersion % "provided"
      )
    )
    .jsSettings(
      scalaJSUseRhino in Global := false,
      libraryDependencies ++= Seq(
        "org.scala-js" %%% "scalajs-dom" % Version.scalaJSDom,
        "com.lihaoyi" %%% "scalatags" % Version.scalaTags,
        "org.scalatest" %%% "scalatest" % Version.scalaTest % "test",
        "org.scalacheck" %%% "scalacheck" % Version.scalaCheck % "test"
      )
    )
    .dependsOn(model)

  lazy val parserJVM = parser.jvm
  lazy val parserJS = parser.js


    
}
