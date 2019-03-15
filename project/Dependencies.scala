import sbt._

object Dependencies {

  val repos = Seq(
    "Local Maven Repo" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/",
    "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases"
  )

  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest % Test
  val scalaCheck = "org.scalacheck" %% "scalacheck" % Version.scalaCheck % Test
  val logback = "ch.qos.logback" % "logback-classic" % Version.logback
  lazy val akkaSlf4J = "com.typesafe.akka" %% "akka-slf4j" % Version.akka
  lazy val akkaTyped = "com.typesafe.akka" %% "akka-actor-typed" % Version.akka
  lazy val akkaCluster = "com.typesafe.akka" %% "akka-cluster" % Version.akka
  lazy val akkaClusterTyped = "com.typesafe.akka" %% "akka-cluster-typed" % Version.akka
  lazy val akkaClusterSharding = "com.typesafe.akka" %% "akka-cluster-sharding" % Version.akka
  lazy val akkaClusterShardingTyped = "com.typesafe.akka" %% "akka-cluster-sharding-typed" % Version.akka
  lazy val akkaPersistence = "com.typesafe.akka" %% "akka-persistence" % Version.akka
  lazy val akkaPersistenceTyped = "com.typesafe.akka" %% "akka-persistence-typed" % Version.akka
  lazy val akkaTestkitTyped = "com.typesafe.akka" %% "akka-actor-testkit-typed" % Version.akka % Test
  lazy val akkaStream = "com.typesafe.akka" %% "akka-stream" % Version.akka
  lazy val akkaStreamTyped = "com.typesafe.akka" %% "akka-stream-typed" % Version.akka
  lazy val akkaHttp = "com.typesafe.akka" %% "akka-http" % Version.akkaHttp
  lazy val akkaHttp2 = "com.typesafe.akka" %% "akka-http2-support" % Version.akkaHttp
  lazy val akkaHttpTestkit = "com.typesafe.akka" %% "akka-http-testkit" % Version.akkaHttp % Test
  lazy val akkaHttpCirce = "de.heikoseeberger" %% "akka-http-circe" % Version.akkaHttpJson
  lazy val akkaManagementClusterBootstrap = "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % Version.akkaClusterManagement
  lazy val akkaServiceDiscoveryDNS = "com.lightbend.akka.discovery" %% "akka-discovery-dns" % Version.akkaClusterManagement
  lazy val akkaServiceDiscoveryKubernetes = "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % Version.akkaClusterManagement
  lazy val akkaManagement = "com.lightbend.akka.management" %% "akka-management" % Version.akkaClusterManagement
  lazy val akkaClusterHttpManagement = "com.lightbend.akka.management" %% "akka-management-cluster-http" % Version.akkaClusterManagement
  lazy val akkaCors = "ch.megard" %% "akka-http-cors" % Version.akkaCors
  lazy val circe = "io.circe" %% "circe-core" % Version.circe
  lazy val circeGeneric = "io.circe" %% "circe-generic" % Version.circe
  lazy val circeParser = "io.circe" %% "circe-parser" % Version.circe
  lazy val akkaPersistenceCassandra = "com.typesafe.akka" %% "akka-persistence-cassandra" % Version.cassandraPluginVersion
  lazy val cassandraLauncher = "com.typesafe.akka" %% "akka-persistence-cassandra-launcher" % Version.cassandraPluginVersion
  lazy val slick = "com.typesafe.slick" %% "slick" % Version.slick
  lazy val slickHikaryCP = "com.typesafe.slick" %% "slick-hikaricp" % Version.slick
  lazy val postgres = "org.postgresql" % "postgresql" % Version.postgres
  lazy val h2 = "com.h2database" % "h2" % Version.h2 % Test
  lazy val keycloakAdapter = "org.keycloak" % "keycloak-adapter-core" % Version.keycloak
  lazy val keycloak = "org.keycloak" % "keycloak-core" % Version.keycloak
  lazy val jbossLogging = "org.jboss.logging" % "jboss-logging" % Version.jboss
  lazy val httpClient = "org.apache.httpcomponents" % "httpclient" % Version.httpcomponents
  lazy val akkaKafkaStreams = "com.typesafe.akka" %% "akka-stream-kafka" % Version.alpakkaKafka
  lazy val embeddedKafka = "net.manub" %% "scalatest-embedded-kafka" % Version.embeddedKafka
  lazy val alpakkaS3 = "com.lightbend.akka" %% "akka-stream-alpakka-s3" % Version.alpakka
  lazy val akkaQuartzScheduler = "com.enragedginger" %% "akka-quartz-scheduler" % Version.akkaQuartzScheduler
  lazy val phantomDSL = "com.outworkers" %% "phantom-dsl" % Version.phantom
  lazy val phantomJDK8 = "com.outworkers" %% "phantom-jdk8" % Version.phantom
  lazy val sbtResolver = "io.spray" %% "sbt-revolver" % Version.sprayresolver
  lazy val scalaCsv = "com.github.tototoshi" %% "scala-csv" % Version.scalaCsv
}
