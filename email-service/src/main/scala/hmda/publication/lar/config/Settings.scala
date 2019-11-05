package hmda.publication.lar.config

import akka.actor.{ ActorSystem => UntypedActorSystem }
import com.typesafe.config.Config
import hmda.messages.pubsub.HmdaGroups

class Settings(config: Config) {
  def this(system: UntypedActorSystem) = this(system.settings.config)

  def raw: Config = config

  object email {
    private val emailPath   = config.getConfig("hmda.lar.email")
    val subject: String     = emailPath.getString("subject")
    val content: String     = emailPath.getString("content")
    val fromAddress: String = emailPath.getString("from-address")
    val parallelism: Int    = emailPath.getInt("parallelism")
  }

  object kafka {
    val bootstrapServers: String = config.getString("kafka.hosts")
    val commitSettings: Config   = config.getConfig("kafka.commit")
  }
}

object Settings {
  def apply(system: UntypedActorSystem): Settings = new Settings(system)
  def apply(config: Config): Settings             = new Settings(config)
}
