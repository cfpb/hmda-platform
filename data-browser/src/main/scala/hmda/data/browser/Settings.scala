package hmda.data.browser

import akka.actor.{ActorSystem => UntypedActorSystem}
import com.typesafe.config.Config

import scala.concurrent.duration._

class Settings(config: Config) {
  def this(system: UntypedActorSystem) = this(system.settings.config)

   private def getDuration(key: String): FiniteDuration = {
    val duration = config.getDuration(key)
    FiniteDuration(duration.toMillis, MILLISECONDS)
  }

   object server {
    val host: String = config.getString("server.bindings.address")
    val port: Int = config.getInt("server.bindings.port")
    val askTimeout: FiniteDuration = getDuration("server.ask-timeout")
  }
}

 object Settings {
  def apply(system: UntypedActorSystem): Settings = new Settings(system)
  def apply(config: Config): Settings = new Settings(config)
}