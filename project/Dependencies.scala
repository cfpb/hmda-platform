import sbt._

object Dependencies {

  val repos = Seq(
   "Local Maven Repo"  at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    "Typesafe Repo"     at "http://repo.typesafe.com/typesafe/releases/",
    "Sonatype releases" at "https://oss.sonatype.org/content/repositories/releases",
    Resolver.bintrayRepo("mfglabs", "maven")    
  )

  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest % "test"
  val scalaCheck = "org.scalacheck" %% "scalacheck" % Version.scalaCheck % "test"
  val logback = "ch.qos.logback" % "logback-classic" % Version.logback
  val akkaSlf4J = "com.typesafe.akka" %% "akka-slf4j" % Version.akka
  val akka = "com.typesafe.akka" %% "akka-actor" % Version.akka
  val akkaHttp = "com.typesafe.akka" %% "akka-http-experimental" % Version.akkaHttp
  val akkaHttpJson = "com.typesafe.akka" %% "akka-http-spray-json-experimental" % Version.akkaHttp
  val akkaHttpTestkit = "com.typesafe.akka" %% "akka-http-testkit-experimental" % Version.akkaHttp

}
