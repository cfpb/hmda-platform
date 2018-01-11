import Dependencies._
import BuildSettings._
import com.lucidchart.sbt.scalafmt.ScalafmtCorePlugin.autoImport._

lazy val commonDeps = Seq(logback, scalaTest, scalaCheck)
lazy val akkaDeps = Seq(akkaSlf4J,
                        akkaCluster,
                        akkaTyped,
                        akkaStream,
                        akkaManagement,
                        akkaManagementClusterBootstrap,
                        akkaServiceDiscoveryDNS,
                        akkaClusterHttpManagement)
lazy val akkaPersistenceDeps = Seq(akkaPersistence, akkaClusterSharding)
lazy val akkaHttpDeps = Seq(akkaHttp, akkaHttpTestkit, akkaHttpCirce)
lazy val circeDeps = Seq(circe, circeGeneric)

lazy val hmda = (project in file("."))
  .enablePlugins(JavaServerAppPackaging,
                 sbtdocker.DockerPlugin,
                 AshScriptPlugin)
  .settings(hmdaBuildSettings: _*)
  .settings(
    Seq(
      mainClass in Compile := Some("hmda.cluster.HmdaPlatform"),
      assemblyJarName in assembly := {
        s"${name.value}2.jar"
      }
    ),
    scalafmtOnCompile in ThisBuild := true,
    scalafmtTestOnCompile in ThisBuild := true,
    // removes all jar mappings in universal and appends the fat jar
    mappings in Universal := {
      // universalMappings: Seq[(File,String)]
      val universalMappings = (mappings in Universal).value
      val fatJar = (assembly in Compile).value
      // removing means filtering
      val filtered = universalMappings filter {
        case (_, fileName) => !fileName.endsWith(".jar")
      }
      // add the fat jar
      filtered :+ (fatJar -> ("lib/" + fatJar.getName))
    },
    dockerUpdateLatest := true,
    dockerRepository := Some("jmarin"),
    dockerBaseImage := "openjdk:8-jre-alpine",
    // the bash scripts classpath only needs the fat jar
    scriptClasspath := Seq((assemblyJarName in assembly).value)
  )
  .dependsOn(cluster)
  .aggregate(
    model,
    httpModel,
    parser,
    persistence,
    api,
    cluster
  )

lazy val model = (project in file("model"))
  .settings(hmdaBuildSettings: _*)
  .settings(
    libraryDependencies ++= commonDeps ++ akkaDeps
  )

lazy val httpModel = (project in file("http-model"))
  .settings(hmdaBuildSettings: _*)
  .settings(
    libraryDependencies ++= commonDeps ++ akkaHttpDeps ++ circeDeps
  )
  .dependsOn(model)

lazy val parser = (project in file("parser"))
  .settings(hmdaBuildSettings: _*)
  .dependsOn(model)

lazy val persistence = (project in file("persistence"))
  .settings(hmdaBuildSettings: _*)
  .settings(
    libraryDependencies ++= commonDeps ++ akkaDeps ++ akkaPersistenceDeps
  )
  .dependsOn(model)

lazy val validation = (project in file("validation"))
  .settings(hmdaBuildSettings: _*)
  .settings(
    libraryDependencies ++= commonDeps ++ akkaDeps
  )
  .dependsOn(model)

lazy val query = (project in file("query"))
  .settings(hmdaBuildSettings: _*)
  .settings(
    libraryDependencies ++= commonDeps ++ akkaDeps
  )
  .dependsOn(model)

lazy val publication = (project in file("publication"))
  .settings(hmdaBuildSettings: _*)
  .settings(
    libraryDependencies ++= commonDeps ++ akkaDeps
  )
  .dependsOn(model)

lazy val api = (project in file("api"))
  .settings(hmdaBuildSettings: _*)
  .settings(
    libraryDependencies ++= commonDeps ++ akkaDeps ++ akkaHttpDeps
  )
  .dependsOn(httpModel)

lazy val cluster = (project in file("cluster"))
  .settings(hmdaBuildSettings: _*)
  .dependsOn(persistence)
  .dependsOn(validation)
  .dependsOn(query)
  .dependsOn(publication)
  .dependsOn(api)
