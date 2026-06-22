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
  lazy val akkaSlf4J                = "org.apache.pekko"   %% "akka-slf4j"                  % Version.akka
  lazy val akkaTyped                = "org.apache.pekko"   %% "akka-actor-typed"            % Version.akka
  lazy val akkaCluster              = "org.apache.pekko"   %% "akka-cluster"                % Version.akka
  lazy val akkaClusterTyped         = "org.apache.pekko"   %% "akka-cluster-typed"          % Version.akka
  lazy val akkaClusterSharding      = "org.apache.pekko"   %% "akka-cluster-sharding"       % Version.akka
  lazy val akkaClusterShardingTyped = "org.apache.pekko"   %% "akka-cluster-sharding-typed" % Version.akka
  lazy val akkaPersistence          = "org.apache.pekko"   %% "pekko-persistence"            % Version.akka
  lazy val akkaPersistenceTyped     = "org.apache.pekko"   %% "pekko-persistence-typed"      % Version.akka
  lazy val akkaPersistenceQuery     = "org.apache.pekko"   %% "pekko-persistence-query"      % Version.akka
  lazy val akkaTestkitTyped         = "org.apache.pekko"   %% "akka-actor-testkit-typed"    % Version.akka % Test
  lazy val akkaStream               = "org.apache.pekko"   %% "akka-stream"                 % Version.akka
  lazy val akkaStreamTyped          = "org.apache.pekko"   %% "akka-stream-typed"           % Version.akka
  lazy val akkaStreamsTestKit       = "org.apache.pekko"   %% "akka-stream-testkit"         % Version.akka % Test
  lazy val akkaHttp                 = "org.apache.pekko"   %% "akka-http"                   % Version.akkaHttp
  lazy val akkaHttp2                = "org.apache.pekko"   %% "akka-http2-support"          % Version.akkaHttp2Support
  lazy val akkaHttpXml              = "org.apache.pekko"   %% "akka-http-xml"               % Version.akkaHttp
  lazy val akkaHttpTestkit          = "org.apache.pekko"   %% "akka-http-testkit"           % Version.akkaHttp % Test
  lazy val akkaHttpSprayJson        = "org.apache.pekko"   %% "akka-http-spray-json"        % Version.akkaHttp
  lazy val scalaLogging             = "com.typesafe.scala-logging"   %% "scala-logging"               % Version.scalaLogging
  lazy val slickPostgres            = "com.github.tminglei" %% "slick-pg"                    % Version.slickPostgres
  lazy val akkaHttpCirce            = "de.heikoseeberger"   %% "akka-http-circe"             % Version.akkaHttpJson
  lazy val akkaManagementClusterBootstrap =
    "com.lightbend.pekko.management" %% "akka-management-cluster-bootstrap" % Version.akkaClusterManagement exclude ("org.apache.pekko", "akka-http") exclude ("org.apache.pekko", "akka-http-xml")
  lazy val akkaServiceDiscoveryDNS =
    "org.apache.pekko" %% "akka-discovery" % Version.akka exclude ("org.apache.pekko", "akka-http") exclude ("org.apache.pekko", "akka-http-xml")
  lazy val akkaServiceDiscoveryKubernetes =
    "com.lightbend.pekko.discovery" %% "akka-discovery-kubernetes-api" % Version.akkaClusterManagement exclude ("org.apache.pekko", "akka-http") exclude ("org.apache.pekko", "akka-http-xml")
  lazy val akkaManagement =
    "com.lightbend.pekko.management" %% "akka-management" % Version.akkaClusterManagement exclude ("org.apache.pekko", "akka-http") exclude ("org.apache.pekko", "akka-http-xml")
  lazy val akkaClusterHttpManagement =
    "com.lightbend.pekko.management" %% "akka-management-cluster-http" % Version.akkaClusterManagement exclude ("org.apache.pekko", "akka-http") exclude ("org.apache.pekko", "akka-http-xml")
  lazy val akkaCors =
    "ch.megard" %% "akka-http-cors" % Version.akkaCors exclude ("org.apache.pekko", "akka-http") exclude ("org.apache.pekko", "akka-http-xml")
  lazy val circe                    = "io.circe"           %% "circe-core"                          % Version.circe
  lazy val circeGeneric             = "io.circe"           %% "circe-generic"                       % Version.circe
  lazy val circeParser              = "io.circe"           %% "circe-parser"                        % Version.circe
  lazy val akkaPersistenceCassandra = "org.apache.pekko"  %% "pekko-persistence-cassandra"          % Version.cassandraPluginVersion
  lazy val slick                    = "com.typesafe.slick" %% "slick"                               % Version.slick
  lazy val slickHikariCP            = "com.typesafe.slick" %% "slick-hikaricp"                      % Version.slick
  lazy val alpakkaSlick             = "com.lightbend.akka" %% "akka-stream-alpakka-slick"           % Version.alpakka
  lazy val postgres                 = "org.postgresql"     % "postgresql"                           % Version.postgres
  lazy val h2                       = "com.h2database"     % "h2"                                   % Version.h2 % Test
  lazy val testContainers        = "org.testcontainers"         % "testcontainers"              % Version.testContainers % Test
  lazy val apacheCommonsIO        = "commons-io"                % "commons-io"                  % Version.apacheCommons % Test
  lazy val keycloakAdmin         = "org.keycloak"               % "keycloak-admin-client"       % Version.keycloakAdmin exclude("org.jboss.logging", "commons-logging-jboss-logging")
  lazy val keycloakCommons       = "org.keycloak"               % "keycloak-client-common-synced" % Version.keycloakCommons
  lazy val resteasyClient        = "org.jboss.resteasy"         % "resteasy-client"             % Version.resteasy % "provided"
  lazy val resteasyJackson       = "org.jboss.resteasy"         % "resteasy-jackson2-provider"  % Version.resteasy % "provided"
  lazy val resteasyMulti         = "org.jboss.resteasy"         % "resteasy-multipart-provider" % Version.resteasy % "provided"
  lazy val jbossLogging          = "org.jboss.logging"          % "jboss-logging"               % Version.jbossLogging
  lazy val httpClient            = "org.apache.httpcomponents"  % "httpclient"                  % Version.httpcomponents
  lazy val akkaKafkaStreams      = "org.apache.pekko"          %% "akka-stream-kafka"          % Version.akkaKafka exclude("org.apache.kafka", "kafka-clients")
  lazy val kafkaClients          = "org.apache.kafka"           % "kafka-clients"               % Version.kafkaClients
  lazy val alpakkaS3             = "com.lightbend.akka"         %% "akka-stream-alpakka-s3"     % Version.alpakkaS3
  lazy val pekkoQuartzScheduler   = "org.apache"          %% "pekko.extension.quartz"      % Version.pekkoQuartzScheduler
  lazy val sbtResolver           = "io.spray"                   %% "sbt-revolver"               % Version.sprayresolver
  lazy val enumeratum            = "com.beachape"               %% "enumeratum"                 % Version.enumeratum
  lazy val enumeratumCirce       = "com.beachape"               %% "enumeratum-circe"           % Version.enumeratumCirce
  lazy val monix                 = "io.monix"                   %% "monix"                      % Version.monix
  lazy val lettuce               = "io.lettuce"                 % "lettuce-core"                % Version.lettuce
  lazy val guava                 = "com.google.guava"           % "guava"                       % Version.guava
  lazy val awsSesSdk             = "com.amazonaws"              % "aws-java-sdk-ses"            % Version.awsSesSdk exclude ("commons-logging", "commons-logging")
  lazy val jakartaMail           = "jakarta.mail"               % "jakarta.mail-api"            % Version.jakartaMail
  lazy val zeroAllocationHashing = "net.openhft"                % "zero-allocation-hashing"     % Version.zeroAllocation
  lazy val cormorant             = "io.chrisdavenport"          %% "cormorant-parser"           % Version.cormorant
  lazy val cormorantGeneric      = "io.chrisdavenport"          %% "cormorant-generic"          % Version.cormorant
  lazy val alpakkaFile           = "com.lightbend.akka"         %% "akka-stream-alpakka-file"   % Version.alpakka
  lazy val scalacheckShapeless   = "com.github.alexarchambault" %% "scalacheck-shapeless_1.14"  % Version.scalacheckShapeless % Test
  lazy val diffx                 = "com.softwaremill.diffx"     %% "diffx-core"                 % Version.diffx % Test
  lazy val kubernetesApi         = "io.kubernetes"              % "client-java"                 % Version.kubernetesApi
  lazy val keyspacedriver        = "software.aws.mcs"           % "aws-sigv4-auth-cassandra-java-driver-plugin"  % Version.keyspaceDriver
  // https://mvnrepository.com/artifact/software.amazon.msk/aws-msk-iam-auth
  lazy val mskdriver             = "software.amazon.msk"        % "aws-msk-iam-auth"            % Version.mskAuth
  // overriding the log4j-slf4j bridge used by spring, transitively brought in by s3mock
  // this is needed because of CVE-2021-44228 https://cve.mitre.org/cgi-bin/cvename.cgi?name=CVE-2021-44228
  lazy val log4jToSlf4j          = "org.apache.logging.log4j"   % "log4j-to-slf4j"              % Version.log4j % Test
}
