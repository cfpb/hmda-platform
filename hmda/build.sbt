import com.lightbend.cinnamon.sbt.Cinnamon
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

cinnamon in run := true
cinnamon in test := true

libraryDependencies += Cinnamon.library.cinnamonAkka
libraryDependencies += Cinnamon.library.cinnamonAkkaHttp
libraryDependencies += Cinnamon.library.cinnamonAkkaStream
libraryDependencies += Cinnamon.library.cinnamonPrometheus
libraryDependencies += Cinnamon.library.cinnamonPrometheusHttpServer
libraryDependencies += "com.lightbend.akka" %% "akka-diagnostics" % "1.1.7"

javaOptions in Universal ++= Seq(
  "-J-XX:+UnlockExperimentalVMOptions",
  "-J-XX:+UseCGroupMemoryLimitForHeap"
)

javaOptions in reStart ++= (javaOptions in run).value
