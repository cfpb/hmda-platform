import sbt.Keys._
import sbt._
import sbtassembly.AssemblyPlugin.autoImport._
import scoverage.ScoverageSbtPlugin
import spray.revolver.RevolverPlugin.autoImport.Revolver
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

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
      parallelExecution in Test := false,
      testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oC")
    )

}


object HMDABuild extends Build {
  import BuildSettings._
  import Dependencies._

  val commonDeps = Seq(logback, scalaTest, scalaCheck)

  val akkaDeps = commonDeps ++ Seq(akka, akkaSlf4J, akkaStream, akkaTestkit)

  val akkaPersistenceDeps = akkaDeps ++ Seq(akkaPersistence, akkaStream, leveldb, leveldbjni, akkaPersistenceQuery, inMemoryPersistence)

  val httpDeps = akkaDeps ++ Seq(akkaHttp, akkaHttpJson, akkaHttpTestkit)

  val scalazDeps = Seq(scalaz)

  val configDeps = Seq(config)

  val enumDeps = Seq(enumeratum)

  val slickDeps = Seq(slick, hikariCP, h2)

  lazy val hmda = (project in file("."))
    .settings(buildSettings:_*)
    .settings(Revolver.settings:_*)
    .settings(
      Seq(
        assemblyJarName in assembly := {s"${name.value}.jar"},
        mainClass in assembly := Some("hmda.api.HmdaPlatform"),
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
      modelJVM,
      modelJS,
      parserJVM,
      parserJS,
      persistence,
      api,
      query,
      platformTest,
      validation)

  lazy val model = (crossProject in file("model"))
    .settings(buildSettings: _*)
    .jvmSettings(
      Seq(
        libraryDependencies ++= commonDeps ++ enumDeps ++ Seq(
          "org.scala-js" %% "scalajs-stubs" % scalaJSVersion % "provided"
        )
      )
    )
    .jsSettings(
      scoverage.ScoverageKeys.coverageExcludedPackages := "\\*",
      libraryDependencies ++= Seq(
        "org.scalatest" %%% "scalatest" % Version.scalaTest % "test",
        "org.scalacheck" %%% "scalacheck" % Version.scalaCheck % "test",
        "com.beachape" %%% "enumeratum" % Version.enumeratum
      )
    )
    .disablePlugins(ScoverageSbtPlugin)

  lazy val modelJVM = model.jvm
  lazy val modelJS = model.js


  lazy val parser = (crossProject in file("parser"))
    .settings(buildSettings: _*)
    .jvmSettings(
        libraryDependencies ++= commonDeps ++ scalazDeps
    )
    .jsSettings(
      scoverage.ScoverageKeys.coverageExcludedPackages := "\\*",
      libraryDependencies ++= Seq(
        "org.scalatest" %%% "scalatest" % Version.scalaTest % "test",
        "org.scalacheck" %%% "scalacheck" % Version.scalaCheck % "test",
        "org.scalaz" %%% "scalaz-core" % Version.scalaz
      )
    ).disablePlugins(ScoverageSbtPlugin)
    .dependsOn(model % "compile->compile;test->test")

  lazy val parserJVM = parser.jvm
  lazy val parserJS = parser.js

  lazy val validation = (project in file("validation"))
    .settings(buildSettings: _*)
    .settings(
      libraryDependencies ++= commonDeps ++ scalazDeps ++ configDeps ++ Seq(akkaStream)
    ).dependsOn(parserJVM % "compile->compile;test->test")

  lazy val persistenceModel = (project in file("persistence-model"))
    .settings(buildSettings:_*)
    .settings(
      libraryDependencies ++= akkaPersistenceDeps
    )
    .dependsOn(modelJVM % "compile->compile;test->test")

  lazy val persistence = (project in file("persistence"))
    .settings(buildSettings:_*)
    .settings(
      resolvers += Resolver.jcenterRepo,
      Seq(
        assemblyMergeStrategy in assembly := {
          case "application.conf" => MergeStrategy.concat
          case x =>
            val oldStrategy = (assemblyMergeStrategy in assembly).value
            oldStrategy(x)
        },
        libraryDependencies ++= akkaPersistenceDeps
      )
    )
    .dependsOn(persistenceModel % "compile->compile;test->test")
    .dependsOn(validation % "compile->compile;test->test")

  lazy val query = (project in file("query"))
    .settings(buildSettings:_*)
    .settings(
      libraryDependencies ++= akkaPersistenceDeps ++ slickDeps
    )
    .dependsOn(modelJVM % "compile->compile;test->test")
    .dependsOn(persistenceModel % "compile->compile;test->test")


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
    )
    .dependsOn(query % "compile->compile;test->test")
    .dependsOn(persistence % "compile->compile;test->test")


  lazy val platformTest = (project in file("platform-test"))
    .settings(buildSettings: _*)
    .settings(
      Seq(
        libraryDependencies ++= akkaDeps
      )
    )
    .disablePlugins(ScoverageSbtPlugin)
    .dependsOn(parserJVM)


}
