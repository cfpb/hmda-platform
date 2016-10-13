resolvers += "sonatype-releases" at "https://oss.sonatype.org/content/repositories/releases/"

resolvers += "bintray-sbt-plugin-releases" at "http://dl.bintray.com/content/sbt/sbt-plugin-releases"

addSbtPlugin("com.trueaccord.scalapb" % "sbt-scalapb" % "0.5.32")

// compiling *.proto files without protoc (for self contained builds)
// see: https://github.com/os72/protoc-jar
libraryDependencies ++= Seq(
  "com.github.os72" % "protoc-jar" % "3.0.0-b3"
)
