import sbtdocker.Instructions.User

packageName in Docker := "hmda-spark-reporting"

version := "0.1"

dockerfile in docker := {
  // The assembly task generates a fat JAR file
  val artifact: File     = assembly.value
  val artifactTargetPath = s"/opt/spark/cfpb/hmda/jars/${name.value}.jar"

  new Dockerfile {
    from("lightbend/spark:2.1.0-OpenShift-2.4.0-rh-2.12") //Uses 2.12 Scala
    User("jboss")
    run(
      "rm",
      "/opt/spark/jars/kubernetes-client-3.0.0.jar"
    )
    run(
      "curl",
      "https://repo1.maven.org/maven2/io/fabric8/kubernetes-client/4.4.2/kubernetes-client-4.4.2.jar",
      "--output",
      "/opt/spark/jars/kubernetes-client-4.4.2.jar",
      "--silent"
    )
    run(
      "curl",
      "https://repo1.maven.org/maven2/org/apache/hadoop/hadoop-aws/2.7.3/hadoop-aws-2.7.3.jar",
      "--output",
      "/opt/spark/jars/hadoop-aws-2.7.3.jar",
      "--silent"
    )
    run(
      "curl",
      "https://repo1.maven.org/maven2/com/amazonaws/aws-java-sdk/1.7.4/aws-java-sdk-1.7.4.jar",
      "--output",
      "/opt/spark/jars/aws-java-sdk-1.7.4.jar",
      "--silent"
    )
    run("mkdir", "-p", "/opt/spark/cfpb/hmda/jars/")

    add(artifact, artifactTargetPath)
  }
}
