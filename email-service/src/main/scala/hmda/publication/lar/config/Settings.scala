package hmda.publication.lar.config

import akka.actor.{ActorSystem => UntypedActorSystem}
import com.typesafe.config.Config
import hmda.util.BankFilterUtils._

class Settings(config: Config) {
  def this(system: UntypedActorSystem) = this(system.settings.config)

  def raw: Config = config

  val bankFilterList = getFilterList()

  object email {
    private val emailPath   = config.getConfig("hmda.lar.email")
    val subject: String     = emailPath.getString("subject")
    val content: String     = emailPath.getString("content")
    val fromAddress: String = emailPath.getString("from-address")
    val parallelism: Int    = emailPath.getInt("parallelism")
    val timeToRetry: Int    = emailPath.getInt("time-to-retry")

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
