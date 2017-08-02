package hmda.validation.rules

import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.validation.AS
import scala.concurrent.duration._

trait StatsLookup {
  val configuration = ConfigFactory.load()
  val duration = configuration.getInt("hmda.actor.timeout")
  implicit val timeout = Timeout(duration.seconds)

  def validationStats(implicit system: AS[_]) = system.actorSelection("/user/validation-stats")

}
