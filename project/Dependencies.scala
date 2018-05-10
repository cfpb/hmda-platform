import sbt._

object Dependencies {

  val repos = Seq(
   "Local Maven Repo"  at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    "Typesafe Repo"     at "http://repo.typesafe.com/typesafe/releases/",
    "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases"
  )

  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest % "test"
  val scalaCheck = "org.scalacheck" %% "scalacheck" % Version.scalaCheck % "test"
  val logback = "ch.qos.logback" % "logback-classic" % Version.logback
  lazy val akkaSlf4J = "com.typesafe.akka" %% "akka-slf4j" % Version.akka
  lazy val akkaTyped = "com.typesafe.akka" %% "akka-typed" % Version.akka
  lazy val akkaCluster = "com.typesafe.akka" %% "akka-cluster" % Version.akka
  lazy val akkaClusterSharding = "com.typesafe.akka" %% "akka-cluster-sharding" % Version.akka
  lazy val akkaPersistence = "com.typesafe.akka" %% "akka-persistence" % Version.akka
  lazy val akkaStream = "com.typesafe.akka" %% "akka-stream" % Version.akka
  lazy val akkaHttp = "com.typesafe.akka" %% "akka-http" % Version.akkaHttp
  lazy val akkaHttpTestkit = "com.typesafe.akka" %% "akka-http-testkit" % Version.akkaHttp % "test"
  lazy val akkaHttpCirce = "de.heikoseeberger" %% "akka-http-circe" % Version.akkaHttpJson
  lazy val akkaManagementClusterBootstrap = "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % Version.akkaClusterManagement
  lazy val akkaServiceDiscoveryDNS = "com.lightbend.akka.discovery" %% "akka-discovery-dns" % Version.akkaClusterManagement
  lazy val akkaServiceDiscoveryKubernetes = "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % Version.akkaClusterManagement
  lazy val akkaManagement = "com.lightbend.akka.management" %% "akka-management" % Version.akkaClusterManagement
  lazy val akkaClusterHttpManagement = "com.lightbend.akka.management" %% "akka-management-cluster-http" % Version.akkaClusterManagement
  lazy val circe =  "io.circe" %% "circe-core" % Version.circe
  lazy val circeGeneric = "io.circe" %% "circe-generic" % Version.circe
  lazy val circeParser = "io.circe" %% "circe-parser" % Version.circe

}
