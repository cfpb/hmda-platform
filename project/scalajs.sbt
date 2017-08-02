// NOTE: adding ScalaPB here before Scala.js due to protoc conflict.
// See https://github.com/scalapb/ScalaPB/issues/150

addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.8")

libraryDependencies += "com.trueaccord.scalapb" %% "compilerplugin" % "0.5.47"

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.19")