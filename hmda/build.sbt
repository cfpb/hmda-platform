import com.typesafe.sbt.packager.docker._

version := "latest"

packageName in Docker := "hmda-platform"

dockerExposedPorts := Vector(8080, 8081, 8082, 19999, 9080)

dockerEntrypoint ++= Seq(
  """-Dakka.remote.netty.tcp.hostname="$(eval "echo $AKKA_REMOTING_BIND_HOST")"""",
  """-Dakka.management.http.hostname="$(eval "echo $AKKA_REMOTING_BIND_HOST")""""
)

dockerCommands :=
  dockerCommands.value.flatMap {
    case ExecCmd("ENTRYPOINT", args @ _*) =>
      Seq(Cmd("ENTRYPOINT", args.mkString(" ")))
    case v => Seq(v)
  }

javaOptions in Universal ++= Seq(
  "-J-XX:+UnlockExperimentalVMOptions",
  "-J-XX:+UseCGroupMemoryLimitForHeap",
  "-J-XX:+PrintGCDetails",
  "-J-XX:+PrintGCDateStamps",
  "-J-Xloggc:/opt/docker/gc.log",
  "-J-XX:+HeapDumpOnOutOfMemoryError",
  "-J-XX:HeapDumpPath=/dumps/oom.bin"
)

javaOptions in reStart ++= (javaOptions in run).value
