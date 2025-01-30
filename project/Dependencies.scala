import sbt.*

object Dependencies {

  val repos = Seq(
    "Local Maven Repo" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/",
    "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases"
  )

  val scalaTest                     = "org.scalatest"       %% "scalatest"                   % Version.scalaTest % Test
  val scalaCheck                    = "org.scalacheck"      %% "scalacheck"                  % Version.scalaCheck % Test
  val scalaMock                     = "org.scalamock"       %% "scalamock"                   % Version.scalaMock % Test
  val logback                       = "ch.qos.logback"      % "logback-classic"              % Version.logback
  lazy val akkaSlf4J                = "com.typesafe.akka"   %% "akka-slf4j"                  % Version.akka
  lazy val akkaTyped                = "com.typesafe.akka"   %% "akka-actor-typed"            % Version.akka
  lazy val akkaCluster              = "com.typesafe.akka"   %% "akka-cluster"                % Version.akka
  lazy val akkaClusterTyped         = "com.typesafe.akka"   %% "akka-cluster-typed"          % Version.akka
  lazy val akkaClusterSharding      = "com.typesafe.akka"   %% "akka-cluster-sharding"       % Version.akka
  lazy val akkaClusterShardingTyped = "com.typesafe.akka"   %% "akka-cluster-sharding-typed" % Version.akka
  lazy val akkaPersistence          = "com.typesafe.akka"   %% "akka-persistence"            % Version.akka
  lazy val akkaPersistenceTyped     = "com.typesafe.akka"   %% "akka-persistence-typed"      % Version.akka
  lazy val akkaPersistenceQuery     = "com.typesafe.akka"   %% "akka-persistence-query"      % Version.akka
  lazy val akkaTestkitTyped         = "com.typesafe.akka"   %% "akka-actor-testkit-typed"    % Version.akka % Test
  lazy val akkaStream               = "com.typesafe.akka"   %% "akka-stream"                 % Version.akka
  lazy val akkaStreamTyped          = "com.typesafe.akka"   %% "akka-stream-typed"           % Version.akka
  lazy val akkaStreamsTestKit       = "com.typesafe.akka"   %% "akka-stream-testkit"         % Version.akka % Test
  lazy val akkaHttp                 = "com.typesafe.akka"   %% "akka-http"                   % Version.akkaHttp
  lazy val akkaHttp2                = "com.typesafe.akka"   %% "akka-http2-support"          % Version.akkaHttp2Support
  lazy val akkaHttpXml              = "com.typesafe.akka"   %% "akka-http-xml"               % Version.akkaHttp
  lazy val akkaHttpTestkit          = "com.typesafe.akka"   %% "akka-http-testkit"           % Version.akkaHttp % Test
  lazy val akkaHttpSprayJson        = "com.typesafe.akka"   %% "akka-http-spray-json"        % Version.akkaHttp
  lazy val slickPostgres            = "com.github.tminglei" %% "slick-pg"                    % Version.slickPostgres
  lazy val akkaHttpCirce            = "de.heikoseeberger"   %% "akka-http-circe"             % Version.akkaHttpJson
  lazy val akkaManagementClusterBootstrap =
    "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % Version.akkaClusterManagement exclude ("com.typesafe.akka", "akka-http") exclude ("com.typesafe.akka", "akka-http-xml")
  lazy val akkaServiceDiscoveryDNS =
    "com.typesafe.akka" %% "akka-discovery" % Version.akka exclude ("com.typesafe.akka", "akka-http") exclude ("com.typesafe.akka", "akka-http-xml")
  lazy val akkaServiceDiscoveryKubernetes =
    "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % Version.akkaClusterManagement exclude ("com.typesafe.akka", "akka-http") exclude ("com.typesafe.akka", "akka-http-xml")
  lazy val akkaManagement =
    "com.lightbend.akka.management" %% "akka-management" % Version.akkaClusterManagement exclude ("com.typesafe.akka", "akka-http") exclude ("com.typesafe.akka", "akka-http-xml")
  lazy val akkaClusterHttpManagement =
    "com.lightbend.akka.management" %% "akka-management-cluster-http" % Version.akkaClusterManagement exclude ("com.typesafe.akka", "akka-http") exclude ("com.typesafe.akka", "akka-http-xml")
  lazy val akkaCors =
    "ch.megard" %% "akka-http-cors" % Version.akkaCors exclude ("com.typesafe.akka", "akka-http") exclude ("com.typesafe.akka", "akka-http-xml")
  lazy val circe                    = "io.circe"           %% "circe-core"                          % Version.circe
  lazy val circeGeneric             = "io.circe"           %% "circe-generic"                       % Version.circe
  lazy val circeParser              = "io.circe"           %% "circe-parser"                        % Version.circe
  lazy val akkaPersistenceCassandra = "com.typesafe.akka"  %% "akka-persistence-cassandra"          % Version.cassandraPluginVersion
  lazy val cassandraLauncher        = "com.typesafe.akka"  %% "akka-persistence-cassandra-launcher" % Version.cassandraLauncher
  lazy val slick                    = "com.typesafe.slick" %% "slick"                               % Version.slick
  lazy val slickHikariCP            = "com.typesafe.slick" %% "slick-hikaricp"                      % Version.slick
  lazy val alpakkaSlick             = "com.lightbend.akka" %% "akka-stream-alpakka-slick"           % Version.alpakka
  lazy val postgres                 = "org.postgresql"     % "postgresql"                           % Version.postgres
  lazy val h2                       = "com.h2database"     % "h2"                                   % Version.h2 % Test
  lazy val testContainers        = "org.testcontainers"         % "testcontainers"              % Version.testContainers % "test"
  lazy val apacheCommonsIO        = "commons-io"                % "commons-io"                  % Version.apacheCommons % Test
  lazy val keycloakAdapter       = "org.keycloak"               % "keycloak-adapter-core"       % Version.keycloak
  lazy val keycloak              = "org.keycloak"               % "keycloak-core"               % Version.keycloak
  lazy val keycloakAdmin         = "org.keycloak"               % "keycloak-admin-client"       % Version.keycloak
  lazy val resteasyClient        = "org.jboss.resteasy"         % "resteasy-client"             % Version.resteasy % "provided"
  lazy val resteasyJackson       = "org.jboss.resteasy"         % "resteasy-jackson2-provider"  % Version.resteasy % "provided"
  lazy val resteasyMulti         = "org.jboss.resteasy"         % "resteasy-multipart-provider" % Version.resteasy % "provided"
  lazy val jbossLogging          = "org.jboss.logging"          % "jboss-logging"               % Version.jbossLogging
  lazy val httpClient            = "org.apache.httpcomponents"  % "httpclient"                  % Version.httpcomponents
  lazy val akkaKafkaStreams      = "com.typesafe.akka"          %% "akka-stream-kafka"          % Version.akkaKafka
  lazy val embeddedKafka         = "io.github.embeddedkafka"    %% "embedded-kafka"             % Version.embeddedKafka
  lazy val alpakkaS3             = "com.lightbend.akka"         %% "akka-stream-alpakka-s3"     % Version.alpakkaS3
  lazy val akkaQuartzScheduler   = "com.enragedginger"          %% "akka-quartz-scheduler"      % Version.akkaQuartzScheduler
  lazy val sbtResolver           = "io.spray"                   %% "sbt-revolver"               % Version.sprayresolver
  lazy val enumeratum            = "com.beachape"               %% "enumeratum"                 % Version.enumeratum
  lazy val enumeratumCirce       = "com.beachape"               %% "enumeratum-circe"           % Version.enumeratumCirce
  lazy val monix                 = "io.monix"                   %% "monix"                      % Version.monix
  lazy val lettuce               = "io.lettuce"                 % "lettuce-core"                % Version.lettuce
  lazy val scalaJava8Compat      = "org.scala-lang.modules"     %% "scala-java8-compat"         % Version.java8Compat
  lazy val guava                 = "com.google.guava"           % "guava"                       % Version.guava
  lazy val awsSesSdk             = "com.amazonaws"              % "aws-java-sdk-ses"            % Version.awsSesSdk exclude ("commons-logging", "commons-logging")
  lazy val zeroAllocationHashing = "net.openhft"                % "zero-allocation-hashing"     % Version.zeroAllocation
  lazy val cormorant             = "io.chrisdavenport"          %% "cormorant-parser"           % Version.cormorant
  lazy val cormorantGeneric      = "io.chrisdavenport"          %% "cormorant-generic"          % Version.cormorant
  lazy val alpakkaFile           = "com.lightbend.akka"         %% "akka-stream-alpakka-file"   % Version.alpakka
  lazy val scalacheckShapeless   = "com.github.alexarchambault" %% "scalacheck-shapeless_1.14"  % Version.scalacheckShapeless % Test
  lazy val diffx                 = "com.softwaremill.diffx"     %% "diffx-core"                 % Version.diffx % Test
  lazy val kubernetesApi         = "io.kubernetes"              % "client-java"                 % Version.kubernetesApi
  lazy val keyspacedriver        = "software.aws.mcs"           % "aws-sigv4-auth-cassandra-java-driver-plugin"  % "4.0.9"
  // https://mvnrepository.com/artifact/software.amazon.msk/aws-msk-iam-auth
  lazy val mskdriver             = "software.amazon.msk"        % "aws-msk-iam-auth"           % "2.2.0"
  // overriding the log4j-slf4j bridge used by spring, transitively brought in by s3mock
  // this is needed because of CVE-2021-44228 https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2021-44228
  lazy val log4jToSlf4j          = "org.apache.logging.log4j"   % "log4j-to-slf4j"              % Version.log4j % Test
}
