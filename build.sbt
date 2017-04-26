import BuildSettings._
import Dependencies._
import sbtassembly.AssemblyPlugin.autoImport._
import spray.revolver.RevolverPlugin.autoImport.Revolver
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

val commonDeps = Seq(logback, scalaTest, scalaCheck)

val akkaDeps = commonDeps ++ Seq(akka, akkaSlf4J, akkaStream, akkaTestkit)

val akkaPersistenceDeps = akkaDeps ++ Seq(akkaPersistence, akkaStream, leveldb, leveldbjni, akkaPersistenceQuery, inMemoryPersistence, cassandraPersistence)

val httpDeps = akkaDeps ++ Seq(akkaHttp, akkaHttpJson, akkaHttpTestkit)

val scalazDeps = Seq(scalaz)

val configDeps = Seq(typesafeConfig)

val enumDeps = Seq(enumeratum)

val slickDeps = Seq(slick, slickHikariCP, h2, postgres)

val csvDeps = Seq(scalaCsv)

lazy val hmda = (project in file("."))
  .settings(hmdaBuildSettings:_*)
  .settings(Revolver.settings:_*)
  .settings(
    Seq(
      assemblyJarName in assembly := {s"${name.value}.jar"},
      mainClass in assembly := Some("hmda.api.HmdaPlatform"),
      assemblyMergeStrategy in assembly := {
        case "application.conf" => MergeStrategy.concat
        case "application-dev.conf" => MergeStrategy.concat
        case "JS_DEPENDENCIES" => MergeStrategy.concat
        case "META-INF/io.netty.versions.properties" => MergeStrategy.concat
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
    panel,
    persistenceModel,
    persistence,
    api,
    query,
    platformTest,
    validation,
    census)

lazy val model = (crossProject in file("model"))
  .settings(hmdaBuildSettings: _*)
  .jvmSettings(
    Seq(
      libraryDependencies ++= commonDeps ++ enumDeps ++ csvDeps ++ Seq(
        "org.scala-js" %% "scalajs-stubs" % scalaJSVersion % "provided"
      )
    )
  )
  .jsSettings(
    scoverage.ScoverageKeys.coverageEnabled := false,
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % Version.scalaTest % "test",
      "org.scalacheck" %%% "scalacheck" % Version.scalaCheck % "test",
      "com.beachape" %%% "enumeratum" % Version.enumeratum
    )
  )

lazy val modelJVM = model.jvm
lazy val modelJS = model.js


lazy val parser = (crossProject in file("parser"))
  .settings(hmdaBuildSettings: _*)
  .jvmSettings(
    libraryDependencies ++= commonDeps ++ scalazDeps
  )
  .jsSettings(
    scalaJSModuleKind := ModuleKind.CommonJSModule,
    scoverage.ScoverageKeys.coverageEnabled := false,
    libraryDependencies ++= Seq(
      "org.scalatest" %%% "scalatest" % Version.scalaTest % "test",
      "org.scalacheck" %%% "scalacheck" % Version.scalaCheck % "test",
      "org.scalaz" %%% "scalaz-core" % Version.scalaz
    )
  )
  .dependsOn(model % "compile->compile;test->test")

lazy val parserJVM = parser.jvm
lazy val parserJS = parser.js

lazy val validation = (project in file("validation"))
  .settings(hmdaBuildSettings: _*)
  .settings(
    parallelExecution in Test := true,
    libraryDependencies ++= commonDeps ++ scalazDeps ++ configDeps ++ Seq(akkaStream)
  ).dependsOn(parserJVM % "compile->compile;test->test")
  .dependsOn(persistenceModel % "compile->compile;test->test")

lazy val panel = (project in file("panel"))
  .settings(hmdaBuildSettings: _*)
  .settings(
    assemblyMergeStrategy in assembly := {
      case "application.conf" => MergeStrategy.concat
      case "META-INF/io.netty.versions.properties" => MergeStrategy.concat
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },
    libraryDependencies ++= akkaPersistenceDeps
  ).dependsOn(persistenceModel % "compile->compile;test->test")
  .dependsOn(persistence % "compile->compile;test->test")
  .dependsOn(parserJVM % "compile->compile;test->test")
  .dependsOn(query % "compile->compile;test->test")

lazy val persistenceModel = (project in file("persistence-model"))
  .settings(hmdaBuildSettings:_*)
  .settings(
    assemblyMergeStrategy in assembly := {
      case "META-INF/io.netty.versions.properties" => MergeStrategy.concat
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },
    libraryDependencies ++= akkaPersistenceDeps
  ).disablePlugins(ScoverageSbtPlugin)
  .dependsOn(modelJVM % "compile->compile;test->test")

lazy val persistence = (project in file("persistence"))
  .settings(hmdaBuildSettings:_*)
  .settings(
    resolvers += Resolver.jcenterRepo,
    Seq(
      assemblyMergeStrategy in assembly := {
        case "application.conf" => MergeStrategy.concat
        case "META-INF/io.netty.versions.properties" => MergeStrategy.concat
        case x =>
          val oldStrategy = (assemblyMergeStrategy in assembly).value
          oldStrategy(x)
      },
      parallelExecution in Test := false,
      libraryDependencies ++= akkaPersistenceDeps
    )
  )
  .dependsOn(validation % "compile->compile;test->test")

lazy val query = (project in file("query"))
  .settings(hmdaBuildSettings:_*)
  .settings(
    assemblyMergeStrategy in assembly := {
      case "META-INF/io.netty.versions.properties" => MergeStrategy.concat
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },
    parallelExecution in Test := false,
    libraryDependencies ++= configDeps ++ akkaPersistenceDeps ++ slickDeps
  )
  .dependsOn(modelJVM % "compile->compile;test->test")
  .dependsOn(census % "compile->compile;test->test")
  .dependsOn(persistenceModel % "compile->compile;test->test")

lazy val api = (project in file("api"))
  .settings(hmdaBuildSettings: _*)
  .settings(Revolver.settings:_*)
  .settings(
    Seq(
      scoverage.ScoverageKeys.coverageExcludedPackages := "hmda.api.HmdaFilingApi;hmda.api.HmdaAdminApi",
      assemblyJarName in assembly := {s"${name.value}.jar"},
      mainClass in assembly := Some("hmda.api.HmdaFilingApi"),
      assemblyMergeStrategy in assembly := {
        case "application.conf" => MergeStrategy.concat
        case x =>
          val oldStrategy = (assemblyMergeStrategy in assembly).value
          oldStrategy(x)
      },
      parallelExecution in Test := false,
      libraryDependencies ++= httpDeps
    )
  )
  .dependsOn(persistenceModel % "compile->compile;test->test")
  .dependsOn(query % "compile->compile")
  .dependsOn(persistence % "compile->compile")


lazy val platformTest = (project in file("platform-test"))
  .settings(hmdaBuildSettings: _*)
  .settings(
    Seq(
      libraryDependencies ++= akkaDeps
    )
  )
  .disablePlugins(ScoverageSbtPlugin)
  .dependsOn(parserJVM)

lazy val census = (project in file("census"))
  .settings(hmdaBuildSettings: _*)
  .settings(
    Seq(
      mainClass in assembly := Some("TractToCbsa"),
      libraryDependencies ++= commonDeps ++ csvDeps
    )
  ).dependsOn(modelJVM % "compile->compile;test->test")
