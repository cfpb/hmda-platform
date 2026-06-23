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
  lazy val pekkoSlf4J                = "org.apache.pekko"   %% "pekko-slf4j"                  % Version.pekko
  lazy val pekkoTyped                = "org.apache.pekko"   %% "pekko-actor-typed"            % Version.pekko
  lazy val pekkoCluster              = "org.apache.pekko"   %% "pekko-cluster"                % Version.pekko
  lazy val pekkoClusterTyped         = "org.apache.pekko"   %% "pekko-cluster-typed"          % Version.pekko
  lazy val pekkoClusterSharding      = "org.apache.pekko"   %% "pekko-cluster-sharding"       % Version.pekko
  lazy val pekkoClusterShardingTyped = "org.apache.pekko"   %% "pekko-cluster-sharding-typed" % Version.pekko
  lazy val pekkoPersistence          = "org.apache.pekko"   %% "pekko-persistence"            % Version.pekko
  lazy val pekkoPersistenceTyped     = "org.apache.pekko"   %% "pekko-persistence-typed"      % Version.pekko
  lazy val pekkoPersistenceQuery     = "org.apache.pekko"   %% "pekko-persistence-query"      % Version.pekko
  lazy val pekkoTestkitTyped         = "org.apache.pekko"   %% "pekko-actor-testkit-typed"    % Version.pekko % Test
  lazy val pekkoStream               = "org.apache.pekko"   %% "pekko-stream"                 % Version.pekko
  lazy val pekkoStreamTyped          = "org.apache.pekko"   %% "pekko-stream-typed"           % Version.pekko
  lazy val pekkoStreamsTestKit       = "org.apache.pekko"   %% "pekko-stream-testkit"         % Version.pekko % Test
  lazy val pekkoHttp                 = "org.apache.pekko"   %% "pekko-http"                   % Version.pekkoHttp
  lazy val pekkoHttp2                = "org.playframework"   %% "play-pekko-http2-support"          % Version.pekkoHttp2Support
  lazy val pekkoHttpXml              = "org.apache.pekko"   %% "pekko-http-xml"               % Version.pekkoHttp
  lazy val pekkoHttpTestkit          = "org.apache.pekko"   %% "pekko-http-testkit"           % Version.pekkoHttp % Test
  lazy val pekkoHttpSprayJson        = "org.apache.pekko"   %% "pekko-http-spray-json"        % Version.pekkoHttp
  lazy val scalaLogging             = "com.typesafe.scala-logging"   %% "scala-logging"               % Version.scalaLogging
  lazy val slickPostgres            = "com.github.tminglei" %% "slick-pg"                    % Version.slickPostgres
  lazy val pekkoHttpCirce            = "com.github.pjfanning"   %% "pekko-http-circe"             % Version.pekkoHttpCirce
  lazy val pekkoManagementClusterBootstrap =
    "org.apache.pekko" %% "pekko-management-cluster-bootstrap" % Version.pekkoClusterManagement exclude ("org.apache.pekko", "pekko-http") exclude ("org.apache.pekko", "pekko-http-xml")
  lazy val pekkoServiceDiscoveryDNS =
    "org.apache.pekko" %% "pekko-discovery" % Version.pekko exclude ("org.apache.pekko", "pekko-http") exclude ("org.apache.pekko", "pekko-http-xml")
  lazy val pekkoServiceDiscoveryKubernetes =
    "org.apache.pekko" %% "pekko-discovery-kubernetes-api" % Version.pekkoClusterManagement exclude ("org.apache.pekko", "pekko-http") exclude ("org.apache.pekko", "pekko-http-xml")
  lazy val pekkoManagement =
    "org.apache.pekko" %% "pekko-management" % Version.pekkoClusterManagement exclude ("org.apache.pekko", "pekko-http") exclude ("org.apache.pekko", "pekko-http-xml")
  lazy val pekkoClusterHttpManagement =
    "org.apache.pekko" %% "pekko-management-cluster-http" % Version.pekkoClusterManagement exclude ("org.apache.pekko", "pekko-http") exclude ("org.apache.pekko", "pekko-http-xml")
  lazy val pekkoCors =
    "org.apache.pekko" %% "pekko-http-cors" % Version.pekkoCors exclude ("org.apache.pekko", "pekko-http") exclude ("org.apache.pekko", "pekko-http-xml")
  lazy val circe                    = "io.circe"           %% "circe-core"                          % Version.circe
  lazy val circeGeneric             = "io.circe"           %% "circe-generic"                       % Version.circe
  lazy val circeParser              = "io.circe"           %% "circe-parser"                        % Version.circe
  lazy val pekkoPersistenceCassandra = "org.apache.pekko"  %% "pekko-persistence-cassandra"          % Version.cassandraPluginVersion
  lazy val slick                    = "com.typesafe.slick" %% "slick"                               % Version.slick
  lazy val slickHikariCP            = "com.typesafe.slick" %% "slick-hikaricp"                      % Version.slick
  lazy val pekkoSlick             = "org.apache.pekko" %% "pekko-connectors-slick"           % Version.pekkoSlick
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
  lazy val pekkoKafkaStreams      = "org.apache.pekko"          %% "pekko-connectors-kafka"          % Version.pekkoKafkaStreams exclude("org.apache.kafka", "kafka-clients")
  lazy val kafkaClients          = "org.apache.kafka"           % "kafka-clients"               % Version.kafkaClients
  lazy val pekkoS3             = "org.apache.pekko"             %% "pekko-connectors-s3"     % Version.pekkoS3
  lazy val pekkoQuartzScheduler   = "io.github.samueleresca"    %% "pekko-quartz-scheduler"      % Version.pekkoQuartzScheduler
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
  lazy val pekkoFile             = "org.apache.pekko"           %% "pekko-connectors-file"      % Version.pekkoFile
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
