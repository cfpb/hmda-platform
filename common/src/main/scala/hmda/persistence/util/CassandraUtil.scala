package hmda.persistence.util

import java.io.File

import akka.persistence.cassandra.testkit.CassandraLauncher

object CassandraUtil {

  def startEmbeddedCassandra(): Unit = {
    val port = sys.env.getOrElse("HMDA_CASSANDRA_LOCAL_PORT", 9042.toString).toInt
    val databaseDirectory = new File(s"target/hmda-db-${port}")
    CassandraLauncher.start(
      databaseDirectory,
      CassandraLauncher.DefaultTestConfigResource,
      clean = true,
      port = port
    )

    //shut down Cassandra when JVM stops
    sys.addShutdownHook(shutdown())

  }

  def shutdown(): Unit = CassandraLauncher.stop()

}
