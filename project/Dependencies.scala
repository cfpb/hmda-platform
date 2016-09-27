import sbt._
import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

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
  val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % Version.akka % "test"
  val akkaStream = "com.typesafe.akka" %% "akka-stream" % Version.akka
  val akkaPersistence = "com.typesafe.akka" %% "akka-persistence" % Version.akka
  val akkaRemote = "com.typesafe.akka" %% "akka-remote" % Version.akka
  val scalaPB = "com.trueaccord.scalapb" %% "scalapb-runtime"  % Version.scalapb % PB.protobufConfig
  val leveldb = "org.iq80.leveldb" % "leveldb" % Version.leveldb
  val leveldbjni = "org.fusesource.leveldbjni" % "leveldbjni-all" % Version.leveldbjni
  val akkaPersistenceQuery = "com.typesafe.akka" %% "akka-persistence-query-experimental" % Version.akka
  val inMemoryPersistence = "com.github.dnvriend" %% "akka-persistence-inmemory" % Version.inMemoryPersistence % "test" exclude("com.github.dnvriend", "akka-persistence-query-writer")
  val akkaHttp = "com.typesafe.akka" %% "akka-http-experimental" % Version.akka
  val akkaHttpJson = "com.typesafe.akka" %% "akka-http-spray-json-experimental" % Version.akka
  val akkaHttpTestkit = "com.typesafe.akka" %% "akka-http-testkit" % Version.akka % "test"
  val config = "com.typesafe" % "config" % Version.config
  val enumeratum = "com.beachape" %% "enumeratum" % Version.enumeratum

}
