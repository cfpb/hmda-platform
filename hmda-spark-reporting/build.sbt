name := "hmda-reports"

version := "0.1"

dockerfile in docker := {
  // The assembly task generates a fat JAR file
  val artifact: File = assembly.value
  val artifactTargetPath = s"/opt/spark/examples/jars/${name.value}.jar"

  new Dockerfile {
    //    from("gcr.io/spark-operator/spark:v2.4.0")
    from("lightbend/spark:2.1.0-OpenShift-2.4.0-rh-2.12") //Uses 2.12 Scala
//    from("lightbend/spark:2.1.0-OpenShift-2.4.0")
    addRaw(
      "http://central.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/0.3.1/jmx_prometheus_javaagent-0.3.1.jar",
      "/prometheus/jmx_prometheus_javaagent.jar"
    )
    addRaw(
      "http://central.maven.org/maven2/org/apache/hadoop/hadoop-aws/2.7.3/hadoop-aws-2.7.3.jar",
      "$SPARK_HOME/jars")
    addRaw(
      "http://central.maven.org/maven2/com/amazonaws/aws-java-sdk/1.7.4/aws-java-sdk-1.7.4.jar",
      "$SPARK_HOME/jars")

    add(artifact, artifactTargetPath)
  }
}
