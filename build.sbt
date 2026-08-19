import Dependencies._
import BuildSettings._
import sbtassembly.AssemblyPlugin.autoImport.assemblyMergeStrategy
import com.typesafe.sbt.packager.docker._

lazy val commonDeps = Seq(logback, scalaTest, scalaCheck, pekkoHttpSprayJson, testContainers, apacheCommonsIO, log4jToSlf4j, kubernetesApi, scalaLogging)

lazy val sparkDeps =
  Seq(
    postgres,
    pekkoKafkaStreams,
    kafkaClients
  )

lazy val authDeps = Seq(keycloakAdmin, jbossLogging, httpClient)

lazy val keycloakServerDeps = Seq(resteasyClient, resteasyJackson, resteasyMulti)

lazy val pekkoDeps = Seq(
  pekkoSlf4J,
  pekkoCluster,
  pekkoTyped,
  pekkoClusterTyped,
  pekkoStream,
  pekkoStreamTyped,
  pekkoManagement,
  pekkoManagementClusterBootstrap,
  pekkoServiceDiscoveryDNS,
  pekkoServiceDiscoveryKubernetes,
  pekkoClusterHttpManagement,
  pekkoClusterHttpManagement,
  pekkoTestkitTyped,
  pekkoStreamsTestKit,
  pekkoCors,
  mskdriver,
  pekkoKafkaStreams,
  kafkaClients,
  pekkoS3,
  pekkoQuartzScheduler,
  pekkoFile
)

lazy val pekkoPersistenceDeps =
  Seq(
    pekkoPersistence,
    pekkoClusterSharding,
    pekkoPersistenceTyped,
    pekkoPersistenceQuery,
    pekkoClusterShardingTyped,
    pekkoPersistenceCassandra,
    keyspacedriver
  )

lazy val pekkoHttpDeps =
  Seq(pekkoHttp, pekkoHttp2, pekkoHttpXml, pekkoHttpTestkit, pekkoStreamsTestKit, pekkoHttpCirce)
lazy val circeDeps      = Seq(circe, circeGeneric, circeParser)
lazy val enumeratumDeps = Seq(enumeratum, enumeratumCirce)
lazy val slickDeps = Seq(slick, slickHikariCP, postgres, h2)
lazy val metaInfMatcher = """META-INF/.+\.(SF|DSA|RSA)""".r
lazy val dockerSettings = Seq(
  dockerBuildCommand := {
    //force amd64 Architecture for k8s docker image compatability
    if (sys.props("os.arch") != "amd64") {
      dockerExecCommand.value ++ Seq("buildx", "build", "--platform=linux/amd64","--provenance=false", "--load") ++ dockerBuildOptions.value :+ "."
    } else dockerBuildCommand.value
  },
  Docker / maintainer := "Hmda-Ops",
  dockerBaseImage := "dhi.io/eclipse-temurin:26.0.1.8-alpine3.24-dev",

  dockerRepository := Some("hmda"),
  Docker / daemonUser := "nonroot",

)

lazy val packageSettings = Seq(
  // removes all jar mappings in universal and appends the fat jar
  Universal / mappings := {
    // universalMappings: Seq[(File,String)]
    val universalMappings = (Universal / mappings).value
    val fatJar            = (Compile / assembly).value
    // removing means filtering
    val filtered = universalMappings filter {
      case (_, fileName) => !fileName.endsWith(".jar") || fileName.contains("cinnamon-agent")
    }
    // add the fat jar
    filtered :+ (fatJar -> ("lib/" + fatJar.getName))
  },
  // the bash scripts classpath only needs the fat jar
  scriptClasspath := Seq((assembly / assemblyJarName).value)
)

lazy val `hmda-root` = (project in file("."))
  .settings(hmdaBuildSettings: _*)
  .aggregate(
    common,
    `hmda-platform`,
    `check-digit`,
    `file-proxy`,
    `institutions-api`,
    `modified-lar`,
    `hmda-analytics`,
    `hmda-auth`,
    `hmda-data-publisher`,
    `hmda-reporting`,
    `ratespread-calculator`,
    `data-browser`,
    `submission-errors`,
    `hmda-quarterly-data-service`
  )

val latestGitTag = settingKey[String]("The latest git tag.")
ThisBuild / latestGitTag := {
  import scala.sys.process._

  val hasTags = "git tag".lineStream_!.nonEmpty

  if (hasTags) {
    "git describe --tags".lineStream_!.head
  } else {
    Keys.sLog.value.warn("No git tags in the checkout, using '-' for build info.")
    "-"
  }
}

lazy val common = (project in file("common"))
  .settings(hmdaBuildSettings: _*)
  .settings(
    Compile / PB.targets := Seq(
      scalapb.gen() -> (Compile / sourceManaged).value / "protobuf"
    ),
    Seq(
      libraryDependencies ++= commonDeps ++ authDeps ++ pekkoDeps ++ pekkoPersistenceDeps ++ pekkoHttpDeps ++ circeDeps ++ slickDeps ++ List(
        cormorant, cormorantGeneric, scalaMock, scalacheckShapeless, diffx
      )
    ),
    Seq(dependencyOverrides ++= Seq(jacksonDatabind,pekkoSerialization ))
    ,
    // addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    // https://github.com/aws-samples/amazon-keyspaces-java-driver-helpers
    Runtime / unmanagedBase := baseDirectory.value / "lib"
  )
  .enablePlugins(BuildInfoPlugin)
  .settings(
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion, latestGitTag),
    buildInfoPackage := "hmda"
  )

lazy val `hmda-platform` = (project in file("hmda"))
  .enablePlugins(
    JavaServerAppPackaging,
    sbtdocker.DockerPlugin,
    AshScriptPlugin
  )
  .settings(hmdaBuildSettings: _*)
  .settings(
    Seq(
      libraryDependencies ++= List(guava, zeroAllocationHashing),
      dependencyOverrides ++= Seq(jacksonDatabind,pekkoSerialization ),
      Compile / mainClass := Some("hmda.HmdaPlatform"),
      assembly / assemblyJarName := "hmda2.jar",
      assembly / assemblyMergeStrategy := {
        case "application.conf"                      => MergeStrategy.concat
        case "cinnamon-reference.conf"               => MergeStrategy.concat
        case "META-INF/io.netty.versions.properties" => MergeStrategy.concat
        case "logback.xml"                           => MergeStrategy.concat
        case "META-INF/MANIFEST.MF" => MergeStrategy.discard
        case metaInfMatcher(_) => MergeStrategy.discard
        case PathList("META-INF", xs@_*) => MergeStrategy.concat
        case PathList("org", "bouncycastle", xs @_*) => MergeStrategy.first
        case PathList("jakarta", xs@_*) => MergeStrategy.last
        case PathList(ps @ _*) if ps.last endsWith ".proto" =>
          MergeStrategy.first
        case "module-info.class" => MergeStrategy.concat
        case PathList("META-INF", "services", xs @ _*) => MergeStrategy.concat
        case PathList("net", "jpountz", xs @ _*) => MergeStrategy.last
        case PathList("org", "lz4", xs @ _*) => MergeStrategy.last
        case PathList(ps @ _*) if ps.last endsWith ".proto" => MergeStrategy.first
        case PathList("com", "datastax", xs@_*) => MergeStrategy.first
        case PathList("org", "apache", xs@_*) => MergeStrategy.first
        case PathList("org", "glassfish", xs@_*) => MergeStrategy.first
        case PathList("com", "sun", xs@_*) => MergeStrategy.first
        case x if x.endsWith("reference-overrides.conf") => MergeStrategy.concat
        case x if x.endsWith("reference.conf") => MergeStrategy.concat
        case x if x.endsWith("version.conf") => MergeStrategy.concat
        case x if x.endsWith("module-info.class") => MergeStrategy.concat
        case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
        case x if x.endsWith("/liblz4-java.so") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4ByteBufferUtils$Match.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
        case x if x.endsWith("/module-info.class") => MergeStrategy.concat
        case x if x.endsWith("/LineTokenizer.class") => MergeStrategy.concat
        case x if x.endsWith("/LogSupport.class") => MergeStrategy.concat
        case x if x.endsWith("/MailcapFile.class") => MergeStrategy.concat
        case x if x.endsWith("/MimeTypeFile.class") => MergeStrategy.concat
        case x =>
          val oldStrategy = (assembly / assemblyMergeStrategy).value
          oldStrategy(x)
      },
    reStart / envVars ++= Map("CASSANDRA_CLUSTER_HOSTS" -> "localhost", "APP_PORT" -> "2551"),
    ),
    dockerSettings,
    packageSettings
  )
  .dependsOn(common % "compile->compile;test->test")
  .dependsOn(`hmda-protocol` % "compile->compile;test->test")

lazy val `check-digit` = (project in file("check-digit"))
  .enablePlugins(
    JavaServerAppPackaging,
    sbtdocker.DockerPlugin,
    AshScriptPlugin
  )
  .settings(hmdaBuildSettings: _*)
  .settings(
    Seq(
      Compile / mainClass := Some("hmda.uli.HmdaUli"),
      dependencyOverrides ++= Seq(jacksonDatabind,pekkoSerialization ),
      assembly / assemblyJarName := {
        s"${name.value}.jar"
      },
      assembly / assemblyMergeStrategy := {

        case "application.conf"                      => MergeStrategy.concat
        case "META-INF/io.netty.versions.properties" => MergeStrategy.concat
        case "META-INF/MANIFEST.MF" => MergeStrategy.discard
        case metaInfMatcher(_) => MergeStrategy.discard
        case PathList("META-INF", xs @ _*) => MergeStrategy.concat
        case PathList("org", "bouncycastle", xs @_*) => MergeStrategy.first
        case PathList("jakarta", xs @ _*) => MergeStrategy.last
        case PathList(ps @ _*) if ps.last endsWith ".proto" =>
          MergeStrategy.first
        case "module-info.class" => MergeStrategy.concat
        case PathList("META-INF", "services", xs @ _*) => MergeStrategy.concat
        case PathList("net", "jpountz", xs @ _*) => MergeStrategy.last
        case PathList("org", "lz4", xs @ _*) => MergeStrategy.last
        case PathList(ps @ _*) if ps.last endsWith ".proto" => MergeStrategy.first
        case PathList("com", "datastax", xs@_*) => MergeStrategy.first
        case PathList("org", "apache", xs@_*) => MergeStrategy.first
        case PathList("org", "glassfish", xs@_*) => MergeStrategy.first
        case PathList("com", "sun", xs@_*) => MergeStrategy.first
        case x if x.endsWith("reference-overrides.conf") => MergeStrategy.concat
        case x if x.endsWith("reference.conf") => MergeStrategy.concat
        case x if x.endsWith("version.conf") => MergeStrategy.concat
        case x if x.endsWith("module-info.class") => MergeStrategy.concat
        case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
        case x if x.endsWith("/liblz4-java.so") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4ByteBufferUtils$Match.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
        case x if x.endsWith("/module-info.class") => MergeStrategy.concat
        case x if x.endsWith("/LineTokenizer.class") => MergeStrategy.concat
        case x if x.endsWith("/LogSupport.class") => MergeStrategy.concat
        case x if x.endsWith("/MailcapFile.class") => MergeStrategy.concat
        case x if x.endsWith("/MimeTypeFile.class") => MergeStrategy.concat
        case x =>
          val oldStrategy = (assembly / assemblyMergeStrategy).value
          oldStrategy(x)
      }
    ),
    dockerSettings,
    packageSettings
  )
  .dependsOn(common % "compile->compile;test->test")
  .dependsOn(`hmda-protocol` % "compile->compile;test->test")

  lazy val `file-proxy` = (project in file("file-proxy"))
    .enablePlugins(
      JavaServerAppPackaging,
      sbtdocker.DockerPlugin,
      AshScriptPlugin
    )
    .settings(hmdaBuildSettings: _*)
    .settings(
      Seq(
        libraryDependencies ++= commonDeps ++ pekkoDeps ++ pekkoHttpDeps ++ circeDeps ++ slickDeps ++
        enumeratumDeps :+ monix :+ lettuce :+ scalaMock,
        dependencyOverrides ++= Seq(jacksonDatabind,pekkoSerialization ),
        Compile / mainClass := Some("hmda.proxy.FileProxy"),
        assembly / assemblyJarName := {
          s"${name.value}.jar"
        },
        assembly/ assemblyMergeStrategy := {
          case "application.conf"                      => MergeStrategy.concat
          case "META-INF/io.netty.versions.properties" => MergeStrategy.concat
          case "META-INF/MANIFEST.MF" => MergeStrategy.discard
          case metaInfMatcher(_) => MergeStrategy.discard
          case PathList("META-INF", xs@_*) => MergeStrategy.concat
          case PathList("org", "bouncycastle", xs @_*) => MergeStrategy.first
          case PathList("jakarta", xs@_*) => MergeStrategy.last
          case PathList(ps @ _*) if ps.last endsWith ".proto" =>
            MergeStrategy.first
          case "module-info.class" => MergeStrategy.concat
          case PathList("META-INF", "services", xs @ _*) => MergeStrategy.concat
          case PathList("net", "jpountz", xs @ _*) => MergeStrategy.last
          case PathList("org", "lz4", xs @ _*) => MergeStrategy.last
          case PathList(ps @ _*) if ps.last endsWith ".proto" => MergeStrategy.first
          case PathList("com", "datastax", xs@_*) => MergeStrategy.first
          case PathList("org", "apache", xs@_*) => MergeStrategy.first
          case PathList("org", "glassfish", xs@_*) => MergeStrategy.first
          case PathList("com", "sun", xs@_*) => MergeStrategy.first
          case x if x.endsWith("reference-overrides.conf") => MergeStrategy.concat
          case x if x.endsWith("reference.conf") => MergeStrategy.concat
          case x if x.endsWith("version.conf") => MergeStrategy.concat
          case x if x.endsWith("module-info.class") => MergeStrategy.concat
          case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
          case x if x.endsWith("/liblz4-java.so") => MergeStrategy.concat
          case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
          case x if x.endsWith("/LZ4ByteBufferUtils$Match.class") => MergeStrategy.concat
          case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
          case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
          case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
          case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
          case x if x.endsWith("/module-info.class") => MergeStrategy.concat
          case x if x.endsWith("/LineTokenizer.class") => MergeStrategy.concat
          case x if x.endsWith("/LogSupport.class") => MergeStrategy.concat
          case x if x.endsWith("/MailcapFile.class") => MergeStrategy.concat
          case x if x.endsWith("/MimeTypeFile.class") => MergeStrategy.concat
          case x =>
            val oldStrategy = (assembly / assemblyMergeStrategy).value
            oldStrategy(x)
        }
      ),
      dockerSettings,
      packageSettings
    )
    .dependsOn(common % "compile->compile;test->test")


lazy val `institutions-api` = (project in file("institutions-api"))
  .enablePlugins(
    JavaServerAppPackaging,
    sbtdocker.DockerPlugin,
    AshScriptPlugin
  )
  .settings(hmdaBuildSettings: _*)
  .settings(
    Seq(
      Compile / mainClass := Some("hmda.institution.HmdaInstitutionApi"),
      dependencyOverrides ++= Seq(jacksonDatabind,pekkoSerialization ),
      assembly / assemblyMergeStrategy := {
        case "application.conf"                      => MergeStrategy.concat
        case "META-INF/io.netty.versions.properties" => MergeStrategy.concat
        case "META-INF/MANIFEST.MF" => MergeStrategy.discard
        case metaInfMatcher(_) => MergeStrategy.discard
        case PathList("META-INF", xs@_*) => MergeStrategy.concat
        case PathList("org", "bouncycastle", xs @_*) => MergeStrategy.first
        case PathList("jakarta", xs@_*) => MergeStrategy.last
        case PathList(ps @ _*) if ps.last endsWith ".proto" =>
          MergeStrategy.first
        case "module-info.class" => MergeStrategy.concat
        case PathList("META-INF", "services", xs @ _*) => MergeStrategy.concat
        case PathList("net", "jpountz", xs @ _*) => MergeStrategy.last
        case PathList("org", "lz4", xs @ _*) => MergeStrategy.last
        case PathList(ps @ _*) if ps.last endsWith ".proto" => MergeStrategy.first
        case PathList("com", "datastax", xs@_*) => MergeStrategy.first
        case PathList("org", "apache", xs@_*) => MergeStrategy.first
        case PathList("org", "glassfish", xs@_*) => MergeStrategy.first
        case PathList("com", "sun", xs@_*) => MergeStrategy.first
        case x if x.endsWith("reference-overrides.conf") => MergeStrategy.concat
        case x if x.endsWith("reference.conf") => MergeStrategy.concat
        case x if x.endsWith("version.conf") => MergeStrategy.concat
        case x if x.endsWith("module-info.class") => MergeStrategy.concat
        case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
        case x if x.endsWith("/liblz4-java.so") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4ByteBufferUtils$Match.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
        case x if x.endsWith("/module-info.class") => MergeStrategy.concat
        case x if x.endsWith("/LineTokenizer.class") => MergeStrategy.concat
        case x if x.endsWith("/LogSupport.class") => MergeStrategy.concat
        case x if x.endsWith("/MailcapFile.class") => MergeStrategy.concat
        case x if x.endsWith("/MimeTypeFile.class") => MergeStrategy.concat
        case x =>
          val oldStrategy = (assembly / assemblyMergeStrategy).value
          oldStrategy(x)
      },
      assembly / assemblyJarName := {
        s"${name.value}.jar"
      }
    ),
    dockerSettings,
    packageSettings
  )
  .dependsOn(common % "compile->compile;test->test")

lazy val `hmda-data-publisher` = (project in file("hmda-data-publisher"))
  .enablePlugins(
    JavaServerAppPackaging,
    sbtdocker.DockerPlugin,
    AshScriptPlugin
  )
  .settings(hmdaBuildSettings: _*)
  .settings(
    Seq(
      libraryDependencies ++= commonDeps ++ pekkoDeps ++ pekkoHttpDeps ++ circeDeps ++ slickDeps ++ enumeratumDeps :+
        scalaMock :+ cormorantGeneric :+ scalacheckShapeless :+ diffx,
      dependencyOverrides ++= Seq(jacksonDatabind,pekkoSerialization ),
      Compile / mainClass := Some("hmda.publisher.HmdaDataPublisherApp"),
      assembly / assemblyJarName := {
        s"${name.value}.jar"
      },
      assembly / assemblyMergeStrategy := {
        case "application.conf"                      => MergeStrategy.concat
        case "META-INF/io.netty.versions.properties" => MergeStrategy.concat
        case "META-INF/MANIFEST.MF" => MergeStrategy.discard
        case metaInfMatcher(_) => MergeStrategy.discard
        case PathList("META-INF", xs@_*) => MergeStrategy.concat
        case PathList("org", "bouncycastle", xs @_*) => MergeStrategy.first
        case PathList("jakarta", xs@_*) => MergeStrategy.last
        case PathList(ps @ _*) if ps.last endsWith ".proto" =>
          MergeStrategy.first
        case "module-info.class" => MergeStrategy.concat
        case PathList("META-INF", "services", xs @ _*) => MergeStrategy.concat
        case PathList("net", "jpountz", xs @ _*) => MergeStrategy.last
        case PathList("org", "lz4", xs @ _*) => MergeStrategy.last
        case PathList(ps @ _*) if ps.last endsWith ".proto" => MergeStrategy.first
        case PathList("com", "datastax", xs@_*) => MergeStrategy.first
        case PathList("org", "apache", xs@_*) => MergeStrategy.first
        case PathList("org", "glassfish", xs@_*) => MergeStrategy.first
        case PathList("com", "sun", xs@_*) => MergeStrategy.first
        case x if x.endsWith("reference-overrides.conf") => MergeStrategy.concat
        case x if x.endsWith("reference.conf") => MergeStrategy.concat
        case x if x.endsWith("version.conf") => MergeStrategy.concat
        case x if x.endsWith("module-info.class") => MergeStrategy.concat
        case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
        case x if x.endsWith("/liblz4-java.so") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4ByteBufferUtils$Match.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
        case x if x.endsWith("/module-info.class") => MergeStrategy.concat
        case x if x.endsWith("/LineTokenizer.class") => MergeStrategy.concat
        case x if x.endsWith("/LogSupport.class") => MergeStrategy.concat
        case x if x.endsWith("/MailcapFile.class") => MergeStrategy.concat
        case x if x.endsWith("/MimeTypeFile.class") => MergeStrategy.concat
        case x =>
          val oldStrategy = (assembly / assemblyMergeStrategy).value
          oldStrategy(x)
      }
    ),
    dockerSettings,
    packageSettings,
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")
  )
  .dependsOn(common % "compile->compile;test->test")
  .dependsOn(`hmda-protocol` % "compile->compile;test->test")

lazy val `hmda-dashboard` = (project in file("hmda-dashboard"))
  .enablePlugins(
    JavaServerAppPackaging,
    sbtdocker.DockerPlugin,
    AshScriptPlugin
  )
  .settings(hmdaBuildSettings: _*)
  .settings(
    Seq(
      libraryDependencies ++= commonDeps ++ pekkoDeps ++ pekkoHttpDeps ++ circeDeps ++ slickDeps ++
        enumeratumDeps :+ monix :+ lettuce :+ scalaMock,
      dependencyOverrides ++= Seq(jacksonDatabind,pekkoSerialization ),
      assembly / assemblyMergeStrategy := {
        case "application.conf"                      => MergeStrategy.concat
        case "META-INF/io.netty.versions.properties" => MergeStrategy.concat
        case "META-INF/MANIFEST.MF" => MergeStrategy.discard
        case metaInfMatcher(_) => MergeStrategy.discard
        case PathList("META-INF", xs@_*) => MergeStrategy.concat
        case PathList("org", "bouncycastle", xs @_*) => MergeStrategy.first
        case PathList("jakarta", xs@_*) => MergeStrategy.last
        case PathList(ps @ _*) if ps.last endsWith ".proto" => MergeStrategy.first
        case PathList("META-INF", "services", xs @ _*) => MergeStrategy.concat
        case PathList("net", "jpountz", xs @ _*) => MergeStrategy.last
        case PathList("org", "lz4", xs @ _*) => MergeStrategy.last
        case PathList(ps @ _*) if ps.last endsWith ".proto" => MergeStrategy.first
        case PathList("com", "datastax", xs@_*) => MergeStrategy.first
        case PathList("org", "apache", xs@_*) => MergeStrategy.first
        case PathList("org", "glassfish", xs@_*) => MergeStrategy.first
        case PathList("com", "sun", xs@_*) => MergeStrategy.first
        case x if x.endsWith("reference-overrides.conf") => MergeStrategy.concat
        case x if x.endsWith("reference.conf") => MergeStrategy.concat
        case x if x.endsWith("version.conf") => MergeStrategy.concat
        case x if x.endsWith("module-info.class") => MergeStrategy.concat
        case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
        case x if x.endsWith("/liblz4-java.so") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4ByteBufferUtils$Match.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
        case "module-info.class" => MergeStrategy.concat
        case x if x.endsWith("/module-info.class") => MergeStrategy.concat
        case x if x.endsWith("/LineTokenizer.class") => MergeStrategy.concat
        case x if x.endsWith("/LogSupport.class") => MergeStrategy.concat
        case x if x.endsWith("/MailcapFile.class") => MergeStrategy.concat
        case x if x.endsWith("/MimeTypeFile.class") => MergeStrategy.concat
        
        case x =>
          val oldStrategy = (assembly / assemblyMergeStrategy).value
          oldStrategy(x)
      },
      assembly / assemblyJarName := {
        s"${name.value}.jar"
      }
    ),
    dockerSettings,
    packageSettings
  )
  .dependsOn(common % "compile->compile;test->test")
  .dependsOn(`hmda-protocol` % "compile->compile;test->test")

lazy val `ratespread-calculator` = (project in file("ratespread-calculator"))
  .enablePlugins(
    JavaServerAppPackaging,
    sbtdocker.DockerPlugin,
    AshScriptPlugin
  )
  .settings(hmdaBuildSettings: _*)
  .settings(
    Seq(       dependencyOverrides ++= Seq(jacksonDatabind,pekkoSerialization ),
        Compile / mainClass := Some("hmda.calculator.HmdaRateSpread"),
      assembly / assemblyMergeStrategy := {
        case "application.conf"                      => MergeStrategy.concat
        case "META-INF/io.netty.versions.properties" => MergeStrategy.concat
        case "META-INF/MANIFEST.MF" => MergeStrategy.discard
        case "reference.conf" => MergeStrategy.concat
        case metaInfMatcher(_) => MergeStrategy.discard
        case PathList("META-INF", "services", xs @ _*) => MergeStrategy.concat

        case PathList("META-INF", xs@_*) => MergeStrategy.concat
        case PathList("net", "jpountz", xs @ _*) => MergeStrategy.last
        case PathList("org", "lz4", xs @ _*) => MergeStrategy.last
        case PathList("org", "bouncycastle", xs @_*) => MergeStrategy.first
        case PathList("jakarta", xs@_*) => MergeStrategy.last
        case PathList(ps @ _*) if ps.last endsWith ".proto" => MergeStrategy.first
        case PathList("com", "datastax", xs@_*) => MergeStrategy.first
        case PathList("org", "apache", xs@_*) => MergeStrategy.first
        case PathList("org", "glassfish", xs@_*) => MergeStrategy.first
        case PathList("com", "sun", xs@_*) => MergeStrategy.first
        case x if x.endsWith("reference-overrides.conf") => MergeStrategy.concat
        case x if x.endsWith("reference.conf") => MergeStrategy.concat
        case x if x.endsWith("version.conf") => MergeStrategy.concat
        case x if x.endsWith("module-info.class") => MergeStrategy.concat
        case x if x.endsWith("/LineTokenizer.class") => MergeStrategy.concat
        case x if x.endsWith("/LogSupport.class") => MergeStrategy.concat
        case x if x.endsWith("/MailcapFile.class") => MergeStrategy.concat
        case x if x.endsWith("/MimeTypeFile.class") => MergeStrategy.concat
        case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
        case x if x.endsWith("/liblz4-java.so") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4ByteBufferUtils$Match.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
        case x =>
          val oldStrategy = (assembly / assemblyMergeStrategy).value
          oldStrategy(x)
      },
      assembly / assemblyJarName := {
        s"${name.value}.jar"
      }
    ),
    dockerSettings,
    packageSettings
  )
  .dependsOn(common % "compile->compile;test->test")
  .dependsOn(`hmda-protocol` % "compile->compile;test->test")

lazy val `modified-lar` = (project in file("modified-lar"))
  .enablePlugins(
    JavaServerAppPackaging,
    sbtdocker.DockerPlugin,
    AshScriptPlugin
  )
  .settings(hmdaBuildSettings: _*)
  .settings(
    Seq(
      Compile / mainClass := Some("hmda.publication.lar.ModifiedLarApp"),
      dependencyOverrides ++= Seq(jacksonDatabind,pekkoSerialization ),
      assembly / assemblyMergeStrategy := {
        case "application.conf"                      => MergeStrategy.concat
        case "META-INF/io.netty.versions.properties" => MergeStrategy.concat
        case "META-INF/MANIFEST.MF" => MergeStrategy.discard
        case metaInfMatcher(_) => MergeStrategy.discard
        case PathList("META-INF", xs@_*) => MergeStrategy.concat
        case PathList("org", "bouncycastle", xs @_*) => MergeStrategy.first
        case PathList("jakarta", xs@_*) => MergeStrategy.last
        case PathList(ps @ _*) if ps.last endsWith ".proto" =>
          MergeStrategy.first
        case "module-info.class" => MergeStrategy.concat
        case PathList("META-INF", "services", xs @ _*) => MergeStrategy.concat
        case PathList("net", "jpountz", xs @ _*) => MergeStrategy.last
        case PathList("org", "lz4", xs @ _*) => MergeStrategy.last
        case PathList(ps @ _*) if ps.last endsWith ".proto" => MergeStrategy.first
        case PathList("com", "datastax", xs@_*) => MergeStrategy.first
        case PathList("org", "apache", xs@_*) => MergeStrategy.first
        case PathList("org", "glassfish", xs@_*) => MergeStrategy.first
        case PathList("com", "sun", xs@_*) => MergeStrategy.first
        case x if x.endsWith("reference-overrides.conf") => MergeStrategy.concat
        case x if x.endsWith("reference.conf") => MergeStrategy.concat
        case x if x.endsWith("version.conf") => MergeStrategy.concat
        case x if x.endsWith("module-info.class") => MergeStrategy.concat
        case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
        case x if x.endsWith("/liblz4-java.so") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4ByteBufferUtils$Match.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
        case x if x.endsWith("/module-info.class") => MergeStrategy.concat
        case x if x.endsWith("/LineTokenizer.class") => MergeStrategy.concat
        case x if x.endsWith("/LogSupport.class") => MergeStrategy.concat
        case x if x.endsWith("/MailcapFile.class") => MergeStrategy.concat
        case x if x.endsWith("/MimeTypeFile.class") => MergeStrategy.concat
        case x =>
          val oldStrategy = (assembly / assemblyMergeStrategy).value
          oldStrategy(x)
      },
      assembly / assemblyJarName := {
        s"${name.value}.jar"
      }
    ),
    dockerSettings,
    packageSettings
  )
  .dependsOn(common % "compile->compile;test->test")
  .dependsOn(`hmda-protocol`)
  .dependsOn(common)

lazy val `irs-publisher` = (project in file("irs-publisher"))
  .enablePlugins(
    JavaServerAppPackaging,
    sbtdocker.DockerPlugin,
    AshScriptPlugin
  )
  .settings(hmdaBuildSettings: _*)
  .settings(
    Seq(
      Compile / mainClass := Some("hmda.publication.lar.IrsPublisherApp"),
      dependencyOverrides ++= Seq(jacksonDatabind,pekkoSerialization ),
      assembly / assemblyMergeStrategy := {
        case "application.conf"                      => MergeStrategy.concat
        case "META-INF/io.netty.versions.properties" => MergeStrategy.concat
        case "META-INF/MANIFEST.MF" => MergeStrategy.discard
        case metaInfMatcher(_) => MergeStrategy.discard
        case PathList("META-INF", xs@_*) => MergeStrategy.concat
        case PathList("org", "bouncycastle", xs @_*) => MergeStrategy.first
        case PathList("jakarta", xs@_*) => MergeStrategy.last
        case PathList(ps @ _*) if ps.last endsWith ".proto" =>
          MergeStrategy.first
        case "module-info.class" => MergeStrategy.concat
        case PathList("META-INF", "services", xs @ _*) => MergeStrategy.concat
        case PathList("net", "jpountz", xs @ _*) => MergeStrategy.last
        case PathList("org", "lz4", xs @ _*) => MergeStrategy.last
        case PathList(ps @ _*) if ps.last endsWith ".proto" => MergeStrategy.first
        case PathList("com", "datastax", xs@_*) => MergeStrategy.first
        case PathList("org", "apache", xs@_*) => MergeStrategy.first
        case PathList("org", "glassfish", xs@_*) => MergeStrategy.first
        case PathList("com", "sun", xs@_*) => MergeStrategy.first
        case x if x.endsWith("reference-overrides.conf") => MergeStrategy.concat
        case x if x.endsWith("reference.conf") => MergeStrategy.concat
        case x if x.endsWith("version.conf") => MergeStrategy.concat
        case x if x.endsWith("module-info.class") => MergeStrategy.concat
        case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
        case x if x.endsWith("/liblz4-java.so") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4ByteBufferUtils$Match.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
        case x if x.endsWith("/module-info.class") => MergeStrategy.concat
        case x if x.endsWith("/LineTokenizer.class") => MergeStrategy.concat
        case x if x.endsWith("/LogSupport.class") => MergeStrategy.concat
        case x if x.endsWith("/MailcapFile.class") => MergeStrategy.concat
        case x if x.endsWith("/MimeTypeFile.class") => MergeStrategy.concat
        case x =>
          val oldStrategy = (assembly / assemblyMergeStrategy).value
          oldStrategy(x)
      },
      assembly / assemblyJarName := {
        s"${name.value}.jar"
      }
    ),
    dockerSettings,
    packageSettings
  )
  .dependsOn(common % "compile->compile;test->test")
  .dependsOn(`hmda-protocol`)
  .dependsOn(common)

lazy val `hmda-reporting` = (project in file("hmda-reporting"))
  .enablePlugins(
    JavaServerAppPackaging,
    sbtdocker.DockerPlugin,
    AshScriptPlugin
  )
  .settings(hmdaBuildSettings: _*)
  .settings(
    Seq(
      Compile / mainClass := Some("hmda.reporting.HmdaReporting"),
      dependencyOverrides ++= Seq(jacksonDatabind,pekkoSerialization ),
      assembly / assemblyMergeStrategy := {
        case "application.conf"                      => MergeStrategy.concat
        case "META-INF/io.netty.versions.properties" => MergeStrategy.concat
        case "META-INF/MANIFEST.MF" => MergeStrategy.discard
        case metaInfMatcher(_) => MergeStrategy.discard
        case PathList("META-INF", xs@_*) => MergeStrategy.concat
        case PathList("org", "bouncycastle", xs @_*) => MergeStrategy.first
        case PathList("jakarta", xs@_*) => MergeStrategy.last
        case PathList(ps @ _*) if ps.last endsWith ".proto" =>
          MergeStrategy.first
        case "module-info.class" => MergeStrategy.concat
        case PathList("META-INF", "services", xs @ _*) => MergeStrategy.concat
        case PathList("net", "jpountz", xs @ _*) => MergeStrategy.last
        case PathList("org", "lz4", xs @ _*) => MergeStrategy.last
        case PathList(ps @ _*) if ps.last endsWith ".proto" => MergeStrategy.first
        case PathList("com", "datastax", xs@_*) => MergeStrategy.first
        case PathList("org", "apache", xs@_*) => MergeStrategy.first
        case PathList("org", "glassfish", xs@_*) => MergeStrategy.first
        case PathList("com", "sun", xs@_*) => MergeStrategy.first
        case x if x.endsWith("reference-overrides.conf") => MergeStrategy.concat
        case x if x.endsWith("reference.conf") => MergeStrategy.concat
        case x if x.endsWith("version.conf") => MergeStrategy.concat
        case x if x.endsWith("module-info.class") => MergeStrategy.concat
        case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
        case x if x.endsWith("/liblz4-java.so") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4ByteBufferUtils$Match.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
        case x if x.endsWith("/module-info.class") => MergeStrategy.concat
        case x if x.endsWith("/LineTokenizer.class") => MergeStrategy.concat
        case x if x.endsWith("/LogSupport.class") => MergeStrategy.concat
        case x if x.endsWith("/MailcapFile.class") => MergeStrategy.concat
        case x if x.endsWith("/MimeTypeFile.class") => MergeStrategy.concat
        case x =>
          val oldStrategy = (assembly / assemblyMergeStrategy).value
          oldStrategy(x)
      },
      assembly / assemblyJarName := {
        s"${name.value}.jar"
      }
    ),
    dockerSettings,
    packageSettings
  )
  .dependsOn(common % "compile->compile;test->test")
  .dependsOn(`hmda-protocol`)
  .dependsOn(common)

lazy val `hmda-protocol` = (project in file("protocol"))
  .enablePlugins(
    JavaServerAppPackaging,
    sbtdocker.DockerPlugin,
    AshScriptPlugin,
    PekkoGrpcPlugin
  )
  .settings(hmdaBuildSettings: _*)

lazy val `hmda-analytics` = (project in file("hmda-analytics"))
  .enablePlugins(
    JavaServerAppPackaging,
    sbtdocker.DockerPlugin,
    AshScriptPlugin
  )
  .settings(hmdaBuildSettings: _*)
  .settings(
    Seq(
      Compile / mainClass := Some("hmda.analytics.HmdaAnalyticsApp"),
      dependencyOverrides ++= Seq(jacksonDatabind,pekkoSerialization ),
      assembly / assemblyMergeStrategy := {
        case "application.conf"                      => MergeStrategy.concat
        case "META-INF/io.netty.versions.properties" => MergeStrategy.concat
        case "META-INF/MANIFEST.MF" => MergeStrategy.discard
        case metaInfMatcher(_) => MergeStrategy.discard
        case PathList("META-INF", xs@_*) => MergeStrategy.concat
        case PathList("org", "bouncycastle", xs @_*) => MergeStrategy.first
        case PathList("jakarta", xs@_*) => MergeStrategy.last
        case PathList(ps @ _*) if ps.last endsWith ".proto" =>
          MergeStrategy.first
        case "module-info.class" => MergeStrategy.concat
        case PathList("META-INF", "services", xs @ _*) => MergeStrategy.concat
        case PathList("net", "jpountz", xs @ _*) => MergeStrategy.last
        case PathList("org", "lz4", xs @ _*) => MergeStrategy.last
        case PathList(ps @ _*) if ps.last endsWith ".proto" => MergeStrategy.first
        case PathList("com", "datastax", xs@_*) => MergeStrategy.first
        case PathList("org", "apache", xs@_*) => MergeStrategy.first
        case PathList("org", "glassfish", xs@_*) => MergeStrategy.first
        case PathList("com", "sun", xs@_*) => MergeStrategy.first
        case x if x.endsWith("reference-overrides.conf") => MergeStrategy.concat
        case x if x.endsWith("reference.conf") => MergeStrategy.concat
        case x if x.endsWith("version.conf") => MergeStrategy.concat
        case x if x.endsWith("module-info.class") => MergeStrategy.concat
        case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
        case x if x.endsWith("/liblz4-java.so") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4ByteBufferUtils$Match.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
        case x if x.endsWith("/module-info.class") => MergeStrategy.concat
        case x if x.endsWith("/LineTokenizer.class") => MergeStrategy.concat
        case x if x.endsWith("/LogSupport.class") => MergeStrategy.concat
        case x if x.endsWith("/MailcapFile.class") => MergeStrategy.concat
        case x if x.endsWith("/MimeTypeFile.class") => MergeStrategy.concat
        case x =>
          val oldStrategy = (assembly / assemblyMergeStrategy).value
          oldStrategy(x)
      },
      assembly / assemblyJarName := {
        s"${name.value}.jar"
      }
    ),
    dockerSettings,
    packageSettings
  )
  .dependsOn(common % "compile->compile;test->test")

  lazy val `hmda-auth` = (project in file("hmda-auth"))
    .enablePlugins(
      JavaServerAppPackaging,
      sbtdocker.DockerPlugin,
      AshScriptPlugin
    )
    .settings(hmdaBuildSettings: _*)
    .settings(
      Seq(
        libraryDependencies ++= keycloakServerDeps,
        Compile / mainClass := Some("hmda.authService.HmdaAuth"),
        dependencyOverrides ++= Seq(jacksonDatabind,pekkoSerialization ),
        assembly / assemblyJarName := {
          s"${name.value}.jar"
        },
        assembly / assemblyMergeStrategy := {
          case "application.conf"                      => MergeStrategy.concat
          case "META-INF/io.netty.versions.properties" => MergeStrategy.concat
          case "META-INF/MANIFEST.MF" => MergeStrategy.discard
          case metaInfMatcher(_) => MergeStrategy.discard
          case PathList("META-INF", xs @ _*) => MergeStrategy.concat
          case PathList("org", "bouncycastle", xs @_*) => MergeStrategy.first
          case PathList("jakarta", xs @ _*) => MergeStrategy.last
          case "reference.conf" => MergeStrategy.concat
          case PathList(ps @ _*) if ps.last endsWith ".proto" =>
            MergeStrategy.first
          case "module-info.class" => MergeStrategy.concat
          case PathList("META-INF", "services", xs @ _*) => MergeStrategy.concat
          case PathList("net", "jpountz", xs @ _*) => MergeStrategy.last
          case PathList("org", "lz4", xs @ _*) => MergeStrategy.last
          case PathList(ps @ _*) if ps.last endsWith ".proto" => MergeStrategy.first
          case PathList("com", "datastax", xs@_*) => MergeStrategy.first
          case PathList("org", "apache", xs@_*) => MergeStrategy.first
          case PathList("org", "glassfish", xs@_*) => MergeStrategy.first
          case PathList("com", "sun", xs@_*) => MergeStrategy.first
          case x if x.endsWith("reference-overrides.conf") => MergeStrategy.concat
          case x if x.endsWith("reference.conf") => MergeStrategy.concat
          case x if x.endsWith("version.conf") => MergeStrategy.concat
          case x if x.endsWith("module-info.class") => MergeStrategy.concat
          case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
          case x if x.endsWith("/liblz4-java.so") => MergeStrategy.concat
          case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
          case x if x.endsWith("/LZ4ByteBufferUtils$Match.class") => MergeStrategy.concat
          case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
          case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
          case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
          case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
          case x if x.endsWith("/module-info.class") => MergeStrategy.concat
          case x if x.endsWith("/LineTokenizer.class") => MergeStrategy.concat
          case x if x.endsWith("/LogSupport.class") => MergeStrategy.concat
          case x if x.endsWith("/MailcapFile.class") => MergeStrategy.concat
          case x if x.endsWith("/MimeTypeFile.class") => MergeStrategy.concat
          case x =>
          val oldStrategy = (assembly / assemblyMergeStrategy).value
          oldStrategy(x)
        }
      ),
      dockerSettings,
      packageSettings
    )
    .dependsOn(common % "compile->compile;test->test")
    .dependsOn(`institutions-api` % "compile->compile;test->test")

lazy val `rate-limit` = (project in file("rate-limit"))
  .enablePlugins(
    JavaServerAppPackaging,
    sbtdocker.DockerPlugin,
    AshScriptPlugin
  )
  .settings(hmdaBuildSettings: _*)
  .settings(
    Seq(
      libraryDependencies ++= commonDeps ++ pekkoDeps ++ pekkoHttpDeps :+ guava,
      dependencyOverrides ++= Seq(jacksonDatabind,pekkoSerialization ),
      Compile / mainClass := Some("hmda.rateLimit.RateLimitApp"),
      assembly / assemblyMergeStrategy := {
        case "application.conf"                      => MergeStrategy.concat
        case "META-INF/io.netty.versions.properties" => MergeStrategy.concat
        case "META-INF/MANIFEST.MF" => MergeStrategy.discard
        case metaInfMatcher(_) => MergeStrategy.discard
        case PathList("META-INF", xs@_*) => MergeStrategy.concat
        case PathList("org", "bouncycastle", xs @_*) => MergeStrategy.first
        case PathList("jakarta", xs@_*) => MergeStrategy.last
        case PathList(ps @ _*) if ps.last endsWith ".proto" =>
          MergeStrategy.first
        case "module-info.class" => MergeStrategy.concat
        case PathList("META-INF", "services", xs @ _*) => MergeStrategy.concat
        case PathList("net", "jpountz", xs @ _*) => MergeStrategy.last
        case PathList("org", "lz4", xs @ _*) => MergeStrategy.last
        case PathList(ps @ _*) if ps.last endsWith ".proto" => MergeStrategy.first
        case PathList("com", "datastax", xs@_*) => MergeStrategy.first
        case PathList("org", "apache", xs@_*) => MergeStrategy.first
        case PathList("org", "glassfish", xs@_*) => MergeStrategy.first
        case PathList("com", "sun", xs@_*) => MergeStrategy.first
        case x if x.endsWith("reference-overrides.conf") => MergeStrategy.concat
        case x if x.endsWith("reference.conf") => MergeStrategy.concat
        case x if x.endsWith("version.conf") => MergeStrategy.concat
        case x if x.endsWith("module-info.class") => MergeStrategy.concat
        case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
        case x if x.endsWith("/liblz4-java.so") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4ByteBufferUtils$Match.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
        case x if x.endsWith("/module-info.class") => MergeStrategy.concat
        case x if x.endsWith("/LineTokenizer.class") => MergeStrategy.concat
        case x if x.endsWith("/LogSupport.class") => MergeStrategy.concat
        case x if x.endsWith("/MailcapFile.class") => MergeStrategy.concat
        case x if x.endsWith("/MimeTypeFile.class") => MergeStrategy.concat
        case x =>
          val oldStrategy = (assembly / assemblyMergeStrategy).value
          oldStrategy(x)
      },
      assembly / assemblyJarName := {
        s"${name.value}.jar"
      }
    ),
    dockerSettings,
    packageSettings
  )
  .dependsOn(common % "compile->compile;test->test")
  .dependsOn(`hmda-protocol`)

lazy val `data-browser` = (project in file("data-browser"))
  .enablePlugins(
    JavaServerAppPackaging,
    sbtdocker.DockerPlugin,
    AshScriptPlugin
  )
  .settings(hmdaBuildSettings: _*)
  .settings(
    Seq(
      libraryDependencies ++= commonDeps ++ pekkoDeps ++ pekkoHttpDeps ++ circeDeps ++ slickDeps ++
        enumeratumDeps :+ monix :+ lettuce :+ scalaMock,
      dependencyOverrides ++= Seq(jacksonDatabind,pekkoSerialization ),
      assembly / assemblyMergeStrategy := {
        case "application.conf"                      => MergeStrategy.concat
        case "META-INF/io.netty.versions.properties" => MergeStrategy.concat
        case "META-INF/MANIFEST.MF" => MergeStrategy.discard
        case metaInfMatcher(_) => MergeStrategy.discard
        case PathList("META-INF", xs@_*) => MergeStrategy.concat
        case PathList("org", "bouncycastle", xs @_*) => MergeStrategy.first
        case PathList("jakarta", xs@_*) => MergeStrategy.last
        case PathList(ps @ _*) if ps.last endsWith ".proto" =>
          MergeStrategy.first
        case "module-info.class" => MergeStrategy.concat
        case PathList("META-INF", "services", xs @ _*) => MergeStrategy.concat
        case PathList("net", "jpountz", xs @ _*) => MergeStrategy.last
        case PathList("org", "lz4", xs @ _*) => MergeStrategy.last
        case PathList(ps @ _*) if ps.last endsWith ".proto" => MergeStrategy.first
        case PathList("com", "datastax", xs@_*) => MergeStrategy.first
        case PathList("org", "apache", xs@_*) => MergeStrategy.first
        case PathList("org", "glassfish", xs@_*) => MergeStrategy.first
        case PathList("com", "sun", xs@_*) => MergeStrategy.first
        case x if x.endsWith("reference-overrides.conf") => MergeStrategy.concat
        case x if x.endsWith("reference.conf") => MergeStrategy.concat
        case x if x.endsWith("version.conf") => MergeStrategy.concat
        case x if x.endsWith("module-info.class") => MergeStrategy.concat
        case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
        case x if x.endsWith("/liblz4-java.so") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4ByteBufferUtils$Match.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
        case x if x.endsWith("/module-info.class") => MergeStrategy.concat
        case x if x.endsWith("/LineTokenizer.class") => MergeStrategy.concat
        case x if x.endsWith("/LogSupport.class") => MergeStrategy.concat
        case x if x.endsWith("/MailcapFile.class") => MergeStrategy.concat
        case x if x.endsWith("/MimeTypeFile.class") => MergeStrategy.concat
        case x =>
          val oldStrategy = (assembly / assemblyMergeStrategy).value
          oldStrategy(x)
      },
      assembly / assemblyJarName := {
        s"${name.value}.jar"
      }
    ),
    dockerSettings,
    packageSettings
  )
  .dependsOn(common % "compile->compile;test->test")

lazy val `submission-errors` = (project in file("submission-errors"))
  .enablePlugins(JavaServerAppPackaging, sbtdocker.DockerPlugin, AshScriptPlugin)
  .settings(hmdaBuildSettings)
  .settings(
    Seq(
      libraryDependencies ++= commonDeps ++ pekkoDeps ++ pekkoHttpDeps ++ circeDeps ++ slickDeps :+ monix :+ slickPostgres,
      dependencyOverrides ++= Seq(jacksonDatabind,pekkoSerialization ),
      assembly / assemblyMergeStrategy := {
        case "application.conf" => MergeStrategy.concat
        case "META-INF/io.netty.versions.properties" => MergeStrategy.concat
        case "META-INF/MANIFEST.MF" => MergeStrategy.discard
        case metaInfMatcher(_) => MergeStrategy.discard
        case PathList("META-INF", xs@_*) => MergeStrategy.concat
        case PathList("org", "bouncycastle", xs @_*) => MergeStrategy.first
        case PathList("jakarta", xs@_*) => MergeStrategy.last
        case PathList(ps@_*) if ps.last endsWith ".proto" =>
          MergeStrategy.first
        case "module-info.class" => MergeStrategy.concat
        case PathList("META-INF", "services", xs @ _*) => MergeStrategy.concat
        case PathList("net", "jpountz", xs @ _*) => MergeStrategy.last
        case PathList("org", "lz4", xs @ _*) => MergeStrategy.last
        case PathList(ps @ _*) if ps.last endsWith ".proto" => MergeStrategy.first
        case PathList("com", "datastax", xs@_*) => MergeStrategy.first
        case PathList("org", "apache", xs@_*) => MergeStrategy.first
        case PathList("org", "glassfish", xs@_*) => MergeStrategy.first
        case PathList("com", "sun", xs@_*) => MergeStrategy.first
        case x if x.endsWith("reference-overrides.conf") => MergeStrategy.concat
        case x if x.endsWith("reference.conf") => MergeStrategy.concat
        case x if x.endsWith("version.conf") => MergeStrategy.concat
        case x if x.endsWith("module-info.class") => MergeStrategy.concat
        case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
        case x if x.endsWith("/liblz4-java.so") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4ByteBufferUtils$Match.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
        case x if x.endsWith("/module-info.class") => MergeStrategy.concat
        case x if x.endsWith("/LineTokenizer.class") => MergeStrategy.concat
        case x if x.endsWith("/LogSupport.class") => MergeStrategy.concat
        case x if x.endsWith("/MailcapFile.class") => MergeStrategy.concat
        case x if x.endsWith("/MimeTypeFile.class") => MergeStrategy.concat
        case x =>
          val oldStrategy = (assembly / assemblyMergeStrategy).value
          oldStrategy(x)
      },
    ),
    dockerSettings,
    packageSettings
  )
  .dependsOn(common % "compile->compile;test->test") // allows you to depend on code from both compile and test scopes

lazy val `email-service` = (project in file("email-service"))
  .enablePlugins(JavaServerAppPackaging, sbtdocker.DockerPlugin, AshScriptPlugin)
  .settings(hmdaBuildSettings: _*)
  .settings(
    Seq(
      Compile / mainClass := Some("hmda.publication.lar.EmailReceiptApp"),
      dependencyOverrides ++= Seq(jacksonDatabind,pekkoSerialization ),
      assembly / assemblyMergeStrategy := {
        case "application.conf"                      => MergeStrategy.concat
        case "META-INF/io.netty.versions.properties" => MergeStrategy.concat
        case "META-INF/MANIFEST.MF" => MergeStrategy.discard
        case metaInfMatcher(_) => MergeStrategy.discard
        case PathList("META-INF", xs@_*) => MergeStrategy.concat
        case PathList("org", "bouncycastle", xs @_*) => MergeStrategy.first
        case PathList("jakarta", xs@_*) => MergeStrategy.last
        case PathList(ps @ _*) if ps.last endsWith ".proto" =>
          MergeStrategy.first
        case "module-info.class" => MergeStrategy.concat
        case PathList("META-INF", "services", xs @ _*) => MergeStrategy.concat
        case PathList("net", "jpountz", xs @ _*) => MergeStrategy.last
        case PathList("org", "lz4", xs @ _*) => MergeStrategy.last
        case PathList(ps @ _*) if ps.last endsWith ".proto" => MergeStrategy.first
        case PathList("com", "datastax", xs@_*) => MergeStrategy.first
        case PathList("org", "apache", xs@_*) => MergeStrategy.first
        case PathList("org", "glassfish", xs@_*) => MergeStrategy.first
        case PathList("com", "sun", xs@_*) => MergeStrategy.first
        case x if x.endsWith("reference-overrides.conf") => MergeStrategy.concat
        case x if x.endsWith("reference.conf") => MergeStrategy.concat
        case x if x.endsWith("version.conf") => MergeStrategy.concat
        case x if x.endsWith("module-info.class") => MergeStrategy.concat
        case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
        case x if x.endsWith("/liblz4-java.so") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4ByteBufferUtils$Match.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
        case x if x.endsWith("/module-info.class") => MergeStrategy.last
        case x if x.endsWith("/LineTokenizer.class") => MergeStrategy.last
        case x if x.endsWith("/LogSupport.class") => MergeStrategy.last
        case x if x.endsWith("/MailcapFile.class") => MergeStrategy.last
        case x if x.endsWith("/MimeTypeFile.class") => MergeStrategy.last
        case x =>
          val oldStrategy = (assembly / assemblyMergeStrategy).value
          oldStrategy(x)
      },
      assembly / assemblyJarName := {
        s"${name.value}.jar"
      },
      libraryDependencies ++= monix :: pekkoKafkaStreams :: kafkaClients :: awsSesSdk :: jakartaMail :: logback :: Nil
    ),
    dockerSettings,
    packageSettings
  )
  .dependsOn(common % "compile->compile;test->test")
  .dependsOn(`hmda-protocol`)

lazy val `hmda-quarterly-data-service` = (project in file ("hmda-quarterly-data-service"))
  .enablePlugins(
    JavaServerAppPackaging,
    sbtdocker.DockerPlugin,
    AshScriptPlugin
  )
  .settings(hmdaBuildSettings: _*)
  .settings(
    Seq(
      libraryDependencies ++= commonDeps ++ pekkoDeps ++ pekkoHttpDeps ++ circeDeps ++ slickDeps ++
        enumeratumDeps :+ monix :+ lettuce :+ scalaMock,
      dependencyOverrides ++= Seq(jacksonDatabind,pekkoSerialization ),
      assembly / assemblyMergeStrategy := {
        case "application.conf"                      => MergeStrategy.concat
        case "META-INF/io.netty.versions.properties" => MergeStrategy.concat
        case "META-INF/MANIFEST.MF" => MergeStrategy.discard
        case metaInfMatcher(_) => MergeStrategy.discard
        case PathList("META-INF", xs@_*) => MergeStrategy.concat
        case PathList("org", "bouncycastle", xs @_*) => MergeStrategy.first
        case PathList("jakarta", xs@_*) => MergeStrategy.last
        case PathList(ps @ _*) if ps.last endsWith ".proto" =>
          MergeStrategy.first
        case "module-info.class" => MergeStrategy.concat
        case PathList("META-INF", "services", xs @ _*) => MergeStrategy.concat
        case PathList("net", "jpountz", xs @ _*) => MergeStrategy.last
        case PathList("org", "lz4", xs @ _*) => MergeStrategy.last
        case PathList(ps @ _*) if ps.last endsWith ".proto" => MergeStrategy.first
        case PathList("com", "datastax", xs@_*) => MergeStrategy.first
        case PathList("org", "apache", xs@_*) => MergeStrategy.first
        case PathList("org", "glassfish", xs@_*) => MergeStrategy.first
        case PathList("com", "sun", xs@_*) => MergeStrategy.first
        case x if x.endsWith("reference-overrides.conf") => MergeStrategy.concat
        case x if x.endsWith("reference.conf") => MergeStrategy.concat
        case x if x.endsWith("version.conf") => MergeStrategy.concat
        case x if x.endsWith("module-info.class") => MergeStrategy.concat
        case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
        case x if x.endsWith("/liblz4-java.so") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4ByteBufferUtils$Match.class") => MergeStrategy.concat
        case x if x.endsWith("/LZ4BlockInputStream.class") => MergeStrategy.concat
        case x if x.endsWith("/XXHash64JavaSafe.class") => MergeStrategy.concat
        case x if x.endsWith("/module-info.class") => MergeStrategy.concat
        case x if x.endsWith("/LineTokenizer.class") => MergeStrategy.concat
        case x if x.endsWith("/LogSupport.class") => MergeStrategy.concat
        case x if x.endsWith("/MailcapFile.class") => MergeStrategy.concat
        case x if x.endsWith("/MimeTypeFile.class") => MergeStrategy.concat
        case x =>
          val oldStrategy = (assembly / assemblyMergeStrategy).value
          oldStrategy(x)
      },
      assembly / assemblyJarName := {
        s"${name.value}.jar"
      }
    ),
    dockerSettings,
    packageSettings
  )
  .dependsOn(common % "compile->compile;test->test")
  .dependsOn(`hmda-protocol` % "compile->compile;test->test")
