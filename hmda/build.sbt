//import com.lightbend.cinnamon.sbt.Cinnamon
import com.typesafe.sbt.packager.docker._

version := "latest"

packageName in Docker := "hmda-platform"

dockerExposedPorts := Vector(8080, 8081, 8082, 19999, 9080, 1099)

dockerEntrypoint ++= Seq(
  """-Dakka.remote.netty.tcp.hostname="$(eval "echo $AKKA_REMOTING_BIND_HOST")"""",
  """-Dakka.management.http.hostname="$(eval "echo $AKKA_REMOTING_BIND_HOST")""""
)

//Add Yourkit profiler
//This requires openjdk:11 as base image as opposed to openjdk:11-jre-slim"
//dockerCommands ++= Seq(
//  ExecCmd("RUN", "bash", "-c",
//  "curl -o /tmp/YourKit-JavaProfiler-2019.8-docker.zip https://www.yourkit.com/download/docker/YourKit-JavaProfiler-2019.8-docker.zip && " +
//   "unzip /tmp/YourKit-JavaProfiler-2019.8-docker.zip -d /opt/docker && " +
//   "rm /tmp/YourKit-JavaProfiler-2019.8-docker.zip")
//)

dockerCommands :=
  dockerCommands.value.flatMap {
    case ExecCmd("ENTRYPOINT", args @ _*) =>
      Seq(Cmd("ENTRYPOINT", args.mkString(" ")))
    case v => Seq(v)
  }

javaOptions in Universal ++= Seq(
  "-J-XX:+UnlockExperimentalVMOptions",
  "-J-XX:+UseContainerSupport",
  "-J-XX:+UnlockDiagnosticVMOptions",
  "-J-XX:+DebugNonSafepoints",
  "-J-XX:+HeapDumpOnOutOfMemoryError",
  "-J-XX:HeapDumpPath=/dumps/oom.bin",
  "-Dcom.sun.management.jmxremote",
  "-Dcom.sun.management.jmxremote.authenticate=false",
  "-Dcom.sun.management.jmxremote.ssl=false",
  "-Dcom.sun.management.jmxremote.local.only=false",
  "-Dcom.sun.management.jmxremote.port=1099",
  "-Dcom.sun.management.jmxremote.rmi.port=1099",
  "-Djava.rmi.server.hostname=127.0.0.1"
  //  ,"-agentpath:/opt/docker/YourKit-JavaProfiler-2019.8/bin/linux-x86-64/libyjpagent.so=port=10001,listen=all,dir=/opt/docker,sampling_settings_path=/ope/docker"
)

javaOptions in reStart ++= (javaOptions in run).value