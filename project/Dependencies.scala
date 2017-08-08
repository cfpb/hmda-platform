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
  val scalaz = "org.scalaz" %% "scalaz-core" % Version.scalaz
  val akkaSlf4J = "com.typesafe.akka" %% "akka-slf4j" % Version.akka
  val akka = "com.typesafe.akka" %% "akka-actor" % Version.akka
  val akkaCluster = "com.typesafe.akka" %% "akka-cluster" % Version.akka
  val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % Version.akka % "test"
  val akkaStream = "com.typesafe.akka" %% "akka-stream" % Version.akka
  val akkaPersistence = "com.typesafe.akka" %% "akka-persistence" % Version.akka
  val leveldb = "org.iq80.leveldb" % "leveldb" % Version.leveldb
  val leveldbjni = "org.fusesource.leveldbjni" % "leveldbjni-all" % Version.leveldbjni
  val akkaPersistenceQuery = "com.typesafe.akka" %% "akka-persistence-query-experimental" % Version.akka
  val inMemoryPersistence = "com.github.dnvriend" %% "akka-persistence-inmemory" % Version.inMemoryPersistence exclude("com.github.dnvriend", "akka-persistence-query-writer")
  val cassandraPersistence = "com.typesafe.akka" %% "akka-persistence-cassandra" % Version.cassandra
  val akkaHttp = "com.typesafe.akka" %% "akka-http" % Version.akkaHttp
  val akkaHttpJson = "com.typesafe.akka" %% "akka-http-spray-json" % Version.akkaHttp
  val akkaHttpTestkit = "com.typesafe.akka" %% "akka-http-testkit" % Version.akkaHttp % "test"
  val typesafeConfig = "com.typesafe" % "config" % Version.config
  val enumeratum = "com.beachape" %% "enumeratum" % Version.enumeratum
  val scalaCsv = "com.github.tototoshi" %% "scala-csv" % Version.scalaCsv
  val constructr = "de.heikoseeberger" %% "constructr" % Version.constructrVersion
  val constructrZookeeper = "com.lightbend.constructr" %% "constructr-coordination-zookeeper" % Version.constructrZookeeperVersion
  val cassandraUnit = "org.cassandraunit" % "cassandra-unit" % Version.cassandraUnit
  val alpakkaCassandra = "com.lightbend.akka" %% "akka-stream-alpakka-cassandra" % Version.alpakka
  val cassandraDriver = "com.datastax.cassandra" % "cassandra-driver-core" % Version.cassandraDriver
}
