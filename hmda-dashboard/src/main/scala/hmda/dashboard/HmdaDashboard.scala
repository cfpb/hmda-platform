package hmda.dashboard

import akka.Done
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.dashboard.api.HmdaDashboardApi
import org.slf4j.LoggerFactory
import hmda.query.DbConfiguration.dbConfig
import hmda.query.HmdaQuery.{readRawData, readSubmission}
import hmda.util.BankFilterUtils._
import hmda.util.streams.FlowUtils.framing
import hmda.utils.YearUtils.Period

import scala.concurrent.Future
import scala.concurrent.duration._

object HmdaDashboard extends App {

  val log = LoggerFactory.getLogger("hmda-dashboard")
  log.info("Starting hmda-dashboard")

  implicit val system: ActorSystem = ActorSystem("hmda-dashboard")
  system.actorOf(HmdaDashboardApi.props(), "hmda-dashboard")

}