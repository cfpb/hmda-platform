package hmda.persistence.config

import com.typesafe.config.ConfigFactory

object PersistenceConfig {
  val levelDBconfig = ConfigFactory.parseString(
    """
      |akka {
      |  loglevel = "INFO"
      |  actor.warn-about-java-serializer-usage = off
      |  persistence {
      |    journal.plugin = "akka.persistence.journal.leveldb"
      |    journal.leveldb.native = off
      |    journal.leveldb.dir = "target/journal"
      |    snapshot-store.plugin = "akka.persistence.snapshot-store.local"
      |    snapshot-store.local.dir = "target/snapshots"
      |    query.journal.id = "akka.persistence.query.journal.leveldb"
      |  }
      |}
    """.stripMargin
  ).withFallback(ConfigFactory.load())

  val cassandraConfig = ConfigFactory.load()

  val configuration = {
    val isDemo = ConfigFactory.load().getBoolean("hmda.isDemo")
    if (isDemo) levelDBconfig else cassandraConfig
  }
}
