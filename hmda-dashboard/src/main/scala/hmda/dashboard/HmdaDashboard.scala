package hmda.dashboard

import akka.actor.ActorSystem
import hmda.dashboard.api.HmdaDashboardApi
import org.slf4j.LoggerFactory


object HmdaDashboard extends App {

  val log = LoggerFactory.getLogger("hmda-dashboard")
  log.info("Starting hmda-dashboard")

  implicit val system: ActorSystem = ActorSystem("hmda-dashboard")
  system.actorOf(HmdaDashboardApi.props(), "hmda-dashboard")

}