package hmda.persistence.util

import java.io.File
import akka.persistence.cassandra.testkit.CassandraLauncher

object CassandraUtil {

  def startEmbeddedCassandra(): Unit = {
    val databaseDirectory = new File("target/hmda-db")
    CassandraLauncher.start(
      databaseDirectory,
      CassandraLauncher.DefaultTestConfigResource,
      clean = true,
      port = 9042,
      CassandraLauncher.classpathForResources("logback-test.xml")
    )

    //shut down Cassandra when JVM stops
    sys.addShutdownHook(shutdown())

  }

  def shutdown(): Unit = CassandraLauncher.stop()

}
